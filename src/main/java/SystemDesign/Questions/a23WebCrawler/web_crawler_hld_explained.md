# Web Crawler: High-Level Design Explained in Detail

This document explains every component in the Web Crawler HLD image: what it does, why it exists, and how data moves through it. It goes deepest on three topics: **priority**, **politeness** and **content deduplication**.

The core idea to remember:

> A crawler is a loop. Take a URL from the **Frontier**, **fetch** it politely, **parse** out text and links, **drop** anything already seen or duplicated, **store** what is new, and feed the new links back into the Frontier.

---

## Table of contents

1. [Problem statement and requirements](#1-problem-statement-and-requirements)
2. [Capacity estimation](#2-capacity-estimation)
3. [High-level architecture](#3-high-level-architecture)
4. [Component-by-component explanation](#4-component-by-component-explanation)
5. [End-to-end crawl flow](#5-end-to-end-crawl-flow)
6. [URL Frontier: priority and politeness in detail](#6-url-frontier-priority-and-politeness-in-detail)
7. [URL normalization and URL dedup (Bloom filter)](#7-url-normalization-and-url-dedup-bloom-filter)
8. [Content dedup: how duplicates are detected and removed](#8-content-dedup-how-duplicates-are-detected-and-removed)
9. [Data model](#9-data-model)
10. [Scaling and sharding](#10-scaling-and-sharding)
11. [Failure handling, traps and robustness](#11-failure-handling-traps-and-robustness)
12. [Freshness and recrawling](#12-freshness-and-recrawling)
13. [Trade-offs and design decisions](#13-trade-offs-and-design-decisions)
14. [Interview-ready summary](#14-interview-ready-summary)

---

## 1. Problem statement and requirements

A web crawler (also called a spider or bot) automatically discovers and downloads web pages. Typical uses:

- Feeding a **search engine** index.
- Building **datasets** for ML or analytics.
- **Monitoring** pages for changes (prices, news, compliance).
- **Archiving** (for example, web archives).

### Functional requirements

| Requirement | Meaning |
| --- | --- |
| Start from seed URLs | Begin with a known set of URLs, sitemaps or earlier crawl results |
| Discover new URLs | Extract links from every fetched page and enqueue them |
| Download page content | Fetch HTML (and optionally other content types) over HTTP(S) |
| Store content | Keep raw and parsed content for downstream consumers such as an indexer |
| Avoid duplicates | Do not crawl the same URL twice, and do not store the same content twice |
| Respect site rules | Obey `robots.txt`, crawl-delay and rate limits |
| Recrawl | Revisit pages to keep content fresh |

### Non-functional requirements

| Requirement | Meaning |
| --- | --- |
| **Scalable** | Handle billions of pages by adding machines |
| **Polite** | Never overload a website; behave like a good citizen |
| **Robust** | Survive bad HTML, slow servers, crawler traps and node failures |
| **Extensible** | Easy to add new content types, parsers or downstream consumers |
| **Efficient** | Do not waste bandwidth, storage or compute on duplicates |

---

## 2. Capacity estimation

These are **illustrative assumptions** for design discussion, not measurements.

| Metric | Calculation | Estimate |
| --- | --- | --- |
| Pages per month | Given | 1 billion |
| Average crawl rate | 1B / (30 × 86,400 s) | ≈ 386 pages/s |
| Peak crawl rate | Average × 2 | ≈ 770 pages/s |
| Average page size | Assumed | ≈ 100 KB |
| Average bandwidth | 386 × 100 KB | ≈ 39 MB/s ≈ 310 Mbps |
| Raw storage per month | 1B × 100 KB | ≈ 100 TB |
| After compression (~3×) | 100 TB / 3 | ≈ 35 TB |
| Bloom filter for 1B URLs | 1B × 9.6 bits (1% false positives) | ≈ 1.2 GB |

Takeaways:

- The workload is **network- and storage-bound**, not CPU-bound.
- Fetching is dominated by **waiting on remote servers**, so fetchers must be **asynchronous** with many concurrent connections.
- The "seen URL" set must fit in memory, which is why a Bloom filter is used.

---

## 3. High-level architecture

```
                         ┌──────────────────────── CRAWL LOOP ───────────────────────┐
                         │                                                            │
 Seed URLs ──► URL Frontier ──► Fetcher Workers ──► Parser + Extractor ──► Content Dedup
 (sitemaps)   (priority +        (async HTTP,        (text, links,          (SHA-256 +
               politeness)        retries)            metadata)              SimHash)
                  ▲                  │  ▲                 │                     │
                  │                  ▼  │                 │ extracted links     │ unique pages
          unseen URLs         DNS + robots Cache          ▼                     ▼
                  │                                  URL Seen Filter      Blob Store ──► Indexer /
                  └────────────────────────────────  (Bloom filter)      (raw + parsed)   Downstream
                                                                                           (via Kafka)
            Metadata DB (URL state, ETag, depth, last/next crawl) ──► recrawl schedule ──► Frontier
```

There are two loops:

1. **Discovery loop:** page, then links, then Seen Filter, then Frontier, then page.
2. **Freshness loop:** Metadata DB, then recrawl schedule, then Frontier.

---

## 4. Component-by-component explanation

### 4.1 Seed URLs

The starting set of URLs. Sources include:

- A hand-picked list of high-quality sites (news, directories, popular domains).
- `sitemap.xml` files that sites publish to list their pages.
- URLs from earlier crawls.
- URLs submitted by users or other systems.

The quality of seeds shapes the crawl: good seeds link to many other good pages. Seeds usually get the highest priority.

### 4.2 URL Frontier

The Frontier is the crawler's **brain and to-do list**. It holds every URL waiting to be crawled and answers one question: **"which URL should we fetch next?"**

It must satisfy two competing needs:

| Need | Question it answers | Mechanism |
| --- | --- | --- |
| **Priority** | Which URLs matter most? | Front queues F1..Fn |
| **Politeness** | When is it safe to hit this host again? | Per-host back queues plus a timing heap |

Full details are in [section 6](#6-url-frontier-priority-and-politeness-in-detail).

The Frontier is **persisted** (Kafka, Redis or a disk-backed queue). The backlog can hold billions of URLs, so it cannot live only in memory, and a crash must not lose it.

### 4.3 Fetcher workers

Fetchers download pages. Their job for each URL:

1. Resolve DNS (from the DNS cache).
2. Check `robots.txt` rules (from the robots cache).
3. Send an HTTP GET with a clear `User-Agent` and contact info.
4. Use **conditional requests** when recrawling: `If-None-Match` (ETag) and `If-Modified-Since`. A `304 Not Modified` response means no body is transferred.
5. Enforce **timeouts** (connect and total), a **maximum body size** and a **content-type filter** (for example, only `text/html`).
6. Follow a **limited number of redirects** and record the final URL.
7. Report the outcome (status code, response time, errors) to the Metadata DB and the Frontier.

Design points:

- **Asynchronous I/O** (event loops or non-blocking clients) lets one worker keep hundreds of connections open. Most time is spent waiting on servers, not computing.
- Fetchers are **stateless** apart from in-flight work, so they scale horizontally.
- Retries use **exponential backoff**, and permanently failing URLs go to a **dead-letter queue (DLQ)**.

### 4.4 DNS and robots.txt cache

**DNS cache.** DNS lookups add latency and, at crawl scale, become a hidden bottleneck. A local resolver cache (honoring TTLs) removes most lookups.

**robots.txt cache.** Before crawling any URL on a host, the crawler must know that host's rules. `robots.txt` is fetched once per host, parsed and cached, typically for hours up to a day. It holds:

- `Disallow` / `Allow` path rules for our user-agent.
- An optional `Crawl-delay`.
- Sometimes `Sitemap:` entries (a free source of seed URLs).

Edge cases the cache should handle:

| Situation | Typical behavior |
| --- | --- |
| `robots.txt` returns 404 | Treat as "everything allowed" |
| `robots.txt` returns 5xx or times out | Be conservative: delay or skip the host, retry later |
| Very large `robots.txt` | Parse only up to a size cap |

### 4.5 Parser and extractor

Takes the raw HTML and produces structured output:

- **Text content** (stripped of scripts and styles).
- **Title and metadata** (description, language, canonical link).
- **Outgoing links** (`<a href>`, and optionally `<link>`, `<iframe>`, `<img>` and sitemaps).
- **Directives** such as `<meta name="robots" content="noindex,nofollow">` and the `X-Robots-Tag` header.

It also normalizes each link: it resolves relative URLs against the page URL, lowercases the host, removes the `#fragment`, strips known tracking parameters and so on (see [section 7](#7-url-normalization-and-url-dedup-bloom-filter)).

Real-world HTML is messy, so the parser must be **fault-tolerant** (broken tags, wrong encodings) and run with **time and memory limits** so one pathological page cannot stall a worker.

### 4.6 URL Seen Filter

Checks "have we already seen this URL?" for every extracted link, **before** it enters the Frontier. This prevents infinite loops (A links to B, B links back to A) and wasted fetches. It is a **Bloom filter** backed by an exact seen-store. See [section 7](#7-url-normalization-and-url-dedup-bloom-filter).

### 4.7 Content Dedup

Checks "have we already seen this page's **content**?" **after** fetching. Different URLs often serve identical or near-identical content (mirrors, `http` vs `https`, `www` vs bare domain, print views, tracking-parameter variants). Content dedup saves storage and keeps the index clean. Full detail in [section 8](#8-content-dedup-how-duplicates-are-detected-and-removed).

### 4.8 Blob store

Large, cheap, durable object storage (S3, HDFS, GCS) for:

- Raw HTML (compressed).
- Parsed text and metadata.

Objects are keyed by **content hash**, so identical content naturally maps to the same object. Raw HTML is kept so it can be **reprocessed** later (for example, when the parser improves) without recrawling.

### 4.9 Metadata DB

Stores per-URL and per-host state. Used for scheduling, dedup bookkeeping, analytics and debugging. It needs high write throughput and key lookups, so a wide-column or key-value store (Cassandra, DynamoDB, Bigtable, HBase) is a good fit. See [section 9](#9-data-model).

### 4.10 Indexer / downstream consumers

Unique pages are published to **Kafka** (a "page fetched" event with a pointer to the blob). Consumers such as the search indexer, ML pipelines or analytics jobs read from there. Using a queue **decouples** crawling speed from downstream processing speed, so a slow indexer never slows the crawler.

---

## 5. End-to-end crawl flow

Following one URL through the system:

| Step | Component | What happens |
| --- | --- | --- |
| 1 | **Seed** | Seed URLs and sitemap URLs are loaded into the Frontier with high priority |
| 2 | **Frontier** | Picks a priority queue, then a host whose back queue is **due** (politeness delay elapsed), and hands out one URL |
| 3 | **Fetcher** | Resolves DNS (cache), checks `robots.txt` (cache), sends the HTTP GET (conditional if recrawling) |
| 4 | **Parser** | Extracts text, metadata and links; normalizes the links |
| 5 | **Dedup** | Each link goes through the URL Seen Filter; the page body goes through content dedup (SHA-256, then SimHash) |
| 6 | **Store** | Unique page goes to the Blob store; state goes to the Metadata DB; a "page fetched" event goes to Kafka |
| 7 | **Enqueue** | New unseen URLs get a priority score and go back into the Frontier; failures retry with backoff, then go to the DLQ |

The loop runs until the Frontier is empty, a **depth limit** is hit, or a **per-host page budget** is exhausted. For the open web, the Frontier effectively never empties.

---

## 6. URL Frontier: priority and politeness in detail

This is the most important component to understand. The design below follows the classic **Mercator-style** split: **front queues for priority, back queues for politeness**.

```
   new / recrawl URLs
          │
          ▼
   ┌──────────────┐
   │  Prioritizer │   assigns a priority score → bucket
   └──────┬───────┘
          ▼
 ┌────────────────────────────────────────┐
 │ FRONT QUEUES  (decide WHAT to crawl)   │
 │  F1 high   F2 medium   F3 low          │
 └──────┬─────────────────────────────────┘
        ▼   biased selection (F1 picked most often)
   ┌──────────────┐
   │    Router    │   host = hash(host) → that host's back queue
   └──────┬───────┘
          ▼
 ┌────────────────────────────────────────┐
 │ BACK QUEUES  (decide WHEN it is safe)  │
 │  a.com: [u1,u2,u3]  b.org: [u1,u2] ... │   one queue per host
 └──────┬─────────────────────────────────┘
        ▼
 ┌────────────────────────────────────────┐
 │ MIN-HEAP keyed by next_allowed_time    │   earliest-due host on top
 └──────┬─────────────────────────────────┘
        ▼
   Fetcher workers pull the next host that is due
```

### 6.1 What is priority?

**Priority** answers: *"If I can only crawl a fraction of the web right now, which URLs give the most value?"*

Not all URLs are equal:

- A home page of a major news site changes hourly and is valuable.
- A 15-levels-deep archive page on a tiny blog rarely changes and rarely matters.

If the Frontier were a plain FIFO queue (pure BFS), valuable pages could wait behind millions of low-value ones. Priority makes the crawler spend its limited bandwidth on the best URLs first.

**Signals used to compute a priority score** (illustrative formula):

```
priority(url) = w1 · importance  +  w2 · freshness_need  −  w3 · depth_penalty
```

| Signal | Meaning | Example |
| --- | --- | --- |
| **Importance** | How valuable the page or site is | PageRank-like score, number of inbound links, domain authority, traffic rank |
| **Freshness need** | How likely the page has changed since the last crawl | `change_rate × time_since_last_crawl`; news front pages score high |
| **Depth** | Distance from a seed | Deeper pages usually matter less, so they get a penalty |
| **Seed / source** | Where the URL came from | Seed URLs and sitemap URLs get a boost |
| **Past failures** | Whether the URL has been flaky | Frequently failing URLs get lower priority |

**How the score is used.** The Prioritizer maps the score into one of several **front queues**, F1 (highest) to Fn (lowest). When the Frontier needs a URL, it picks a front queue with a **bias toward higher priority**. For example, pick F1 about 60% of the time, F2 about 30% and F3 about 10%. This is better than "always F1 first" because a strict rule would **starve** low-priority queues forever. The biased pick keeps important pages moving fast while still eventually crawling the long tail.

### 6.2 What is politeness?

**Politeness** answers: *"How do we crawl a website without hurting it?"*

A crawler can easily send hundreds of requests per second at a single site. That can:

- Overload small servers (effectively a denial of service).
- Raise the site owner's bandwidth bill.
- Get our crawler **blocked** (IP bans, CAPTCHAs), which hurts our own coverage.

Politeness means following written and unwritten rules so a site barely notices us.

**The rules the system enforces:**

| Rule | How it is enforced |
| --- | --- |
| **Obey `robots.txt`** | Check cached `Allow`/`Disallow` rules for our user-agent before every fetch |
| **Honor `Crawl-delay`** | Minimum seconds between requests to the same host |
| **One connection per host at a time** | Only one in-flight request per host (one back queue per host) |
| **Minimum delay when none is specified** | Use a default, for example 1 second between requests |
| **Adaptive delay** | `delay = max(crawl_delay, k × last_response_time)`; for example k = 10, so slow servers are hit less often |
| **Back off on stress signals** | On `429 Too Many Requests` or `503`, honor `Retry-After` and increase the delay exponentially |
| **Limit by IP, not only hostname** | Many hostnames share one server (shared hosting); cap the combined rate per IP |
| **Identify ourselves** | Send a descriptive `User-Agent` with a URL or contact email so owners can reach us |
| **Respect page-level directives** | `noindex`, `nofollow`, `X-Robots-Tag` |

### 6.3 How the Frontier implements politeness

**Back queues: one queue per host.**

- The Router sends each URL into the back queue for its host (for example, all `a.com` URLs go to one queue).
- A single queue per host means URLs for that host leave **one at a time**, so the host is never hit in parallel.

**A timing heap decides "when".**

- A **min-heap** holds one entry per non-empty back queue: `(next_allowed_time, host_queue)`.
- Fetchers pop the entry with the **earliest** time. If that time is still in the future, the fetcher waits or checks the next entry.
- After a fetch completes, the host's `next_allowed_time = now + delay` (with `delay` from the rules above), and the entry goes back into the heap.

**Refilling.** When a back queue becomes empty, the Frontier pulls URLs from the front queues (using the biased priority selection) until it finds one for a host that has no active back queue, and creates a queue for it. Fetchers are therefore never idle while URLs exist, and priority still governs which hosts get a queue.

**Why this works well:**

- Many hosts are crawled **in parallel** (high total throughput), but each host is crawled **slowly and sequentially** (politeness).
- A site with 10 million pages is still crawled at a gentle rate; it just takes longer.

### 6.4 Worked example

Frontier state:

```
Front queues:  F1 = [ news.com/home ]     F2 = [ blog.org/post1, shop.net/item9 ]     F3 = [ news.com/archive/2009/x ]

Back queues:   news.com → [ /home, /archive/2009/x ]     blog.org → [ /post1 ]     shop.net → [ /item9 ]

Heap (next_allowed_time):  blog.org @ 10:00:00.0,  shop.net @ 10:00:00.4,  news.com @ 10:00:01.0
```

1. A fetcher pops `blog.org` (earliest, due now) and fetches `/post1`.
2. It measures the response time (say 300 ms) and sets `next_allowed_time = now + max(1 s, 10 × 0.3 s) = now + 3 s`.
3. It pops `shop.net` (due at :00.4), and so on.
4. When `news.com`'s time comes, `/home` goes before `/archive/2009/x` because it came from the higher-priority queue.

At no point do two requests hit the same host simultaneously, yet several hosts are active at once.

### 6.5 Persistence and recovery

The Frontier is the crawler's **state**, so it is persisted. A common layout keeps front queues and the URL backlog in Kafka or a disk-backed queue, with the small, hot part (the heap and the head of each back queue) in memory. After a crash, the in-memory part is rebuilt from the persisted state, and in-flight URLs are re-queued (making fetches **idempotent** matters).

---

## 7. URL normalization and URL dedup (Bloom filter)

### 7.1 Why normalize

Many different strings point to the same page. Before checking "seen?", every URL is converted to a **canonical form**:

| Rule | Example |
| --- | --- |
| Lowercase scheme and host | `HTTP://Example.COM/A` → `http://example.com/A` |
| Remove default port | `example.com:80` → `example.com` |
| Remove fragment | `/page#section2` → `/page` |
| Resolve relative paths | `/a/b/../c` → `/a/c` |
| Sort query parameters | `?b=2&a=1` → `?a=1&b=2` |
| Strip tracking / session parameters | `utm_source`, `fbclid`, `sessionid` removed |
| Handle trailing slash consistently | `/dir` vs `/dir/` follow one rule |
| Percent-encoding normalization | `%7e` → `~` |

Without normalization the "seen" check misses obvious duplicates and the crawler wastes work.

### 7.2 Why a Bloom filter

For billions of URLs, a hash set of full URLs needs hundreds of GB. A **Bloom filter** answers set membership in a fraction of the space.

**How it works:**

1. Keep a bit array of `m` bits, all 0.
2. Use `k` independent hash functions.
3. **Insert** URL `u`: compute `h1(u)..hk(u)` and set those `k` bits to 1.
4. **Query** URL `u`: compute the same `k` positions.
   - If **any bit is 0** → the URL is **definitely new**.
   - If **all bits are 1** → the URL is **probably seen**.

**Guarantees:**

- **No false negatives:** a URL we have seen is never reported as new, so we never crawl it twice by mistake.
- **Some false positives:** a new URL may look seen (because other URLs set its bits), so a few new URLs are wrongly skipped. This is an acceptable loss in a web-scale crawl, and it can be tuned.

**Sizing formulas** (n = items, p = target false-positive rate):

```
m = − n · ln(p) / (ln 2)²        bits needed
k = (m / n) · ln 2               number of hash functions
```

For `n = 1 billion` and `p = 1%`: `m/n ≈ 9.6` bits per URL, `k ≈ 7`, so memory ≈ **1.2 GB**. That fits comfortably in RAM (and can be sharded by host or URL hash across nodes).

**Limitations and mitigations:**

- You **cannot delete** from a standard Bloom filter. For recrawl and "forget" use cases, use a counting Bloom filter or rotate and rebuild filters periodically.
- Because of false positives, an exact **seen-store** (key-value store keyed by URL hash) can be consulted when a high-value URL gets a "probably seen" answer.
- A filter that fills up beyond its design size has a rising false-positive rate, so monitor load and rebuild or scale out.

---

## 8. Content dedup: how duplicates are detected and removed

URL dedup stops us fetching the same **URL** twice. Content dedup stops us **storing and indexing the same content** twice, even when it arrives from different URLs.

### 8.1 Why different URLs give the same content

- `http://` vs `https://`, `www.` vs bare domain.
- Mirrors and syndicated or scraped copies of articles.
- Print-friendly or AMP versions of a page.
- Tracking and session parameters that slipped past normalization.
- Pagination and sort-order variants showing the same items.
- Pages that differ only in a timestamp, ad block or "related articles" box (**near-duplicates**).

### 8.2 The two levels of content dedup

| Level | Catches | Technique | Cost |
| --- | --- | --- | --- |
| **Exact duplicate** | Byte-identical (after cleaning) bodies | Cryptographic hash (SHA-256) | Very cheap, exact |
| **Near duplicate** | Pages that are *almost* the same | **SimHash** (or MinHash / shingling) | Cheap, approximate |

### 8.3 Step 0: clean the content before hashing

Hashing the raw HTML would make two copies look different because of ads, timestamps or navigation. So the **parser** first extracts the **main text** and normalizes it:

- Strip scripts, styles and HTML tags.
- Remove boilerplate: navigation, headers, footers, cookie banners, ads.
- Lowercase, collapse whitespace, optionally drop punctuation.

The result is a stable "core text" representation that dedup runs on.

### 8.4 Exact dedup with SHA-256

1. Compute `content_hash = SHA-256(core_text)`.
2. Look it up in a **hash index** (key-value store or the Metadata DB).
3. **Found** → exact duplicate. **Not found** → insert the hash and continue.

This is O(1) per page and has essentially no false positives.

### 8.5 Near-duplicate dedup with SimHash

SHA-256 changes completely if a single character changes, so it cannot find near-duplicates. **SimHash** is a **locality-sensitive hash**: *similar* texts produce *similar* fingerprints, so similarity can be measured by how many bits differ (the **Hamming distance**).

**How to compute a 64-bit SimHash:**

1. **Tokenize** the core text into features (words, or word n-grams called shingles) and give each a **weight** (for example, term frequency, or TF-IDF).
2. **Hash** each feature to a 64-bit value.
3. Keep a 64-slot accumulator `V`, initialized to 0. For every feature, for each bit position `i`:
   - if the feature's bit `i` is **1** → add `+weight` to `V[i]`
   - if it is **0** → add `−weight` to `V[i]`
4. **Collapse** to the fingerprint: bit `i` = 1 if `V[i] > 0`, else 0.

**Worked example (8 bits for readability; real systems use 64):**

| Feature | Weight | Hash bits | Contribution per bit position (1 → +w, 0 → −w) |
| --- | --- | --- | --- |
| `web` | 2 | `10110010` | +2, −2, +2, +2, −2, −2, +2, −2 |
| `crawler` | 3 | `01101011` | −3, +3, +3, −3, +3, −3, +3, +3 |
| `design` | 2 | `11010001` | +2, +2, −2, +2, −2, −2, −2, +2 |
| **Sum V** | | | **1, 3, 3, 1, −1, −7, 3, 3** |
| **Fingerprint** | | | **1 1 1 1 0 0 1 1** → `11110011` |

(The hash bits above are made up for illustration.)

**Comparing fingerprints (Hamming distance = number of differing bits):**

```
Page A:  11110011
Page B:  11110111     differs in 1 bit  → distance 1   → near-duplicate
Page C:  00101100     differs in 7 bits → distance 7   → different page
```

Pages A and B differ by one changed word, so their fingerprints are almost the same. In a 64-bit setup, a common rule is: **Hamming distance ≤ 3 → near-duplicate** (the threshold is tunable).

**Why this works:** a small text change affects only the few features that changed. Their contributions to `V` are small compared with the many unchanged features, so only a few bits flip. A different page has mostly different features, so about half the bits flip (a distance near 32 of 64).

### 8.6 How to search for near-matches efficiently

Comparing a new fingerprint against **billions** of stored fingerprints with Hamming distance is far too slow. The standard trick uses the **pigeonhole principle**:

- Split the 64-bit fingerprint into **4 blocks of 16 bits**.
- If two fingerprints differ in **at most 3 bits**, then **at least one of the 4 blocks is identical** (3 differing bits can touch at most 3 blocks).
- Maintain **4 index tables**, one per block position (in practice, bit-permuted copies of the fingerprint). Each table maps a 16-bit block value to the fingerprints that contain it.
- For a new page: look up each of its 4 blocks. This yields a small **candidate set**. Compute the exact Hamming distance only for those candidates. Any candidate with distance ≤ 3 means a near-duplicate.

This turns "compare against everything" into "a few index lookups plus a handful of comparisons".

### 8.7 What "remove" actually means (the dedup decision)

After computing `content_hash` and `simhash`, the pipeline decides:

| Case | Detection | Action |
| --- | --- | --- |
| **Exact duplicate** | `content_hash` already in the hash index | Do **not** write a new blob. Record `url → canonical_doc_id` in the Metadata DB. Do **not** emit a "page fetched" event to the indexer. |
| **Near duplicate** | SimHash within threshold of an existing fingerprint | Link the URL to the existing **duplicate cluster**. Keep one **canonical** document in the index (see below). Skip or demote the others. |
| **Unique** | No match | Store the blob, save the `content_hash` and `simhash` in the indexes, emit the event to Kafka for indexing. |

**Choosing the canonical copy of a cluster** (heuristics, in a typical order):

1. The URL the page declares via `<link rel="canonical">`.
2. The URL with the higher importance or priority score.
3. The shorter, cleaner URL (for example, `https` over `http`, no parameters).
4. The earliest-crawled URL.

**What happens to the duplicate's links?** If a duplicate page's links are identical to the canonical page's, they are already known, so the URL Seen Filter rejects them. Nothing extra is enqueued. The crawler still records the duplicate URL's existence, so it is not refetched through the Seen Filter, and recrawl logic can treat the whole cluster as one logical page.

**Result:** storage, indexing and ranking work are spent once per piece of content instead of once per URL.

### 8.8 Alternatives and refinements

- **MinHash with LSH** estimates Jaccard similarity of shingle sets; it is better for set-overlap similarity and is another common choice for near-duplicate detection.
- **Block-level dedup** hashes paragraphs or chunks to detect partial overlaps (for example, a page that embeds a copied section).
- **Conditional GET** (`ETag`, `If-Modified-Since`) avoids downloading unchanged pages at all on recrawl, which is dedup against *our own past copy*.

### 8.9 Trade-offs and tuning

| Concern | Detail |
| --- | --- |
| **False merges** | A threshold that is too loose can merge genuinely different pages (for example, product pages that differ only in a model number). Tune the threshold on labeled samples and keep boilerplate removal good. |
| **Missed duplicates** | A threshold that is too strict lets near-copies through. |
| **Cost** | SimHash is cheap to compute, but the index of fingerprints must be sharded and kept in memory or fast storage at web scale. |
| **Where to run** | Compute fingerprints in the parse stage; keep the fingerprint index as a shared service or shard it by block value. |

---

## 9. Data model

Illustrative schemas (field names are examples).

**URL table** (key: `url_hash`)

| Field | Purpose |
| --- | --- |
| `url_hash` (PK) | Hash of the normalized URL |
| `url` | Normalized URL |
| `host` | For sharding and politeness |
| `status` | `queued`, `fetched`, `failed`, `duplicate`, `blocked` |
| `depth` | Distance from the seed |
| `priority` | Latest priority score |
| `last_crawl_time`, `next_crawl_time` | Scheduling |
| `etag`, `last_modified` | For conditional GET |
| `http_status`, `retry_count` | Outcome and retry tracking |
| `content_hash`, `simhash` | Dedup references |
| `canonical_doc_id` | Points to the canonical copy if this URL is a duplicate |
| `change_count`, `change_rate` | Learned update frequency |

**Host table** (key: `host`)

| Field | Purpose |
| --- | --- |
| `host` (PK) | Hostname |
| `ip` | For per-IP rate limiting |
| `robots_txt`, `robots_fetched_at` | Cached rules and their age |
| `crawl_delay` | From `robots.txt` or default |
| `avg_response_ms` | Input to the adaptive delay |
| `next_allowed_time` | Politeness timer |
| `pages_crawled`, `page_budget` | Per-host budget to avoid traps |

**Content/doc table** (key: `content_hash` or `doc_id`)

| Field | Purpose |
| --- | --- |
| `doc_id`, `content_hash` | Identity |
| `simhash` | Near-duplicate fingerprint |
| `blob_pointer` | Location of the raw and parsed content in the Blob store |
| `cluster_id` | Duplicate cluster membership |
| `fetched_at` | Timestamp |

---

## 10. Scaling and sharding

**Shard by host.** Assign each hostname to a crawler node using `hash(host) mod N` (preferably **consistent hashing**, so adding or removing nodes moves only a small share of hosts).

Each node owns its hosts' **Frontier slice** (front queues, back queues and heap) and its **fetchers**.

Why host-based sharding is the right key:

- **Politeness stays local.** All URLs for a host live on one node, so there is no cross-node coordination to enforce one-at-a-time and crawl-delay.
- **Caches stay effective.** DNS and `robots.txt` entries for a host live on the node that needs them.
- **Kafka partitions** keyed by host route URLs to the right node.

**Cross-shard traffic:** when a node extracts a link that belongs to a host owned by another node, it **forwards** the URL (through Kafka) to the owner. The owner then runs the Seen Filter check and enqueues it.

**Scaling each part:**

| Part | How it scales |
| --- | --- |
| Fetchers | Add nodes; async I/O per node |
| Frontier | More shards (host-hash partitions) |
| Bloom filter / seen-store | Shard by URL hash or host hash |
| Fingerprint index | Shard by block value (SimHash tables) |
| Blob store | Object storage scales horizontally |
| Metadata DB | Wide-column store, partition by `url_hash` or `host` |
| Indexer | Independent Kafka consumers |

**Multi-region.** Run crawler clusters in several regions and crawl hosts from the region closest to them for better latency. Dedup indexes and the Frontier assignment must then be partitioned consistently across regions.

---

## 11. Failure handling, traps and robustness

### 11.1 Failures

| Failure | Response |
| --- | --- |
| Timeout / connection error | Retry with **exponential backoff** (for example 1 min, 5 min, 30 min); after N attempts, send to the **DLQ** |
| `5xx` response | Treat as temporary; back off and retry |
| `429` / `503` with `Retry-After` | Honor the header; raise the host's delay |
| `404` / `410` | Mark gone; do not retry (maybe a rare re-check) |
| `robots.txt` unreachable | Be conservative; delay host and retry |
| Fetcher crashes mid-fetch | The URL stays leased in the Frontier; after a lease timeout it is re-queued (fetches are **idempotent**) |
| Frontier node crashes | Rebuild from persisted queues (Kafka offsets, Redis snapshot or AOF) |
| Parser crash on bad HTML | Per-page time and memory limits; mark the URL failed and move on |
| Blob store write failure | Retry; do not mark the URL "done" until the write succeeds |

### 11.2 Crawler traps

A **crawler trap** is a site structure that generates endless unique-looking URLs and can eat the crawler's budget:

- Infinite calendars (`/calendar?month=2031`, `2032`, ...).
- Session IDs appended to every link.
- Repeating path segments (`/a/b/a/b/a/b/...`).
- Dynamically generated pages with infinite pagination or filters.

**Defenses:**

| Defense | Detail |
| --- | --- |
| **Maximum depth** | Do not follow links beyond depth D |
| **URL length limit** | Drop URLs longer than N characters |
| **Per-host page budget** | Cap pages crawled per host per period |
| **Repeating-segment detection** | Drop paths where a pattern repeats many times |
| **Parameter limits** | Cap the number of query parameters and values per path |
| **Content dedup** | Many trap pages end up with the same or near-same content |
| **Monitoring** | Alert when one host suddenly generates a huge share of new URLs |

### 11.3 JavaScript-heavy pages

Plain HTTP fetch sees only the initial HTML, so single-page apps may appear empty. The option is a **headless-browser render pool** that executes JavaScript. It is **10 to 100 times more expensive**, so use it selectively (for example, only for important hosts or pages detected as JS-driven).

### 11.4 Observability

Track and alert on:

- Pages crawled per second, per host and overall.
- Frontier size and age of the oldest URL per priority.
- Fetch success, error, timeout and `429` rates.
- Dedup rates (URL filter hits, exact and near duplicates).
- Per-host crawl rate versus its politeness limit.
- Kafka consumer lag for the indexer.
- Bloom filter fill ratio (an estimate of its current false-positive rate).

---

## 12. Freshness and recrawling

The web changes, so a crawl is never "done". But recrawling everything constantly is wasteful, so recrawl **adaptively**:

1. **Learn change rate per URL.** On each recrawl, compare the new `content_hash` with the stored one. Update `change_rate` (for example, an exponentially weighted estimate of changes per day).
2. **Schedule by importance and change rate.** A news homepage that changes hourly is recrawled hourly; a static "About" page monthly or less.
3. **Use conditional GET.** Send `If-None-Match` / `If-Modified-Since`. A `304 Not Modified` response costs almost no bandwidth, and we just update `last_crawl_time`.
4. **Back off for unchanged pages.** If a page has not changed across several recrawls, lengthen its interval.
5. **Feed the Frontier.** The Metadata DB's `next_crawl_time` produces recrawl URLs, which enter the Frontier through the Prioritizer like any other URL. This is the dashed "recrawl schedule" arrow in the diagram.

Sitemaps (`<lastmod>`) and push signals (feeds, pings) can trigger earlier recrawls.

---

## 13. Trade-offs and design decisions

| Decision | Choice | Why | Cost |
| --- | --- | --- | --- |
| Crawl order | **Priority** queues, not plain BFS | Spend limited bandwidth on valuable, fresh pages first | More complexity; needs good scoring signals |
| Politeness | **One back queue per host + timing heap** | Strict per-host ordering with high total parallelism | More queues and bookkeeping |
| Priority selection | **Biased random** over front queues | Avoids starving low priority | Slightly delays top-priority pages |
| URL dedup | **Bloom filter** | 1B URLs in about 1.2 GB; no false negatives | A few new URLs wrongly skipped; no deletes |
| Content dedup | **SHA-256 + SimHash** | Catches exact and near copies cheaply | Threshold tuning; fingerprint index to maintain |
| Sharding | **By host** | Politeness and caches stay local | Hot hosts can create imbalance; cross-shard link forwarding |
| Fetch model | **Async I/O** | Many concurrent slow connections per worker | More complex than thread-per-request |
| Frontier storage | **Persisted queues** (Kafka / Redis / disk) | No lost backlog on crash | Added latency and ops cost |
| Raw storage | **Keep raw HTML in blob store** | Reprocess without recrawl | Storage cost (mitigated by compression and dedup) |
| Decoupling | **Kafka to indexer** | Slow downstream does not slow crawling | Eventual consistency between crawl and index |
| JS rendering | **Optional, selective** | Covers JS-driven sites | 10 to 100× cost per page |

---

## 14. Interview-ready summary

> "I would design the crawler as a distributed loop around a **URL Frontier**. Seeds and sitemaps enter the Frontier. The Frontier has **front queues** for **priority**, scored by importance, freshness need and depth, and **back queues**, one per host, with a **min-heap on next-allowed time** for **politeness**, so each host is hit one request at a time while many hosts are crawled in parallel.
>
> **Async fetchers** use cached DNS and cached `robots.txt`, honor crawl-delay with an adaptive delay, and use conditional GET on recrawls. The **parser** extracts text, metadata and links, and normalizes URLs. New links pass a **Bloom-filter URL seen check** (about 1.2 GB for 1B URLs at 1% false positives) before re-entering the Frontier. Fetched content goes through **content dedup**: **SHA-256** for exact copies and a **64-bit SimHash** with Hamming distance ≤ 3 for near-duplicates, indexed with block tables for fast lookup. Duplicates are linked to a canonical document and are not stored or indexed again.
>
> Unique pages go to a **blob store**, state goes to a **metadata DB**, and a **Kafka** event feeds the indexer. We **shard by host** so politeness and caches stay local, handle failures with **backoff and a DLQ**, defend against **crawler traps** with depth, length and per-host limits, and **recrawl adaptively** based on each page's learned change rate. The main trade-offs are Bloom-filter false positives versus memory, SimHash threshold tuning versus dedup accuracy, and priority crawling versus implementation complexity."

### Concepts to memorize

URL Frontier, front queues (priority), back queues (politeness), min-heap on next-allowed time, `robots.txt` and crawl-delay, adaptive delay, URL normalization, Bloom filter (`m = −n·ln p / (ln 2)²`, `k = (m/n)·ln 2`), SHA-256 exact dedup, SimHash and Hamming distance, pigeonhole block lookup, canonical document, host-based sharding, conditional GET, crawler traps, DLQ, adaptive recrawl.

*All numbers are illustrative design assumptions.*
