Yes. **Capacity estimation** is one of the most important parts of system design because it converts vague requirements into numbers that drive your architecture.

The goal is not mathematical perfection. In an interview, you want to show:

> **Users → traffic → storage → bandwidth → infrastructure implications**

## 1. The basic framework

I recommend memorizing this flow:

```text
1. Users
   ↓
2. DAU / requests
   ↓
3. Average & Peak QPS
   ↓
4. Storage
   ↓
5. Bandwidth
   ↓
6. Scaling implications
```

---

# 2. Start with users

Suppose the interviewer asks:

> Design Twitter/X.

You might assume:

```text
100M registered users
20M DAU
```

But **registered users don't directly tell you traffic**.

DAU is more useful.

---

# 3. Calculate requests per day

Suppose:

```text
20M DAU

Each user:
- opens app 10 times/day
- each session generates ~5 API requests
```

Then:

```text
20M × 10 × 5

= 1 billion requests/day
```

Now convert that to QPS.

---

# 4. QPS / RPS

QPS = Queries Per Second.

Formula:

```text
Average QPS =
Requests per day / 86,400
```

So:

```text
1,000,000,000 / 86,400

≈ 11,574 QPS
```

You can round:

> **~12K average QPS**

---

# 5. Peak traffic

You cannot design only for average traffic.

Suppose we assume peak traffic is **5× average**:

```text
12K × 5

= 60K QPS
```

So:

```text
Average QPS ≈ 12K
Peak QPS    ≈ 60K
```

Now you have an actual scalability requirement.

---

# 6. Read vs Write traffic

This is extremely important.

Suppose:

```text
90% reads
10% writes
```

At 60K peak QPS:

```text
Reads:
60K × 90%
= 54K QPS

Writes:
60K × 10%
= 6K QPS
```

Now you can start thinking about architecture.

For example:

```text
             60K QPS
                |
        +-------+-------+
        |               |
      Reads           Writes
      54K              6K
        |               |
      Cache             DB
        |
      Read replicas
```

---

# 7. Storage estimation

Now suppose users create **100M posts/day**.

Each post contains approximately:

```text
Post data        = 500 bytes
Metadata         = 500 bytes
Indexes          = 500 bytes
--------------------------------
Total            ≈ 1.5 KB
```

Then:

```text
100M × 1.5 KB

= 150 GB/day
```

For one year:

```text
150 GB × 365

≈ 54.75 TB/year
```

If you retain data for 5 years:

```text
54.75 × 5

≈ 274 TB
```

Then consider replication.

If replication factor = 3:

```text
274 TB × 3
≈ 822 TB
```

Now you've got a meaningful storage requirement.

---

# 8. Don't forget indexes

This is where many candidates make mistakes.

If your actual data is:

```text
100 TB
```

your database may require considerably more because of:

```text
Primary data
+ indexes
+ metadata
+ replication
+ compaction overhead
+ backups
```

For a rough interview estimate, you can explicitly say:

> "I'll add approximately 30–50% overhead for indexes and metadata."

The exact percentage depends on the database and access patterns.

---

# 9. Bandwidth

Suppose each request returns:

```text
20 KB
```

and peak traffic is:

```text
60K QPS
```

Then:

```text
60,000 × 20 KB

= 1,200,000 KB/sec
≈ 1.2 GB/sec
```

So your service needs roughly:

```text
~1.2 GB/s outbound
```

Or in bits:

```text
1.2 GB/s × 8
≈ 9.6 Gbps
```

That's significant.

This might lead you toward:

```text
CDN
caching
compression
pagination
smaller payloads
```

---

# 10. The magic conversions you should memorize

For interviews:

```text
1 day = 86,400 seconds

1M/day
≈ 11.6 requests/sec

10M/day
≈ 116 requests/sec

100M/day
≈ 1,157 requests/sec

1B/day
≈ 11,574 requests/sec
```

A very useful shortcut:

> **1 million requests/day ≈ 12 QPS**

Then apply your peak multiplier.

For example:

```text
100M requests/day

≈ 1,200 average QPS

5× peak

≈ 6,000 peak QPS
```

---

# 11. A complete example

Let's say the interviewer asks:

> Design an Instagram-like system.

You establish:

```text
100M DAU

Each user:
10 feed requests/day

100M × 10
= 1B feed requests/day
```

Average:

```text
1B / 86,400
≈ 11.6K QPS
```

Peak:

```text
11.6K × 5
≈ 58K QPS
```

Suppose 95% are reads:

```text
Read:
58K × 95%
≈ 55K QPS

Write:
58K × 5%
≈ 3K QPS
```

Now posts:

```text
10M new posts/day
```

Average write rate:

```text
10M / 86,400
≈ 116 posts/sec
```

Peak:

```text
116 × 5
≈ 580 posts/sec
```

Suppose each post's metadata is 2 KB:

```text
10M × 2 KB
= 20 GB/day
```

Yearly:

```text
20 × 365
≈ 7.3 TB/year
```

But **images/videos dominate storage**, so you would separately estimate media storage rather than treating everything as 2 KB.

That distinction is very important in real system design.

---

# 12. Capacity estimation is NOT just calculations

This is the key thing I want you to understand.

You calculate numbers **to make architectural decisions**.

For example:

### You calculate:

```text
Peak = 100K QPS
```

Then ask:

> Can one server handle 1K QPS?

If yes:

```text
100K / 1K

≈ 100 servers
```

Add headroom:

```text
~120–130 servers
```

Now you've justified horizontal scaling.

---

### You calculate:

```text
Storage = 500 TB
```

Then you realize:

> A single database machine isn't appropriate.

So you start considering:

```text
Sharding
Replication
Object storage
Partitioning
```

---

### You calculate:

```text
Bandwidth = 20 Gbps
```

Now:

> Serving all media directly from application servers is probably inefficient.

So:

```text
Application
     ↓
Object Storage
     ↓
CDN
     ↓
Users
```

That's why capacity estimation matters.

---

# 13. What to actually say in an interview

Don't spend 10 minutes doing complicated mathematics.

Say something like:

> **"I'll make a few assumptions first. Let's assume 20M DAU, each generating around 50 requests per day. That gives us roughly 1B requests per day, or about 12K average QPS. Assuming a 5× peak factor, we should design for roughly 60K QPS. I'll then split this into read and write traffic and estimate storage and bandwidth so that we can determine whether we need caching, sharding, replication, or CDN."**

That sounds much more senior than throwing random numbers around.

---

## The cheat sheet

Memorize this:

```text
DAU
 ↓
Actions/user/day
 ↓
Requests/day
 ↓
÷ 86,400
 ↓
Average QPS
 ↓
× Peak factor
 ↓
Peak QPS
 ↓
Read / Write split
 ↓
Storage
 ↓
Bandwidth
 ↓
Servers / DB / Cache / Shards / CDN
```

### And remember these 5 formulas:

```text
Requests/day
= Users × requests/user/day

Average QPS
= Requests/day ÷ 86,400

Peak QPS
= Average QPS × peak factor

Storage/day
= writes/day × average record size

Bandwidth
= QPS × average response size
```

**Next, the most useful thing to learn is how to make the assumptions quickly**—DAU, requests/user/day, peak multiplier, read/write ratio, record size, and retention—because that's where interviewers usually push back: *"Why did you assume 5×?"*, *"Why 20 requests/user?"*, etc.


Storage

You're right. The earlier storage calculation mixed **metadata storage** and **media storage**, which makes it confusing.

For interviews, let's make storage estimation much more systematic.

## 1. First ask: What are we storing?

For something like Instagram/Twitter, there are **two very different types of storage**:

```text
1. Metadata
   → user ID
   → post ID
   → caption
   → timestamps
   → likes
   → comments metadata
   → relationships

2. Media
   → images
   → videos
   → reels
```

**Media will usually dominate storage.**

---

# 2. Calculate metadata storage

Let's keep our memorized assumption:

```text
50M new posts/day
```

Suppose each post's metadata is approximately:

```text
2 KB/post
```

Then:

```text
50M × 2 KB
= 100 GB/day
```

Yearly:

```text
100 GB × 365
≈ 36.5 TB/year
```

So:

> **Metadata ≈ 100 GB/day ≈ 36.5 TB/year**

That's the database storage before considering replication, indexes, backups, etc.

---

# 3. Now calculate media storage

This is where we need a different assumption.

Suppose each post contains an image/video and the **average compressed media size is 500 KB**.

Then:

```text
50M posts/day × 500 KB
= 25,000,000,000 KB
```

Approximately:

```text
= 25 TB/day
```

That's already huge.

Yearly:

```text
25 TB × 365
≈ 9.1 PB/year
```

So you can see why saying **"5–10 TB/day media"** without explaining the assumed media size was misleading.

---

# 4. But not every post has media

Let's make it more realistic.

Suppose:

```text
50M posts/day

60% contain media
40% text-only
```

Therefore:

```text
50M × 60%
= 30M media posts/day
```

Average media size:

```text
500 KB
```

Then:

```text
30M × 500 KB
≈ 15 TB/day
```

Yearly:

```text
15 TB × 365
≈ 5.5 PB/year
```

Now the number makes sense.

---

# 5. Then replication

Suppose your object storage has **3 copies** for durability:

```text
15 TB/day × 3
= 45 TB/day physical storage
```

Yearly:

```text
5.5 PB × 3
≈ 16.5 PB
```

But in a real cloud architecture, you might use erasure coding/object-storage durability rather than simply storing exactly three full copies, so **don't automatically multiply by 3 unless you explicitly assume replication factor 3**.

---

# 6. The important interview distinction

Think of storage as:

```text
                 STORAGE
                    |
          +---------+---------+
          |                   |
       Metadata             Media
          |                   |
       ~2 KB/post          ~500 KB/media
          |                   |
       Database          Object Storage
          |                   |
      Sharding          Replication/
      Indexes            durability
```

And this leads directly to your architecture:

```text
Post request
     |
     +---- Metadata → DB
     |
     +---- Image/video → Object Storage
                              |
                              ↓
                             CDN
                              |
                              ↓
                            Users
```

---

# 7. What should YOU memorize?

Don't memorize **100 GB/day + 5 TB/day** as arbitrary numbers.

Memorize the **calculation pattern**:

```text
New content/day
×
Average metadata size
=
Metadata storage/day

New media/day
×
Average media size
=
Media storage/day
```

For our generic example:

```text
50M posts/day
× 2 KB metadata
= 100 GB/day metadata

30M media/day
× 500 KB media
= 15 TB/day media
```

So your interview cheat sheet becomes:

```text
100M DAU
↓
20 actions/user/day
↓
2B requests/day
↓
24K average QPS
↓
120K peak QPS
↓
90% read / 10% write
↓
50M new posts/day
↓
100 GB/day metadata
↓
~15 TB/day media
↓
DB + Object Storage + CDN
```

### One thing to be careful about

**Storage ≠ bandwidth.**

If you store **15 TB/day**, that doesn't mean you're transferring 15 TB/day.

Storage asks:

> **How much data do we need to retain?**

Bandwidth asks:

> **How much data are we serving per second?**

That's the next distinction I'd learn before memorizing the final capacity sheet.
