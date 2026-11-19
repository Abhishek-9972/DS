# System Design Interview — Data Modelling Handbook (v2)

*Entities, keys, indexes, invariants and failure modes for 31 classic designs. Whiteboard-first: each system lists its access patterns, the smallest correct model, the invariant that must never break, and what happens when things fail.*

## What is new in v2

- **No more placeholders.** v1 had 40+ rows saying 'choose indexes from the primary read/write paths'. Every table now has a concrete key, index or ownership rationale.
- **Access-pattern-first.** Each system opens with the 3–5 queries that drive the schema, then states its critical invariant and the exact mechanism that enforces it.
- **Corrected or deepened models** where v1 was weak: seat booking (one authority for seat state), job scheduler (run vs attempt, fencing), train booking (leg-based inventory), WhatsApp (sequencer + read pointers instead of per-message receipts for groups), calendar (recurrence, exceptions, free/busy), stock trading (buying-power holds, no reserved word `order`), Airbnb (night-claim vs exclusion constraint), auction (minimum increment, anti-sniping), payments (ledger transaction groups, webhook dedupe).
- **New reference sections:** conventions, a pattern catalog with copy-ready SQL, a storage-selection guide, a capacity cheat sheet, common anti-patterns, a 45-minute pacing guide and a pattern-to-system index.
- **Source note:** schemas are proposed interview models inferred from each system's well-known architecture. They are logical designs, not vendor migration scripts; adjust types and constraints to your database.

## How to use this in an interview

1. **State access patterns before drawing tables.** Reads and writes decide keys, indexes and partitioning.
2. **Name the source of truth** and separate it from caches, search indexes, event streams and analytics projections.
3. **Draw the smallest normalized transactional model first** (4–6 entities). Add denormalized read models only for a demonstrated query or scale need.
4. **For every table say three things:** the primary key, the uniqueness or invariant it protects, and the query it serves.
5. **State the critical invariant out loud** (one seat sold once, one idempotent payment result, one winner per auction) and name the mechanism: unique constraint, conditional update, exclusion constraint, single writer, or lease.
6. **For wide-column stores** give the partition key, clustering order and partition-bounding strategy. Never present a relational schema as a Cassandra schema.
7. **Close with failure handling:** retries, idempotency, outbox, reconciliation, retention, and how derived data is rebuilt.

---

# Part A — Conventions used throughout

| Topic | Convention | Why |
| --- | --- | --- |
| Identifiers | UUIDv7 / ULID / Snowflake (time-sortable, 64–128 bit). Avoid per-shard auto-increment | Sortable by creation time, no coordination, safe across shards |
| Money | `amount_minor BIGINT` (cents/paise) plus `currency CHAR(3)` | Floats lose cents; never mix currencies in one sum |
| Time | `TIMESTAMPTZ` in UTC. Keep IANA timezone separately when wall-clock meaning matters (calendar, schedules) | DST-safe; display is a presentation concern |
| Status | Enum column plus an explicit state machine. Transitions are conditional updates (`WHERE status = :expected`) | Makes retries and races safe |
| Optimistic locking | `version INT` incremented on every update; writer includes `WHERE version = :seen` | Lost-update prevention without long locks |
| Deletes | Soft delete (`deleted_at`) for user-visible data; tombstones in LSM stores; hard delete only for compliance (GDPR) | Auditing, undo, sync |
| Multi-tenancy | Put `tenant_id` first in keys and indexes | Locality, easy sharding, isolation |
| Notation | `PK` primary key, `FK` foreign key, `UQ` unique, `IDX` index, `((a,b), c)` Cassandra partition key `(a,b)` with clustering column `c` |  |

---

# Part B — Pattern catalog (reused by many systems)

**P1 — Idempotency gate.** Persist the key before doing work; a unique constraint arbitrates concurrent retries.

```sql
INSERT INTO idempotency(scope, idem_key, request_hash, status)
VALUES (:merchant_id, :key, :hash, 'IN_PROGRESS')
ON CONFLICT (scope, idem_key) DO NOTHING;
-- 0 rows inserted: load existing row.
--   same request_hash  -> return stored response (safe replay)
--   different hash     -> reject (HTTP 422): key reuse with another payload
```

**P2 — Conditional update (compare-and-set).** The invariant lives in the WHERE clause; rowcount tells you whether you won.

```sql
UPDATE inventory
   SET reserved = reserved + :q, version = version + 1
 WHERE variant_id = :v AND warehouse_id = :w
   AND on_hand - reserved >= :q;      -- rowcount 0 => out of stock
```

**P3 — Transactional outbox.** Business state and the event commit atomically; a relay (poller or CDC such as Debezium) publishes later; consumers dedupe on `event_id`.

```sql
BEGIN;
  INSERT INTO customer_order(...) VALUES (...);
  INSERT INTO outbox(event_id, aggregate_id, event_type, payload) VALUES (...);
COMMIT;   -- relay publishes, then sets published_at
```

**P4 — Lease-based claiming.** Workers claim rows with a time-bound lease; expired leases are re-claimable. `SKIP LOCKED` avoids contention.

```sql
UPDATE job_run
   SET status = 'RUNNING', worker_id = :w, attempt_no = attempt_no + 1,
       lease_until = now() + interval '30 seconds'
 WHERE run_id IN (SELECT run_id FROM job_run
                   WHERE status = 'READY' AND run_at <= now()
                   ORDER BY run_at LIMIT 100 FOR UPDATE SKIP LOCKED)
RETURNING *;
```

**P5 — Append-only double-entry ledger.** Never update or delete entries; corrections are new entries. Balances are projections.

```sql
-- signed amounts: debit < 0, credit > 0; each txn_id must sum to zero
CREATE TABLE ledger_entry (
  entry_id BIGINT PRIMARY KEY, txn_id UUID NOT NULL, account_id BIGINT NOT NULL,
  amount_minor BIGINT NOT NULL, currency CHAR(3) NOT NULL, created_at TIMESTAMPTZ NOT NULL);
CREATE INDEX ON ledger_entry (account_id, created_at);
-- invariant check: SELECT txn_id FROM ledger_entry GROUP BY txn_id HAVING SUM(amount_minor) <> 0;
```

**P6 — Bucketed wide-column partition.** One table per query; bound partition size with a bucket.

```sql
CREATE TABLE message (
  conversation_id UUID, bucket INT, seq BIGINT, sender_id UUID, body BLOB,
  PRIMARY KEY ((conversation_id, bucket), seq)
) WITH CLUSTERING ORDER BY (seq DESC);
-- bucket = seq / 10000 (deterministic from a cursor) or day number (time-based)
```

**P7 — Temporary hold, durable confirm.** A hold is a row (or Redis key) with an expiry; confirmation is a DB transaction that re-checks the hold is still valid. Expiry must serialize against confirmation, and a payment that succeeds after expiry needs a compensation path (refund or re-acquire).

**P8 — Range exclusion constraint** (Postgres). The database refuses overlapping intervals.

```sql
CREATE EXTENSION btree_gist;
ALTER TABLE booking ADD CONSTRAINT no_overlap
  EXCLUDE USING gist (listing_id WITH =, daterange(check_in, check_out) WITH &&)
  WHERE (status IN ('PENDING','CONFIRMED'));
```

**P9 — State-machine guard.** Only legal transitions succeed; replays are no-ops.

```sql
UPDATE trade_order SET status = 'FILLED', version = version + 1
 WHERE order_id = :o AND status IN ('NEW','PARTIALLY_FILLED');
```

**P10 — Rebuildable projection.** Caches, timelines, search indexes, counters and aggregates are derived from a durable source (tables or an event log). Say how you rebuild them (replay, backfill, dual-write plus verify) and how you detect drift (periodic reconciliation).

---

# Part C — Choosing storage

| Need | Typical choice | Why / watch-out |
| --- | --- | --- |
| Transactions, constraints, joins, moderate scale | PostgreSQL / MySQL | Unique and exclusion constraints enforce invariants; shard by tenant or entity when needed |
| Very high write volume, known access patterns, time-ordered history | Cassandra / DynamoDB | Model per query, bucket partitions, no joins, careful with counters and read-before-write |
| Low-latency lookups, counters, locks, leaderboards, geo | Redis | Define TTL, eviction, persistence and behaviour when unavailable; rarely the system of record |
| Full-text, geo-filter, faceting, autocomplete | Elasticsearch / OpenSearch | Derived index; enforce authorization after retrieval |
| Large immutable blobs (media, snapshots, fetched pages) | Object storage (S3-like) plus CDN | Store key, checksum and size in the metadata DB |
| Event ingestion, fan-out, decoupling | Kafka | Partition key defines ordering scope; consumers must tolerate duplicates |
| Large-scale analytics and rollups | ClickHouse / warehouse | Append-only, partition by date; never serve OLTP |
| Graph traversal (routing, social) | Adjacency tables, graph store, or custom in-memory graph | Often precomputed offline and versioned |

---

# Part D — Capacity cheat sheet (rules of thumb)

| Quantity | Rule of thumb |
| --- | --- |
| Seconds per day | 86,400 ≈ 10^5 |
| 1M events/day | ≈ 12 per second on average; peak ≈ 3–10× average |
| 100M events/day | ≈ 1,200 per second average |
| Single PostgreSQL primary | thousands of simple writes/s, tens of thousands of indexed reads/s (hardware dependent) |
| Single Redis node | \~100k simple ops/s; memory is the limit |
| Cassandra partition | keep under \~100 MB and \~100k rows; bucket to stay below |
| Kafka partition | order guaranteed per partition; plan partitions for parallelism, not just throughput |
| Row size | 200–500 B for typical metadata rows; 1 KB × 1B rows = 1 TB before indexes and replication |
| Base62 code space | 62^6 ≈ 57 billion; 62^7 ≈ 3.5 trillion |

Always say the assumption out loud ('assume 100M DAU, 10 reads per write') and derive QPS and storage from it.

---

# Part E — The 31 designs

## 01. URL Shortener

**Storage:** PostgreSQL (or a KV store) for durable mappings; Redis for hot codes; Kafka → ClickHouse for click analytics.

**Access patterns:** (1) redirect by code, read-heavy (\~100:1); (2) create a short URL, optionally with a custom alias; (3) list a user's links; (4) clicks per link per day.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `url_mapping` | code, long\_url, owner\_id, created\_at, expires\_at, status (ACTIVE/DISABLED/EXPIRED), is\_custom | PK(code). IDX(owner\_id, created\_at DESC). Optional UQ(owner\_id, hash(long\_url)) if you dedupe per owner |
| `code_range` | range\_id, next\_value, max\_value, owner\_node | Hands each app node a block of counter values (alternative to random codes) |
| `click_event` | event\_id, code, clicked\_at, referrer, country, device\_type | Not in the OLTP DB: Kafka → ClickHouse partitioned by date |
| `url_daily_stats` | code, stat\_date, clicks, unique\_visitors | PK(code, stat\_date); built asynchronously from click events |

**Invariants and mechanisms**

- *Code is unique:* either random 7-char base62 with `INSERT ... ON CONFLICT` retry, or counter → base62 using pre-allocated ranges (no coordination per write). Custom aliases rely on the same PK.
- *Expired links never redirect:* check `expires_at` on read; a sweeper only reclaims space.
- *Create is retry-safe:* client idempotency key or deterministic dedupe.

**Failure and scale:** Redirect path = Redis `GET url:{code}`, fall back to DB, populate cache with TTL; cache negative results briefly to absorb scans. 302 keeps analytics and expiry accurate; 301 is cacheable by browsers but hides clicks. Shard by `hash(code)`. Hot code: local in-process cache plus CDN.

**Follow-ups:** guessability and abuse (rate limit, malware scan), reuse of expired codes, multi-region reads.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/urls` | Create a short URL. Body: long\_url, optional custom\_alias, expires\_at. Idempotency-Key header. Returns code and short\_url; 409 if the alias is taken |
| `GET /{code}` | Public redirect (302). Redis first, DB on miss. 404 unknown, 410 expired or disabled |
| `GET /v1/urls/{code}` | Metadata for the owner (long\_url, status, expiry, created\_at) |
| `PATCH /v1/urls/{code}` | Change expiry or status (enable / disable) |
| `DELETE /v1/urls/{code}` | Disable the link and evict it from cache |
| `GET /v1/urls?cursor=&limit=` | List the caller's links, newest first (cursor pagination) |
| `GET /v1/urls/{code}/stats?from=&to=&granularity=` | Clicks, unique visitors, top referrers and countries from `url_daily_stats` / ClickHouse |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `url_mapping` | Source of truth for code → long URL, ownership, expiry and status; enforces code uniqueness; serves the redirect on a cache miss |
| `code_range` | Hands each app node a disjoint block of counter values so codes are unique without per-write coordination |
| `click_event` | Immutable raw click log; input to all analytics; never read on the redirect path |
| `url_daily_stats` | Pre-aggregated per-day counts that answer the stats API cheaply |

---

## 02. Seat / Ticket Booking

**Storage:** PostgreSQL is the single authority for seat state and bookings; Redis (optional) for waiting room and hot-show throttling; outbox → Kafka for notifications.

**Access patterns:** (1) seat map for a show; (2) hold N seats atomically; (3) confirm after payment; (4) release expired holds; (5) a user's bookings.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `event` | event\_id, title, category, status | IDX(category, status) |
| `show` | show\_id, event\_id, venue\_id, starts\_at, status | IDX(event\_id, starts\_at); IDX(starts\_at, status) for discovery |
| `venue_seat` | seat\_id, venue\_id, section, row\_no, seat\_no | UQ(venue\_id, section, row\_no, seat\_no) — static layout |
| `show_seat` | show\_id, seat\_id, price\_tier, status (AVAILABLE/HELD/SOLD), hold\_id, hold\_expires\_at, booking\_id, version | **PK(show\_id, seat\_id)** — the single authority for seat state |
| `reservation` | reservation\_id, user\_id, show\_id, status (PENDING/CONFIRMED/EXPIRED/CANCELLED), expires\_at, idempotency\_key | UQ(user\_id, idempotency\_key); IDX(user\_id, created\_at DESC); IDX(status, expires\_at) for the sweeper |
| `reservation_seat` | reservation\_id, seat\_id, show\_id, price\_minor | PK(reservation\_id, seat\_id) — history link; the truth is `show_seat` |
| `payment` | payment\_id, reservation\_id, amount\_minor, currency, status, provider\_ref, idempotency\_key | UQ(provider\_ref) WHERE NOT NULL; IDX(reservation\_id) |
| `outbox` | event\_id, aggregate\_id, event\_type, payload, created\_at, published\_at | P3 |

**Invariant: a seat is sold at most once.** v1 relied on `UNIQUE(show_id, seat_id)` in the link table, which breaks once reservations expire or cancel. Instead make `show_seat` the only owner and use a multi-row conditional update (P2/P7):

```sql
UPDATE show_seat
   SET status = 'HELD', hold_id = :h, hold_expires_at = now() + interval '5 minutes'
 WHERE show_id = :s AND seat_id = ANY(:seat_ids)   -- sort ids to avoid deadlocks
   AND (status = 'AVAILABLE' OR (status = 'HELD' AND hold_expires_at < now()));
-- rowcount must equal cardinality(:seat_ids), otherwise ROLLBACK (all-or-nothing)
```

Confirm with `WHERE hold_id = :h AND status = 'HELD' AND hold_expires_at > now()` inside the booking transaction.

**Failure and scale:** Extend the hold when payment starts, so a slow payment is not cut off mid-flight. If payment succeeds after expiry and the seats were taken, refund and notify (compensation). Expired holds need no sweeper for correctness (the WHERE clause already treats them as free); the sweeper only cleans reservations. For blockbuster on-sales put a Redis waiting room in front and shard hot shows by `show_id`.

**Follow-ups:** best-available seat selection, group bookings, dynamic pricing, overselling policy.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/events?city=&date=&q=` | Discover events; served from search or cache |
| `GET /v1/events/{id}/shows` | Shows (date, venue, time) for an event |
| `GET /v1/shows/{showId}/seats` | Seat map with status and price tier; slightly stale is acceptable |
| `POST /v1/shows/{showId}/holds` | Hold N seats atomically. Body: seat\_ids\[\]. Idempotency-Key. Returns reservation\_id and expires\_at; 409 if any seat is unavailable (all-or-nothing) |
| `DELETE /v1/reservations/{id}` | Release a hold early |
| `POST /v1/reservations/{id}/payments` | Start payment; extends the hold; returns a provider payment intent |
| `POST /v1/payments/webhook` | Provider callback; verifies signature, deduplicates, confirms the booking |
| `GET /v1/bookings` and `GET /v1/bookings/{id}` | Booking history and ticket details |
| `POST /v1/bookings/{id}/cancel` | Cancel and refund per policy; frees seats |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `event` | Describes what is being sold; powers discovery |
| `show` | One performance of an event at a venue and time; the unit of inventory |
| `venue_seat` | Static physical seat layout; unique position per venue |
| `show_seat` | **Single authority for seat state** (available, held, sold) with hold expiry; the conditional update here prevents double booking |
| `reservation` | The user's in-flight or finished purchase; carries expiry and the idempotency key |
| `reservation_seat` | Links a reservation to its seats and the price paid for each (history) |
| `payment` | Payment attempt and outcome per reservation; unique provider reference |
| `outbox` | Reliable event feed for tickets, emails and analytics |

---

## 03. Amazon-like E-commerce

**Storage:** PostgreSQL/MySQL for orders, payments, inventory; search index for catalog; Redis (or DB) for carts; object storage for media.

**Access patterns:** (1) search and browse products; (2) product page; (3) cart add/update; (4) checkout: reserve stock, pay, create order; (5) order history and tracking.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `customer` | customer\_id, name, email, created\_at | UQ(email) |
| `product` | product\_id, title, description, brand\_id, category\_id, status | IDX(category\_id, status) |
| `product_variant` | variant\_id, product\_id, sku, attributes\_json | UQ(sku); IDX(product\_id) |
| `price` | variant\_id, currency, amount\_minor, valid\_from, valid\_to | PK(variant\_id, currency, valid\_from) — price history |
| `inventory` | variant\_id, warehouse\_id, on\_hand, reserved, version | PK(variant\_id, warehouse\_id); CHECK(reserved ≤ on\_hand) |
| `inventory_reservation` | reservation\_id, order\_id, variant\_id, warehouse\_id, qty, status, expires\_at | IDX(status, expires\_at); released on payment failure or timeout |
| `cart` / `cart_item` | cart\_id, customer\_id, status / cart\_id, variant\_id, qty | PK(cart\_id, variant\_id); IDX(customer\_id, status). Often Redis hash with TTL |
| `customer_order` | order\_id, customer\_id, status, total\_minor, currency, shipping\_address\_json, created\_at | IDX(customer\_id, created\_at DESC) |
| `order_item` | order\_item\_id, order\_id, variant\_id, qty, unit\_price\_minor, title\_snapshot | **Snapshot price and title at purchase time** |
| `payment` | payment\_id, order\_id, amount\_minor, status, provider\_ref, idempotency\_key | UQ(idempotency\_key) |
| `shipment` / `shipment_item` | shipment\_id, order\_id, carrier, tracking\_no, status / shipment\_id, order\_item\_id, qty | IDX(order\_id); supports split shipments |
| `outbox` | event\_id, aggregate\_id, event\_type, payload, published\_at | P3 |

**Invariants:** no oversell (P2: `on_hand - reserved >= :q`); order totals are derived from snapshots, never recomputed from current prices; order status moves only along the state machine CREATED → PAYMENT\_PENDING → PAID → FULFILLING → SHIPPED → DELIVERED, with CANCELLED / REFUNDED exits (P9).

**Failure and scale:** Checkout is a saga: reserve stock → create order and payment intent → confirm payment → commit stock; release reservations on failure or timeout. Product pages and search are eventually consistent with stock; the check at checkout is authoritative. Shard orders by `customer_id`; shard inventory by `variant_id` (hot SKUs: see Flash Sale, #31).

**Follow-ups:** multi-warehouse allocation, returns, marketplace sellers, tax and shipping calculation.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/products?q=&category=&sort=` | Search and browse via the search index |
| `GET /v1/products/{id}` | Product page: variants, price, availability badge (eventually consistent) |
| `GET /v1/cart`, `PUT /v1/cart/items/{variantId}`, `DELETE /v1/cart/items/{variantId}` | Read and modify the cart |
| `POST /v1/checkout` | Validate cart, reserve stock, create order and payment intent. Idempotency-Key. Returns order\_id and payment details |
| `POST /v1/orders/{id}/payments` | Confirm or retry payment |
| `GET /v1/orders`, `GET /v1/orders/{id}` | Order history and detail with item price snapshots |
| `POST /v1/orders/{id}/cancel` | Cancel before shipment; releases reservations and refunds |
| `POST /v1/orders/{id}/returns` | Start a return or refund request |
| `GET /v1/shipments/{id}` | Tracking status |
| `PUT /internal/inventory/{variantId}` | Warehouse system adjusts on\_hand |
| `POST /v1/payments/webhook` | Provider callback; deduplicated |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `customer` | Identity and contact of the buyer |
| `product` / `product_variant` | Catalog entries; variant carries the SKU and attributes (size, color) |
| `price` | Price history per variant and currency; current price lookup |
| `inventory` | Stock per variant and warehouse; conditional updates prevent oversell |
| `inventory_reservation` | Temporary stock claims for in-flight orders; released on failure or timeout |
| `cart` / `cart_item` | Pre-purchase basket; short-lived and rewritable |
| `customer_order` | The commercial record: status, totals, address snapshot |
| `order_item` | Purchased lines with price and title snapshot so history never changes |
| `payment` | Charge attempts and results; idempotent |
| `shipment` / `shipment_item` | Fulfilment units and tracking; supports split shipments |
| `outbox` | Reliable events to fulfilment, email and search |

---

## 04. Twitter / X-like Feed

**Storage:** Relational store (or sharded MySQL) for users, tweets and follows; Redis for hot timelines; Cassandra for large timeline or activity history; Kafka for fan-out.

**Access patterns:** (1) home timeline, newest first; (2) a user's profile timeline; (3) follow and unfollow; (4) like, retweet, reply; (5) tweet detail with counts.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `user_account` | user\_id, handle, display\_name, follower\_count, created\_at | UQ(handle) |
| `follow` | follower\_id, followee\_id, created\_at | PK(follower\_id, followee\_id); reverse IDX(followee\_id, follower\_id). In Cassandra keep two tables: `following_by_user` and `followers_by_user` |
| `tweet` | tweet\_id (Snowflake), author\_id, text, media\_refs, reply\_to\_id, quote\_of\_id, visibility, deleted\_at | IDX(author\_id, tweet\_id DESC). Snowflake IDs sort by time, so no separate time column is needed for ordering |
| `timeline_entry` | user\_id, tweet\_id, author\_id | Redis ZSET `timeline:{user_id}` (score = tweet\_id, capped \~800) or Cassandra PK((user\_id), tweet\_id DESC) with a size cap/TTL |
| `tweet_like` | user\_id, tweet\_id, created\_at | PK(user\_id, tweet\_id); IDX(tweet\_id) |
| `tweet_counters` | tweet\_id, likes, retweets, replies | Derived. Redis or sharded counters with periodic flush; exact numbers are eventually consistent |

**Invariants:** the tweet table is canonical; timelines are projections (P10) holding IDs only, hydrated from the tweet cache at read time. A like is unique per `(user_id, tweet_id)`.

**Fan-out strategy:** push (fan-out on write) for normal accounts; for celebrities (e.g. above \~100k followers) skip the push and merge their recent tweets at read time (hybrid). Blocks, mutes and deletes are applied at read/hydration time because they change after fan-out.

**Failure and scale:** Fan-out workers consume Kafka keyed by `author_id` and are idempotent (ZADD is naturally idempotent). Lost timelines are rebuilt from follows plus recent tweets. Hot tweets need sharded counters and cached hydration.

**Follow-ups:** ranking, trending, search, deleting content already fanned out.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/tweets` | Post a tweet (text, media refs, optional reply\_to / quote\_of). Idempotency-Key. Triggers fan-out |
| `GET /v1/tweets/{id}` | Tweet with counts and author |
| `DELETE /v1/tweets/{id}` | Soft delete; readers filter at hydration |
| `GET /v1/timeline/home?cursor=&limit=` | Home timeline (IDs from Redis/Cassandra, hydrated); cursor = last tweet\_id |
| `GET /v1/users/{id}/tweets` | Profile timeline |
| `PUT /v1/users/{id}/follow`, `DELETE /v1/users/{id}/follow` | Follow / unfollow (idempotent) |
| `GET /v1/users/{id}/followers`, `GET /v1/users/{id}/following` | Paginated graph reads |
| `PUT /v1/tweets/{id}/like`, `DELETE /v1/tweets/{id}/like` | Like / unlike |
| `POST /v1/tweets/{id}/retweet` and `POST /v1/tweets/{id}/replies` | Repost and reply |
| `GET /v1/tweets/{id}/replies` | Reply thread |
| `GET /v1/search?q=` | Search over the tweet index |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `user_account` | Identity, handle uniqueness, follower counts |
| `follow` | The social graph; both directions are queryable |
| `tweet` | Canonical tweet record; Snowflake ID gives time order |
| `timeline_entry` | Derived per-user feed of tweet IDs; rebuildable, never authoritative |
| `tweet_like` | One like per user per tweet; source for counters |
| `tweet_counters` | Fast, eventually consistent like/retweet/reply counts |

---

## 05. Uber-like Ride Hailing

**Storage:** PostgreSQL for trips and assignments; Redis (GEO or H3/geohash cell sets) for live driver positions; Kafka for events; time-series store for telemetry.

**Access patterns:** (1) find nearby available drivers; (2) assign exactly one driver to a request; (3) track a trip's state and location; (4) fare calculation and payment; (5) rider/driver trip history.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `rider` / `driver` | rider\_id, profile / driver\_id, vehicle\_id, status, rating, current\_trip\_id | driver: IDX(status); `current_trip_id` is the assignment lock |
| `driver_location` (ephemeral) | driver\_id, lat, lon, cell\_id, heading, speed, observed\_at | Redis: `drivers:{cell_id}` set or GEO; per-driver heartbeat key with TTL \~10–15 s. Reject out-of-order updates by `observed_at` |
| `ride_request` | request\_id, rider\_id, pickup/drop lat-lon, product\_type, status, created\_at | IDX(rider\_id, created\_at DESC) |
| `ride_offer` | request\_id, driver\_id, offered\_at, expires\_at, response | PK(request\_id, driver\_id) — sequential or batched offers |
| `trip` | trip\_id, request\_id, rider\_id, driver\_id, status, fare\_estimate\_minor, final\_fare\_minor, started\_at, ended\_at | UQ(request\_id); partial UQ(driver\_id) WHERE status IN active states; IDX(rider\_id, created\_at) |
| `trip_location_event` | trip\_id, seq\_no, lat, lon, event\_time | Append-only; time-series store partitioned by trip/date |
| `fare_breakdown` | trip\_id, base, distance, time, surge\_multiplier, tolls, tip | Snapshot — never recompute |
| `payment` | payment\_id, trip\_id, amount\_minor, status, provider\_ref, idempotency\_key | UQ(trip\_id) or an attempt model; tips are separate rows |

**Invariant: one active trip per driver and per request.** Enforce with a conditional update:

```sql
UPDATE driver SET status = 'ON_TRIP', current_trip_id = :t
 WHERE driver_id = :d AND status = 'AVAILABLE' AND current_trip_id IS NULL;  -- rowcount 1 = won
```

Plus the partial unique index on `trip(driver_id)` for active statuses, as a backstop. Trip states: REQUESTED → MATCHING → DRIVER\_ASSIGNED → ARRIVED → IN\_PROGRESS → COMPLETED / CANCELLED (P9).

**Failure and scale:** Redis locations are ephemeral and not authoritative for trip ownership. Offers time out and move to the next candidate. Partition matching by city or geo cell so one matching service owns a region. Choose H3/S2 over raw lat/lon scans; query the rider's cell plus a ring of neighbours.

**Follow-ups:** surge pricing (`surge_zone(cell_id, window, multiplier)`), pooled rides, driver cancellations, map-matching.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/rides/estimate` | Fare and ETA for pickup, drop-off and product type |
| `POST /v1/rides` | Request a ride; creates `ride_request` and starts matching. Idempotency-Key |
| `GET /v1/rides/{id}` | Current state, driver and vehicle info |
| `POST /v1/rides/{id}/cancel` | Cancel; may carry a fee depending on state |
| `WS /v1/rides/{id}/track` | Live driver location and status events to the rider |
| `PUT /v1/drivers/me/status` | Go online / offline |
| `POST /v1/drivers/me/location` (or a WebSocket stream) | Location pings every few seconds with observed\_at |
| `POST /v1/offers/{id}/accept`, `POST /v1/offers/{id}/decline` | Driver responds to a ride offer; accept triggers the atomic assignment |
| `POST /v1/trips/{id}/arrived`, `/start`, `/end` | Driver state transitions; `/end` computes the final fare |
| `POST /v1/trips/{id}/tip` and `POST /v1/trips/{id}/rating` | Tip and mutual ratings |
| `GET /v1/trips?cursor=` | Trip history for rider or driver |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `rider` / `driver` | Profiles; driver row holds availability and `current_trip_id`, the assignment lock |
| `driver_location` | Ephemeral latest position per driver for nearby search; rejects stale updates |
| `ride_request` | The rider's intent (pickup, drop-off, product) and matching state |
| `ride_offer` | Which drivers were offered a request, when, and their response |
| `trip` | Authoritative record of the assignment and trip lifecycle |
| `trip_location_event` | Append-only GPS trail for the trip (disputes, fare, safety) |
| `fare_breakdown` | Frozen fare components, including surge multiplier |
| `payment` | Charge for the trip; idempotent; tips tracked separately |

---

## 06. WhatsApp-like Messaging

**Storage:** Cassandra / wide-column for message history; PostgreSQL for users and membership; Redis for presence and connection routing; Kafka for delivery fan-out.

**Access patterns:** (1) append a message to a conversation in order; (2) load the latest N messages and page back; (3) chat list sorted by recent activity with unread counts; (4) deliver to online devices or queue for offline ones; (5) delivery and read state.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `user_account` | user\_id, phone\_number, profile\_name | UQ(phone\_number) |
| `conversation` | conversation\_id, type (DIRECT/GROUP), created\_by, created\_at | 1:1 chats use a deterministic id from the sorted user pair |
| `conversation_member` | conversation\_id, user\_id, role, joined\_at, last\_delivered\_seq, last\_read\_seq | PK(conversation\_id, user\_id); reverse IDX(user\_id) for the chat list. **Read pointers replace per-message receipts** |
| `message` (Cassandra) | conversation\_id, bucket, seq, message\_id, sender\_id, client\_msg\_id, body\_or\_ciphertext, type, sent\_at | PK((conversation\_id, bucket), seq); clustering seq DESC; bucket = seq / 10,000 |
| `inbox` | user\_id, conversation\_id, last\_seq, last\_activity\_at, unread\_count | Chat-list read model. Cassandra cannot update a clustering column, so delete+insert, or sort in a Redis ZSET |
| `pending_delivery` | device\_id, message\_id, enqueued\_at | PK((device\_id), message\_id); removed on device ACK — the offline queue |
| `user_device` | user\_id, device\_id, push\_token, last\_seen\_at | PK(user\_id, device\_id); also per-device E2EE prekeys |
| `presence` (ephemeral) | user\_id, state, last\_active\_at | Redis with TTL |

**Invariants:** (1) per-conversation order comes from a **sequencer**: all writes for a conversation route to one owner shard (consistent hashing) that assigns `seq`. (2) Sends are idempotent via `(sender_id, client_msg_id)` dedupe at the sequencer. (3) Read and delivery state only move forward (`last_read_seq = max(old, new)`).

**Design choices worth saying:** store each message **once per conversation**, not once per recipient; per-recipient state for large groups is a pointer, not a row per message. Media goes to object storage; the message carries the key and the decryption key. With end-to-end encryption the server stores ciphertext only and cannot search it.

**Failure and scale:** Gateways hold connections and register `user → gateway` in Redis. An ACK deletes the pending row; unACKed messages replay on reconnect. Bucket by sequence range so a page-back cursor maps straight to a partition.

**Follow-ups:** group size limits, multi-device sync, message edit/delete (new events), disappearing messages (TTL), push notification fallback.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `WS /v1/connect` | Persistent connection. Client events: `send_message`, `ack`, `typing`, `presence`. Server events: `message`, `receipt`, `typing` |
| `POST /v1/conversations/{id}/messages` | REST fallback to send. Body: client\_msg\_id, type, ciphertext or media ref. Returns server seq. Dedupes on (sender, client\_msg\_id) |
| `GET /v1/conversations/{id}/messages?before_seq=&limit=` | Page back through history (partition = conversation + bucket) |
| `GET /v1/conversations` | Chat list with last message and unread count (from `inbox`) |
| `POST /v1/conversations` | Create a group |
| `POST /v1/conversations/{id}/members`, `DELETE /v1/conversations/{id}/members/{uid}` | Add, remove or leave |
| `POST /v1/conversations/{id}/read` | Body: seq. Advances `last_read_seq` (monotonic) |
| `POST /v1/media/upload-url` | Presigned URL for encrypted media upload |
| `POST /v1/devices` | Register a device: push token and E2EE prekeys |
| `GET /v1/users/{id}/prekeys` | Fetch keys to start an encrypted session |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `user_account` | Identity by phone number |
| `conversation` | Chat identity and type (direct or group) |
| `conversation_member` | Who is in a chat, their role, and delivered/read pointers |
| `message` | Immutable ordered history per conversation, bucketed by sequence |
| `inbox` | Per-user chat-list read model with last activity and unread counts |
| `pending_delivery` | Offline queue per device; row deleted on ACK |
| `user_device` | Devices, push tokens and key material |
| `presence` | Ephemeral online / last-seen state with TTL |

---

## 07. Zoom-like Video Conferencing

**Storage:** Relational DB for meetings and membership; Redis for ephemeral presence and signaling state; WebSocket for signaling; WebRTC/SFU media plane carries audio and video; object storage for recordings.

**Access patterns:** (1) join by meeting code; (2) who is in the meeting right now; (3) schedule and list upcoming meetings; (4) attendance history; (5) recording lookup.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `user_account` | user\_id, email, display\_name | UQ(email) |
| `meeting` | meeting\_id, host\_user\_id, meeting\_code, title, scheduled\_start, scheduled\_end, recurrence\_rule, settings\_json | UQ(meeting\_code); IDX(host\_user\_id, scheduled\_start) |
| `meeting_session` | session\_id, meeting\_id, started\_at, ended\_at, media\_region, sfu\_node\_id | Partial UQ(meeting\_id) WHERE ended\_at IS NULL — at most one live session per meeting |
| `participant_session` | participant\_session\_id, session\_id, user\_id, role, joined\_at, left\_at | IDX(session\_id); IDX(user\_id, joined\_at DESC). **One row per join/leave interval**, because users rejoin |
| `participant_device` (ephemeral) | session\_id, user\_id, device\_id, signaling\_state, last\_heartbeat | Redis with TTL; not durable |
| `recording` | recording\_id, session\_id, object\_key, status (RECORDING/PROCESSING/READY/FAILED), duration\_s, size\_bytes | IDX(session\_id); media bytes live in object storage |

**Invariants:** one live session per meeting (partial unique index); host controls (mute, remove, waiting room) are authorized against `role` at the signaling layer; a participant count never comes from the DB, it comes from the live session service.

**Failure and scale:** Never store media packets in a database. If an SFU dies, the session service reassigns the meeting to another node and clients re-negotiate. A heartbeat TTL turns crashed clients into leave events. Large meetings use SFU cascading; webinars separate presenters (publish) from attendees (subscribe-only).

**Follow-ups:** waiting room, breakout rooms, chat history, recording pipeline (transcode, thumbnails), regional media routing.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/meetings` | Schedule or create an instant meeting; returns meeting\_id and code |
| `GET /v1/meetings/{idOrCode}` | Meeting details and settings |
| `PATCH /v1/meetings/{id}`, `DELETE /v1/meetings/{id}` | Reschedule, change settings, cancel |
| `GET /v1/meetings?from=&to=` | Upcoming meetings |
| `POST /v1/meetings/{code}/join` | Authorize, create or attach to the live session, return signaling URL, short-lived token and assigned SFU |
| `POST /v1/meetings/{id}/leave` | Close the participant interval |
| `POST /v1/meetings/{id}/participants/{uid}/mute`, `/remove`, `/admit` | Host controls, authorized by role |
| `POST /v1/meetings/{id}/recordings` and `/recordings/{rid}/stop` | Start and stop recording |
| `GET /v1/recordings/{id}` | Metadata and signed download URL |
| `WS /v1/signal` | Signaling: offer, answer, ICE candidates, roster updates, chat |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `user_account` | Identity and profile |
| `meeting` | Scheduled definition: host, code, time, recurrence, settings |
| `meeting_session` | One live occurrence; at most one open per meeting; records media region and SFU |
| `participant_session` | Each join/leave interval; powers attendance reports |
| `participant_device` | Ephemeral heartbeat and signaling state (Redis) |
| `recording` | Recording lifecycle and pointer to media in object storage |

---

## 08. Netflix-like Video Streaming

**Storage:** PostgreSQL for catalog, plans and entitlements; object storage plus CDN for segments and manifests; Cassandra/NoSQL for viewing history at scale; Redis for hot metadata and concurrent-stream limits.

**Access patterns:** (1) home page rows (recommendations); (2) title detail; (3) start playback: entitlement check, manifest URL, DRM license; (4) resume position / continue watching; (5) concurrent stream limit per account.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `content` | content\_id, content\_type (MOVIE/SERIES/SEASON/EPISODE), parent\_id, title, description, release\_date, maturity\_rating | IDX(parent\_id). Hierarchy via `parent_id` plus `season_no`, `episode_no`; UQ(parent\_id, season\_no, episode\_no) for episodes |
| `content_availability` | content\_id, region, available\_from, available\_until | PK(content\_id, region, available\_from) — licensing windows |
| `content_asset` | asset\_id, content\_id, codec, resolution, bitrate, manifest\_key, drm\_key\_id, status | IDX(content\_id); one row per rendition/encoding |
| `account` / `profile` | account\_id, plan\_id / profile\_id, account\_id, name, maturity\_level | IDX(account\_id) |
| `subscription` | subscription\_id, account\_id, plan\_id, status, start\_at, end\_at | IDX(account\_id, status). Entitlement = active subscription + plan allows quality/region + content available in region |
| `watch_progress` | profile\_id, content\_id, position\_ms, duration\_ms, updated\_at, completed | PK(profile\_id, content\_id); write-through from Redis heartbeats every \~30 s |
| `continue_watching` (read model) | profile\_id, updated\_at, content\_id, position\_ms | Cassandra PK((profile\_id), updated\_at DESC) — trim to the latest N |
| `viewing_event` | profile\_id, month\_bucket, event\_time, content\_id, event\_type, device\_id | Cassandra PK((profile\_id, month\_bucket), event\_time DESC); also streamed to the analytics warehouse |
| `profile_recommendation` | profile\_id, row\_type, rank, content\_id, model\_version | Offline-computed, served as a key lookup — no joins at request time |

**Invariants:** playback access is decided from subscription, region and maturity — never trust the client. Progress is last-write-wins by `updated_at` (or a monotonic position per device). Concurrent streams are enforced with a Redis set per account whose members expire via heartbeat TTL.

**Failure and scale:** Video bytes are segmented renditions on a CDN; seeking is a segment or byte-range fetch, not a DB query. Catalog metadata is heavily cached and eventually consistent. If the progress store is down, playback still works and the client resumes from its local position.

**Follow-ups:** adaptive bitrate, offline downloads (licence expiry), A/B experiments on rows, multi-region catalog replication.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/home` | Personalized rows for a profile (precomputed recommendations plus catalog) |
| `GET /v1/titles/{id}` | Title detail, maturity rating, availability in the user's region |
| `GET /v1/titles/{id}/episodes?season=` | Episode list for a series |
| `GET /v1/search?q=` | Title search |
| `POST /v1/playback/start` | Check entitlement and concurrent-stream limit; return manifest URL, DRM license token, resume position |
| `POST /v1/playback/{sessionId}/heartbeat` | Position update every \~30 s; keeps the stream slot alive |
| `POST /v1/playback/{sessionId}/stop` | End session; persist progress |
| `GET /v1/continue-watching` | Resume list for a profile |
| `PUT /v1/profiles/{id}/my-list/{titleId}`, `DELETE ...` | Manage the watchlist |
| `GET /v1/profiles`, `POST /v1/profiles` | Manage profiles |
| `POST /v1/subscriptions`, `DELETE /v1/subscriptions/{id}` | Subscribe and cancel |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `content` | Catalog with movie / series / season / episode hierarchy |
| `content_availability` | Licensing windows per region |
| `content_asset` | Each encoded rendition and its manifest and DRM key reference |
| `account` / `profile` | Billing owner and per-person profiles with maturity limits |
| `subscription` | Plan and status; input to entitlement decisions |
| `watch_progress` | Latest position per profile and title |
| `continue_watching` | Read model for the resume row |
| `viewing_event` | Append-only watch history for analytics and recommendations |
| `profile_recommendation` | Offline-computed rows served by key lookup |

---

## 09. Facebook-like Social Network

**Storage:** Sharded relational store (or graph-style objects and associations) for identity, friendships and posts; object storage for photos and video; Redis/Memcached for hot objects and feeds; search index; Kafka for fan-out and notifications.

**Access patterns:** (1) my feed; (2) a user's profile and wall with privacy filtering; (3) friend list and mutual friends; (4) comments and reactions on a post; (5) notifications.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `user_account` | user\_id, name, email, created\_at | UQ(email) |
| `friendship` | user\_low\_id, user\_high\_id, status (PENDING/ACCEPTED/BLOCKED), requested\_by, updated\_at | PK(user\_low\_id, user\_high\_id) with CHECK(user\_low\_id < user\_high\_id); IDX(user\_high\_id, user\_low\_id). Alternative: two directed rows so each user's friends live on their own shard |
| `post` | post\_id, author\_id, text, audience (PUBLIC/FRIENDS/CUSTOM), created\_at, deleted\_at | IDX(author\_id, post\_id DESC) |
| `post_audience` | post\_id, list\_id or user\_id | Custom audience ACL; checked at read |
| `post_media` | media\_id, post\_id, object\_key, media\_type, sort\_order | IDX(post\_id, sort\_order) |
| `comment` | comment\_id, post\_id, author\_id, parent\_comment\_id, body, created\_at | IDX(post\_id, comment\_id); IDX(parent\_comment\_id) for replies |
| `reaction` | user\_id, target\_type, target\_id, reaction\_type, created\_at | PK(user\_id, target\_type, target\_id); IDX(target\_type, target\_id, reaction\_type) for counts |
| `feed_entry` | user\_id, post\_id, author\_id, score\_hint | Projection: Cassandra PK((user\_id, bucket), post\_id DESC) or Redis ZSET. IDs only |
| `notification` | user\_id, notif\_id, type, actor\_id, target\_id, created\_at, read\_at | PK((user\_id), notif\_id DESC) |

**Invariants:** privacy is enforced at **read time** against canonical relationships (a feed entry never grants access by itself). Friendship has exactly one canonical row per pair. Reaction is unique per user per target; counts are denormalized and repairable.

**Failure and scale:** Feed entries are projections that can be rebuilt from posts plus the friend graph. Ranking happens at read time over a candidate set (recent entries plus pulled content). Blocking and unfriending must invalidate cached audiences and filter at hydration.

**Follow-ups:** groups and pages, graph-style sharding by user, mutual-friends computation, privacy-change propagation.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/posts` | Create a post with text, media refs and audience. Idempotency-Key |
| `GET /v1/posts/{id}`, `DELETE /v1/posts/{id}` | Read (privacy-checked) and delete |
| `GET /v1/feed?cursor=` | Ranked feed built from `feed_entry` candidates |
| `GET /v1/users/{id}/posts` | Profile wall, filtered by the viewer's access |
| `POST /v1/friends/requests` | Send a friend request |
| `POST /v1/friends/requests/{id}/accept`, `/decline` | Respond to a request |
| `DELETE /v1/friends/{userId}` | Unfriend |
| `GET /v1/users/{id}/friends`, `GET /v1/users/{id}/mutual-friends` | Graph reads |
| `POST /v1/posts/{id}/comments`, `GET /v1/posts/{id}/comments` | Comments and replies |
| `PUT /v1/posts/{id}/reaction`, `DELETE /v1/posts/{id}/reaction` | One reaction per user |
| `POST /v1/media/upload-url` | Presigned upload for photos and video |
| `GET /v1/notifications`, `POST /v1/notifications/read` | Notification list and mark read |
| `POST /v1/blocks` | Block a user |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `user_account` | Identity and profile |
| `friendship` | One canonical relationship row per pair with status |
| `post` | Canonical posts and their audience setting |
| `post_audience` | Custom audience ACL checked at read time |
| `post_media` | Ordered media attached to a post |
| `comment` | Threaded discussion on posts |
| `reaction` | One reaction per user per target; source for counts |
| `feed_entry` | Derived per-user feed candidates (IDs only) |
| `notification` | Per-user inbox of events |

---

## 10. Airbnb-like Accommodation Booking

**Storage:** PostgreSQL for listings, availability and bookings; search engine for geo/filter search; Redis for caching; payment provider integration.

**Access patterns:** (1) search by location, dates, guests and filters; (2) listing detail with a price quote; (3) reserve and pay for a stay; (4) host calendar management; (5) reviews.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `host` | host\_id, user\_id, verification\_status | UQ(user\_id) |
| `listing` | listing\_id, host\_id, title, lat, lon, geo\_cell, capacity, status, cleaning\_fee\_minor | IDX(host\_id); geo index in search engine |
| `listing_photo` | photo\_id, listing\_id, object\_key, sort\_order | IDX(listing\_id, sort\_order) |
| `availability_calendar` | listing\_id, stay\_date, status (AVAILABLE/BLOCKED/HELD/BOOKED), nightly\_price\_minor, booking\_id, version | PK(listing\_id, stay\_date) — one row per listing-night |
| `booking` | booking\_id, listing\_id, guest\_id, check\_in, check\_out (exclusive), status, total\_minor, currency, expires\_at, created\_at | IDX(guest\_id, created\_at DESC); IDX(listing\_id, check\_in); optional EXCLUDE constraint (P8) |
| `booking_price_breakdown` | booking\_id, nightly\_total, cleaning\_fee, service\_fee, taxes | Snapshot at booking time |
| `payment` / `payout` | payment\_id, booking\_id, status, provider\_ref / payout\_id, host\_id, booking\_id, release\_at | IDX(booking\_id); payouts release after check-in |
| `review` | review\_id, booking\_id, reviewer\_role (GUEST/HOST), rating, comment, published\_at | UQ(booking\_id, reviewer\_role) — two-sided; published together after both submit or a deadline |

**Invariant: a night is booked at most once.** Two workable mechanisms:

```sql
-- A) night-claim: all-or-nothing, rowcount must equal the number of nights
UPDATE availability_calendar
   SET status = 'HELD', booking_id = :b
 WHERE listing_id = :l AND stay_date >= :check_in AND stay_date < :check_out
   AND status = 'AVAILABLE';
-- B) range exclusion on booking (P8): the DB rejects overlapping PENDING/CONFIRMED stays
```

Holds carry `expires_at`; unpaid holds release the nights.

**Failure and scale:** Search results are cached and may be stale; the claim above is the only guarantee. The hard search problem is **date-range availability**: precompute availability into the index for coarse filtering, then verify against the calendar for the top candidates. Partition calendars by `listing_id`.

**Follow-ups:** instant book vs request-to-book, cancellation policies and refunds, dynamic pricing, minimum-stay rules.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/listings/search?lat=&lon=&radius=&check_in=&check_out=&guests=&filters=` | Geo and filter search via the search engine; availability pre-filtered, verified for top results |
| `GET /v1/listings/{id}` | Listing detail and photos |
| `GET /v1/listings/{id}/availability?from=&to=` | Calendar with nightly prices |
| `POST /v1/listings/{id}/quote` | Price breakdown for the stay (nights, fees, taxes) |
| `POST /v1/bookings` | Claim the nights and create a PENDING booking with expiry. Idempotency-Key. 409 if any night is taken |
| `POST /v1/bookings/{id}/pay` | Pay and confirm |
| `POST /v1/bookings/{id}/cancel` | Cancel with policy-based refund; releases nights |
| `GET /v1/bookings`, `GET /v1/bookings/{id}` | Guest trips |
| `POST /v1/host/listings`, `PUT /v1/host/listings/{id}` | Create and edit listings |
| `PUT /v1/host/listings/{id}/calendar` | Block dates, set prices and rules |
| `POST /v1/bookings/{id}/accept`, `/decline` | Host decision for request-to-book |
| `POST /v1/bookings/{id}/reviews`, `GET /v1/listings/{id}/reviews` | Two-sided reviews |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `host` | Host profile and verification state |
| `listing` | Property details, location and capacity; feeds the search index |
| `listing_photo` | Ordered images |
| `availability_calendar` | Per-night status and price; the night-claim target that prevents double booking |
| `booking` | Stay record and lifecycle; optional exclusion constraint on overlapping stays |
| `booking_price_breakdown` | Frozen price components at booking time |
| `payment` / `payout` | Guest charge and delayed host payout |
| `review` | Two-sided review per booking, published together |

---

## 11. Google Docs-like Collaborative Editor

**Storage:** Relational DB for documents and ACLs; an append-only operation log; object storage for snapshots; WebSocket collaboration servers; OT or CRDT for concurrent edits.

**Access patterns:** (1) open a document: latest snapshot plus operations since; (2) submit an operation and broadcast it; (3) presence and cursors; (4) version history; (5) comments anchored to text; (6) sharing and permissions.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `document` | document\_id, owner\_id, title, head\_revision, updated\_at | IDX(owner\_id, updated\_at DESC) |
| `document_acl` | document\_id, principal\_id, role (VIEWER/COMMENTER/EDITOR/OWNER), granted\_by | PK(document\_id, principal\_id); IDX(principal\_id) for 'shared with me' |
| `link_share` | document\_id, role, token\_hash, expires\_at | Anyone-with-link access |
| `document_operation` | document\_id, revision, client\_id, client\_seq, op\_payload, base\_revision, actor\_id, created\_at | PK(document\_id, revision); UQ(document\_id, client\_id, client\_seq) for dedupe |
| `document_snapshot` | snapshot\_id, document\_id, revision, object\_key, created\_at | IDX(document\_id, revision DESC); written every N ops or minutes |
| `named_version` | document\_id, version\_id, revision, label, created\_by | Version history points at a revision |
| `document_comment` | comment\_id, document\_id, anchor, body, author\_id, resolved\_at | Anchor must be transformed with operations (or attach to CRDT element ids) |
| `presence` (ephemeral) | document\_id, user\_id, cursor, updated\_at | Redis / in-memory; TTL |

**Invariant: operations form one total order per document.** Route all connections for a document to one collaboration server (sticky routing by `document_id`) that assigns the next `revision`. The `(document_id, revision)` primary key is the backstop: two concurrent appends cannot both claim the same revision, the loser transforms and retries. `(client_id, client_seq)` makes client retries idempotent.

**Failure and scale:** Load = latest snapshot + replay of later operations. If the owning server dies, another loads from the log (the log is the truth) and clients resend unacknowledged operations. OT needs a central sequencer; CRDTs allow offline and peer merging at the cost of larger metadata. Compact old operations once snapshots exist.

**Follow-ups:** offline editing, suggestions mode, large-document sharding, export, permission revocation during an active session.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/documents` | Create a document |
| `GET /v1/documents/{id}` | Open: latest snapshot, revision, role |
| `PATCH /v1/documents/{id}`, `DELETE /v1/documents/{id}` | Rename, trash |
| `GET /v1/documents?filter=owned` or `shared` | Lists |
| `GET/PUT/DELETE /v1/documents/{id}/permissions` | Manage ACL entries |
| `POST /v1/documents/{id}/link-share` | Enable anyone-with-link at a role |
| `GET /v1/documents/{id}/revisions`, `POST /v1/documents/{id}/versions` | History and named versions |
| `POST /v1/documents/{id}/restore/{rev}` | Restore as a new revision |
| `POST /v1/documents/{id}/comments`, `GET ...`, `POST .../{cid}/resolve` | Anchored comments |
| `GET /v1/documents/{id}/export?format=` | Export to PDF or docx |
| `WS /v1/documents/{id}/collab` | Messages: `join` (with revision), `op` (client\_id, client\_seq, base\_revision, ops), `ack` (revision), `remote_op`, `cursor` |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `document` | Metadata and head revision pointer |
| `document_acl` | Who may view, comment, edit, own |
| `link_share` | Link-based access tokens and role |
| `document_operation` | Authoritative ordered log of edits; PK enforces one order per document; client ids dedupe retries |
| `document_snapshot` | Compacted state to speed loading |
| `named_version` | Human-labelled points in history |
| `document_comment` | Comments with anchors that move with edits |
| `presence` | Ephemeral cursors and active users |

---

## 12. LeetCode-like Online Judge

**Storage:** PostgreSQL for users, problems and submissions; object storage for test data and source code; a queue for execution; isolated sandbox workers; Redis for live leaderboards.

**Access patterns:** (1) submit code and poll or stream the result; (2) problem list and detail; (3) a user's submission history; (4) contest standings in near real time; (5) rejudge a problem.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `user_account` | user\_id, username, email | UQ(username), UQ(email) |
| `problem` | problem\_id, slug, title, difficulty, current\_version\_id | UQ(slug); IDX(difficulty) |
| `problem_version` | version\_id, problem\_id, statement\_key, time\_limit\_ms, memory\_limit\_mb, checker\_type | Submissions point at a version so rejudges are reproducible |
| `problem_test_case` | test\_case\_id, version\_id, input\_key, expected\_key, is\_hidden, weight | IDX(version\_id); hidden data is access-controlled |
| `language` | language\_id, compile\_cmd, run\_cmd, image, version | Sandbox image per language |
| `submission` | submission\_id, user\_id, problem\_version\_id, language\_id, source\_key, status, submitted\_at | IDX(user\_id, submitted\_at DESC); IDX(user\_id, problem\_id); IDX(status, submitted\_at) for the queue |
| `judge_run` | run\_id, submission\_id, worker\_id, status, started\_at, finished\_at, verdict | IDX(submission\_id); one row per judging attempt |
| `test_result` | run\_id, test\_case\_id, verdict, runtime\_ms, memory\_kb | PK(run\_id, test\_case\_id) |
| `contest` / `contest_participant` | contest\_id, starts\_at, ends\_at / contest\_id, user\_id, score, penalty, solved\_count | PK(contest\_id, user\_id); IDX(contest\_id, score DESC, penalty) |

**Invariants:** a submission is judged to completion exactly once per run (lease + status guard, P4/P9); a worker with an expired lease cannot write results (fencing by `run_id`/attempt). Standings are a projection of accepted submissions (P10).

**Failure and scale:** The API persists the submission and enqueues; workers pull, run in an isolated sandbox (no network, cgroup limits, seccomp or gVisor/Firecracker), then store results. A crashed worker's lease expires and the run is retried. Live ranking: Redis ZSET per contest (score encoded with penalty), rebuilt from submissions if lost. Rate-limit submissions per user.

**Follow-ups:** plagiarism detection, special judges, partial scoring, contest-time load spikes.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/problems?difficulty=&tag=` | Problem list |
| `GET /v1/problems/{slug}` | Statement, limits, sample tests (hidden tests never exposed) |
| `POST /v1/problems/{slug}/run` | Run against sample tests only; short-lived and not stored as a submission |
| `POST /v1/problems/{slug}/submissions` | Body: language, code. Rate-limited. Returns 202 with submission\_id and enqueues judging |
| `GET /v1/submissions/{id}` | Status and verdict (poll) or `GET /v1/submissions/{id}/events` (SSE) for live updates |
| `GET /v1/users/me/submissions?problem=` | Submission history |
| `GET /v1/contests`, `POST /v1/contests/{id}/register` | Discover and join contests |
| `GET /v1/contests/{id}/standings` | Leaderboard from Redis ZSET |
| `POST /v1/admin/problems`, `POST /v1/admin/problems/{id}/versions` | Author problems and versions |
| `POST /v1/admin/problems/{id}/rejudge` | Rejudge submissions against a version |
| `POST /internal/runs/claim`, `/runs/{id}/heartbeat`, `/runs/{id}/result` | Worker protocol with lease and fencing |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `user_account` | Identity |
| `problem` | Public identity of a problem and pointer to its current version |
| `problem_version` | Immutable statement and limits so rejudges are reproducible |
| `problem_test_case` | Test inputs and expected outputs; hidden ones are access-controlled |
| `language` | Compile and run commands and sandbox image per language |
| `submission` | The user's code and overall status; the queue source |
| `judge_run` | One judging attempt with worker and verdict |
| `test_result` | Per-test verdict, time and memory |
| `contest` / `contest_participant` | Contest schedule and per-user score, penalty and rank |

---

## 13. Rate Limiter

**Storage:** Redis for atomic counters and bucket state; a config store for policies (cached locally); optional durable event stream for billing and analytics.

**Access patterns:** (1) allow/deny decision in about a millisecond; (2) resolve the policy for a (tenant, user, route) tuple; (3) update policies without redeploys; (4) analytics on throttling.

| Table / key | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `rate_limit_policy` | policy\_id, scope\_type (IP/USER/API\_KEY/TENANT), scope\_id, algorithm, limit, window\_ms, burst, enabled | IDX(scope\_type, scope\_id); cached in process memory, refreshed by push or short poll |
| `rate_limit_rule` | rule\_id, policy\_id, route\_pattern, method, priority, plan\_tier | Most specific rule wins; IDX(policy\_id, priority) |
| `rl:{policy}:{subject}` (Redis hash) | tokens, last\_refill\_ts | Token bucket state; TTL ≈ time to full refill |
| `rl:{policy}:{subject}:{window}` (Redis counter) | count | Fixed or sliding-window counter with TTL |
| `decision_log` | request\_id, subject\_key, policy\_id, allowed, decided\_at | Sampled and asynchronous only — never on the hot path |

**Invariant: check-and-update is atomic.** Use a Lua script (or a single atomic command) so two requests cannot both consume the last token. Take the time from Redis (`TIME`) inside the script, not from app servers with drifting clocks.

**Algorithm cheat sheet:** token bucket (bursts allowed, 2 values per key); sliding-window counter (cheap and smooth, approximate); sliding-window log (exact, memory-hungry); fixed window (simple, boundary spikes).

**Failure and scale:** Shard keys by subject; avoid one global hot key (split into N sub-counters or use local token pre-fetching with periodic sync for approximate limits). Decide per route whether Redis outage means **fail-open** (most APIs) or **fail-closed** (login, SMS, payments). Return `429` with `Retry-After` and remaining-quota headers. Multi-region: per-region limits plus an async global budget.

**Follow-ups:** distributed vs local limiting accuracy, quota vs rate, per-endpoint cost weights.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/check` (or an in-process library call) | Body: subject, route, cost. Returns allowed, remaining, retry\_after\_ms. Single atomic Redis operation |
| Client-facing behaviour | HTTP 429 with `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset` |
| `POST /v1/policies`, `GET /v1/policies/{id}`, `PUT /v1/policies/{id}`, `DELETE /v1/policies/{id}` | Manage policies; changes pushed to gateways |
| `POST /v1/policies/{id}/rules`, `PUT .../rules/{rid}`, `DELETE ...` | Per-route rules with priority |
| `GET /v1/usage?subject=` | Current consumption for a subject |
| `POST /v1/overrides`, `DELETE /v1/overrides/{id}` | Temporary allowlist or blocklist with TTL |
| `GET /v1/decisions?subject=&from=` | Sampled decision log for debugging |

**Table / key responsibilities**

| Table / key | Responsibility |
| --- | --- |
| `rate_limit_policy` | Defines algorithm, limit, window and burst per scope |
| `rate_limit_rule` | Maps routes and tiers to a policy; most specific rule wins |
| `rl:{policy}:{subject}` | Live token bucket state in Redis |
| `rl:{policy}:{subject}:{window}` | Window counters with TTL |
| `decision_log` | Sampled audit and analytics; off the hot path |

---

## 14. Google Calendar

**Storage:** PostgreSQL for events, attendees, ACLs and reminders; Debezium/outbox → Kafka; Elasticsearch for search; Redis for cache.

**Access patterns:** (1) list events in a time window across calendars (expanding recurrences); (2) create or update an event and notify attendees; (3) free/busy for several people; (4) reminders firing on time; (5) incremental sync for clients.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `calendar` | calendar\_id, owner\_user\_id, name, timezone, visibility | IDX(owner\_user\_id) |
| `calendar_acl` | calendar\_id, principal\_id, role | PK(calendar\_id, principal\_id); IDX(principal\_id) |
| `event` (series master or single) | event\_id, calendar\_id, organizer\_id, title, start\_at (UTC), end\_at, tz, is\_all\_day, rrule, rrule\_until, status, version | IDX(calendar\_id, start\_at); IDX(calendar\_id, rrule\_until) to find recurring series that overlap a window |
| `event_override` | event\_id, original\_start, override\_fields\_json or cancelled | PK(event\_id, original\_start) — **only exceptions are stored**; normal instances are expanded from `rrule` |
| `event_attendee` | event\_id, user\_id, response\_status, updated\_at | PK(event\_id, user\_id); IDX(user\_id, event\_id) |
| `busy_interval` | user\_id, start\_at, end\_at, event\_id | IDX(user\_id, start\_at) — materialized free/busy; updated on event/response changes |
| `reminder` | event\_id, user\_id, minutes\_before, next\_fire\_at, status | IDX(status, next\_fire\_at); keep only the **next** fire time per series, advance after firing |
| `calendar_change` | calendar\_id, seq, event\_id, change\_type | PK(calendar\_id, seq) — incremental sync via sync token = last seq |
| `outbox` | event\_id, aggregate\_id, event\_type, payload, published\_at | P3 |

**Recurrence rules to state:** store the start in UTC **plus** the IANA timezone and expand in local time so 9:00 stays 9:00 across DST; all-day events are dates, not timestamps; editing 'this and following' splits the series (set `rrule_until` on the old master, create a new master); editing 'this event only' writes an `event_override`.

**Invariants:** event, attendee and reminder changes commit in one transaction; updates use optimistic `version` (ETag) to avoid lost edits; search results are filtered by ACL after retrieval.

**Failure and scale:** Expanding recurrences at read time keeps writes small; cap unbounded series (`rrule_until` or a horizon, e.g. 2–5 years) when indexing. Reminder workers claim due rows with a lease (P4) and are idempotent. Free/busy across many attendees reads `busy_interval` only.

**Follow-ups:** room booking conflicts, external invites (iCal), per-attendee copies of events vs one shared event, working hours.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/calendars`, `POST /v1/calendars` | List and create calendars |
| `GET /v1/calendars/{id}/events?from=&to=&sync_token=` | Events in a window with recurrences expanded; with sync\_token returns only changes |
| `POST /v1/calendars/{id}/events` | Create an event with attendees, rrule, reminders |
| `GET /v1/events/{id}`, `PATCH /v1/events/{id}`, `DELETE /v1/events/{id}` | Read and edit. `If-Match` ETag; query `scope=this` or `following` or `all` for recurring series |
| `POST /v1/events/{id}/respond` | Accept, decline or tentative |
| `POST /v1/freebusy` | Body: users, from, to. Returns busy intervals |
| `GET/PUT /v1/calendars/{id}/acl` | Sharing |
| `POST /v1/events/{id}/reminders` | Add or change reminders |
| `GET /v1/search?q=` | Search with ACL enforcement |
| `POST /v1/events/import` | Import ICS |
| `POST /v1/calendars/{id}/watch` | Push notifications for changes |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `calendar` | Container with owner, timezone and visibility |
| `calendar_acl` | Who can read or edit a calendar |
| `event` | Single event or recurring series master |
| `event_override` | Exceptions to a series (moved or cancelled instances) |
| `event_attendee` | Invitees and their responses |
| `busy_interval` | Materialized free/busy for fast availability queries |
| `reminder` | Next due notification per event and user |
| `calendar_change` | Ordered change feed for incremental sync |
| `outbox` | Reliable notifications and search updates |

---

## 15. Yelp-like Local Search & Reviews

**Storage:** Relational DB for businesses and reviews; a search engine for geo, text and filters; Redis for hot results; object storage for photos.

**Access patterns:** (1) search by keyword, category and location, ranked by distance, rating and relevance; (2) business detail with recent reviews; (3) post, edit or delete a review; (4) 'open now' filtering.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `user_account` | user\_id, name, email | UQ(email) |
| `business` | business\_id, name, description, lat, lon, geo\_cell, address, status, timezone | Geo index (PostGIS GiST or the search engine); text index in the search engine |
| `business_hours` | business\_id, day\_of\_week, open\_time, close\_time | PK(business\_id, day\_of\_week, open\_time); overnight hours have close < open; `business.timezone` interprets them. Add `special_hours` for holidays |
| `category` / `business_category` | category\_id, parent\_id / business\_id, category\_id | PK(business\_id, category\_id); IDX(category\_id) |
| `review` | review\_id, business\_id, user\_id, rating, text, status (RECOMMENDED/NOT\_RECOMMENDED/REMOVED), created\_at, edited\_at | UQ(user\_id, business\_id); IDX(business\_id, created\_at DESC) |
| `review_photo` | photo\_id, review\_id, object\_key | IDX(review\_id) |
| `business_rating_aggregate` | business\_id, rating\_sum, rating\_count, histogram\_json, updated\_at | Derived. Update incrementally: `rating_sum = rating_sum + :delta`; edits and deletes apply a delta, not a recount |

**Invariants:** one review per user per business (edits allowed); the aggregate equals the sum over visible reviews (verify with a periodic recount job, P10); photos and text are moderated before or soon after publication.

**Failure and scale:** The search index is derived from the DB through CDC. Use geohash/S2/H3 cells or the engine's geo-distance for the radius query, then rank. 'Open now' is computed at query time in the business's timezone, or precomputed as minute-of-week ranges for fast filtering. Ranking should not trust raw averages; use Bayesian smoothing for businesses with few reviews.

**Follow-ups:** fake-review detection, business claim and verification, review votes, personalization.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/businesses/search?q=&category=&lat=&lon=&radius=&open_now=&sort=` | Geo, text and filter search in the search engine |
| `GET /v1/businesses/{id}` | Detail, hours, rating aggregate, photos |
| `GET /v1/businesses/{id}/reviews?cursor=&sort=` | Reviews, recommended first |
| `POST /v1/businesses/{id}/reviews` | Post a review (one per user per business); updates the aggregate |
| `PUT /v1/reviews/{id}`, `DELETE /v1/reviews/{id}` | Edit or delete own review (applies a rating delta) |
| `POST /v1/reviews/{id}/photos` | Upload URL for review photos |
| `POST /v1/reviews/{id}/vote`, `POST /v1/reviews/{id}/report` | Helpfulness vote and abuse report |
| `POST /v1/businesses`, `PUT /v1/businesses/{id}` | Owner creates or edits a listing |
| `POST /v1/businesses/{id}/claim` | Claim and verify ownership |
| `PUT /v1/businesses/{id}/hours` | Regular and special hours |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `user_account` | Reviewer identity |
| `business` | Listing, location, timezone; source for the search index |
| `business_hours` | Weekly hours including overnight spans |
| `category` / `business_category` | Category tree and business tagging |
| `review` | Canonical review with moderation status; unique per user and business |
| `review_photo` | Images attached to reviews |
| `business_rating_aggregate` | Fast rating sum, count and histogram; repaired by a recount job |

---

## 16. Top-K Views / Trending Content

**Storage:** Kafka for ingestion; Flink/Spark streaming for event-time aggregation and dedupe; ClickHouse for arbitrary time-range analytics; Redis sorted sets for serving fixed-window top-K.

**Access patterns:** (1) top K in the last 1 hour / 24 hours / 7 days; (2) top K for a category or region; (3) arbitrary historical ranges (analytics); (4) a single item's recent count.

| Table / key | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `view_event` | event\_id, content\_id, viewer\_id\_hash, event\_time, watch\_ms, source | Kafka topic keyed by `content_id`; dedupe on `event_id` |
| `content_metadata` | content\_id, title, category, published\_at | PK(content\_id); joined only at serve time |
| `content_view_bucket` (ClickHouse) | window\_start, content\_id, category, region, view\_count | AggregatingMergeTree / SummingMergeTree ORDER BY (window\_start, category, content\_id); partition by day; 1-minute buckets rolled up to hour/day |
| `topk:{window}:{start}` (Redis ZSET) | member = content\_id, score = count | ZINCRBY by the aggregator; TTL = window + slack; read with ZREVRANGE |
| `topk_snapshot` | window\_type, window\_start, rank, content\_id, score, computed\_at | PK(window\_type, window\_start, rank) — immutable published results |
| `dedupe_state` | event\_id, first\_seen\_at | Flink keyed state with TTL, or Redis SET NX with expiry |

**Invariants:** define the metric first (raw views vs unique viewers). Because events are partitioned by `content_id`, each item's count lives in one partition: compute local top-K per partition, then merge globally. Windows use **event time** with a watermark and an allowed-lateness policy.

**Failure and scale:** Skew from viral items is handled by salting the key (`content_id#0..N`) and re-merging. For huge cardinality use Count-Min Sketch plus a min-heap (approximate). Streaming results are provisional; a batch job recomputes exact numbers from raw events and corrects the serving store. Redis outage: serve the last snapshot.

**Follow-ups:** sliding vs tumbling vs hopping windows, exactly-once end to end, anti-spam and bot filtering.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/events/views` | Batch ingest of view events (event\_id, content\_id, viewer hash, time). Returns 202 after the Kafka write |
| `GET /v1/topk?window=1h&category=&region=&k=10` | Serve the current top-K from Redis ZSET or the latest snapshot |
| `GET /v1/topk/history?from=&to=&k=&category=` | Arbitrary ranges from ClickHouse |
| `GET /v1/content/{id}/views?window=` | View count for one item |
| `PUT /v1/config/windows` | Configure window types, lateness and K |
| `POST /v1/backfill` | Recompute buckets from raw events for a time range |

**Table / key responsibilities**

| Table / key | Responsibility |
| --- | --- |
| `view_event` | Immutable raw event stream; source of truth for recomputation |
| `content_metadata` | Titles and categories joined at serve time |
| `content_view_bucket` | Pre-aggregated counts per window, content and dimension in ClickHouse |
| `topk:{window}:{start}` | Serving structure for fixed-window top-K |
| `topk_snapshot` | Immutable published rankings for audit and fallback |
| `dedupe_state` | Drops duplicate events during aggregation |

---

## 17. Typeahead / Autocomplete

**Storage:** A prefix index (trie, FST or search-engine completion suggester) built offline; Redis/CDN for hot prefixes; a canonical entity table; Kafka/CDC for updates.

**Access patterns:** (1) top K suggestions for a prefix in tens of milliseconds; (2) trending boosts; (3) per-user personalization overlay; (4) blocklist filtering.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `search_entity` | entity\_id, entity\_type, display\_text, normalized\_text, popularity, status | Canonical source; IDX(normalized\_text) for offline builds |
| `prefix_suggestion` | prefix, rank, entity\_id, display\_text, score, index\_version | PK(prefix, rank); limit prefix length (e.g. 1–10 chars) and drop prefixes below a frequency threshold to avoid index blow-up. Store top-K display strings inline so the read needs no join |
| `query_log` | event\_id, normalized\_query, user\_hash, event\_time, locale | Kafka → warehouse; strict retention and anonymization |
| `query_frequency` | normalized\_query, window\_start, count | Aggregated hourly/daily; feeds the next index build |
| `entity_synonym` | entity\_id, synonym | PK(entity\_id, synonym) |
| `suggestion_blocklist` | term\_or\_pattern, reason | Applied at build time and at serve time |

**Invariants:** normalize identically on write and read (case folding, accents, whitespace, tokenization). Index builds are versioned: build version N+1 offline, validate, then swap atomically (blue/green) instead of mutating live structures. A small real-time overlay handles breaking trends.

**Failure and scale:** Never scan the entity table per keystroke. The prefix is highly cacheable (CDN or Redis with short TTL). Shard the index by prefix range or hash with replicas; the client debounces and cancels stale requests. If the index is unavailable, serve cached prefixes or no suggestions rather than errors.

**Follow-ups:** typo tolerance (edit distance, n-grams), multi-language, personalization vs privacy, freshness SLOs.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/suggest?q=&limit=10&locale=` | Top suggestions for a prefix. Highly cacheable (CDN or Redis, short TTL). Target p99 under about 50 ms |
| `POST /v1/events/queries` | Log executed or selected queries for ranking (batched, anonymized) |
| `POST /v1/entities`, `PUT /v1/entities/{id}`, `DELETE /v1/entities/{id}` | Maintain canonical entities |
| `POST /v1/blocklist`, `DELETE /v1/blocklist/{id}` | Suppress terms |
| `POST /v1/index/builds` | Trigger an offline index build |
| `POST /v1/index/builds/{id}/promote` | Validate and atomically switch to the new version |
| `GET /v1/index/builds` | Build status and version history |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `search_entity` | Canonical items that can be suggested |
| `prefix_suggestion` | Precomputed top-K per prefix, versioned; the read path |
| `query_log` | Raw query events with strict retention |
| `query_frequency` | Aggregated counts that feed the next build |
| `entity_synonym` | Alternate forms mapped to an entity |
| `suggestion_blocklist` | Terms to exclude at build and serve time |

---

## 18. Stock Broker / Trading Platform

**Storage:** Strongly consistent relational store for orders, executions, positions and the ledger; Kafka for order and fill events; a separate market-data path (in-memory/time-series); immutable audit trail.

**Access patterns:** (1) place, amend and cancel orders; (2) pre-trade risk and buying-power check; (3) process executions from the exchange; (4) positions and balances; (5) order history and statements.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `trading_account` | account\_id, user\_id, status, version | Balance fields here are projections only; the ledger is authoritative |
| `instrument` | instrument\_id, symbol, exchange, tick\_size, lot\_size, currency, status | UQ(exchange, symbol) |
| `trade_order` (avoid the reserved word `order`) | order\_id, account\_id, instrument\_id, side, order\_type, quantity, limit\_price, tif, status, client\_order\_id, orig\_order\_id, version, created\_at | UQ(account\_id, client\_order\_id); IDX(account\_id, created\_at DESC); IDX(status) for open-order scans |
| `order_event` | order\_id, seq\_no, event\_type, payload, created\_at | PK(order\_id, seq\_no) — immutable audit trail; amend/cancel are events |
| `execution` | execution\_id, order\_id, exchange\_exec\_id, quantity, price, executed\_at | UQ(exchange\_exec\_id) — fills can be delivered twice |
| `cash_hold` | hold\_id, account\_id, order\_id, amount\_minor, status | Reserves buying power at order placement; released or converted on fill/cancel |
| `position` | account\_id, instrument\_id, quantity, avg\_cost, version | PK(account\_id, instrument\_id); projection of executions |
| `ledger_entry` | entry\_id, txn\_id, account\_id, amount\_minor, currency, reference\_type, reference\_id, created\_at | P5 — append-only, double-entry |
| `settlement` | settlement\_id, execution\_id, settle\_date, status | T+1 or T+2 cash/stock movement |
| `candle` | instrument\_id, interval, bucket\_start, open, high, low, close, volume | PK(instrument\_id, interval, bucket\_start); market data is not OLTP |

**Invariants:** (1) no order is accepted without sufficient buying power: take the hold with a conditional update on the account, serialized per account (single writer per account partition or row lock). (2) Order status moves only along the state machine NEW → VALIDATED → ROUTED → PARTIALLY\_FILLED → FILLED, with CANCELLED, REJECTED, EXPIRED exits (P9). (3) An execution, its position change and its ledger entries commit in one transaction and are idempotent via `exchange_exec_id`.

**Failure and scale:** Store prices and quantities as scaled integers or exact decimals, never floats. Reconcile broker records against exchange drop copies daily. If the exchange link drops, orders stay ROUTED/UNKNOWN until reconciled; never assume rejection. Shard by `account_id`; market data fan-out runs on its own infrastructure.

**Follow-ups:** corporate actions (splits, dividends), margin, order matching engine internals, regulatory audit and retention.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/orders` | Place an order. Body: instrument, side, type, quantity, limit\_price, tif, client\_order\_id. Runs risk and buying-power check; idempotent on (account, client\_order\_id) |
| `GET /v1/orders/{id}`, `GET /v1/orders?status=&from=` | Order status and history |
| `PATCH /v1/orders/{id}` | Amend price or quantity (recorded as events) |
| `DELETE /v1/orders/{id}` | Cancel; final state arrives from the exchange |
| `GET /v1/accounts/{id}/positions`, `/balances`, `/ledger` | Holdings, cash and buying power, ledger entries |
| `GET /v1/accounts/{id}/statements?month=` | Statements and tax documents |
| `POST /v1/funds/deposit`, `POST /v1/funds/withdraw` | Cash movements |
| `GET /v1/instruments/{symbol}`, `GET /v1/quotes/{symbol}`, `GET /v1/candles?symbol=&interval=` | Reference and market data |
| `WS /v1/stream` | Live quotes and order updates |
| `POST /internal/executions`, `POST /internal/order-acks` | Exchange gateway reports fills and acknowledgements (idempotent on exchange\_exec\_id) |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `trading_account` | Account status and balance projections |
| `instrument` | Tradable symbols with tick and lot sizes |
| `trade_order` | Current state of each order; state-machine guard |
| `order_event` | Immutable audit trail of every order change |
| `execution` | Each fill from the exchange; unique per exchange execution id |
| `cash_hold` | Buying power reserved by open orders |
| `position` | Quantity and cost per instrument, derived from executions |
| `ledger_entry` | Authoritative append-only double-entry money record |
| `settlement` | Tracks T+N cash and security settlement |
| `candle` | Aggregated OHLC market data |

---

## 19. Dropbox-like File Storage

**Storage:** Object storage for content blocks; PostgreSQL (sharded) for namespace, versions and permissions; Redis for metadata cache; a change journal plus notification channel for sync.

**Access patterns:** (1) list a folder; (2) upload a new version with only changed blocks; (3) download a version; (4) sync: what changed since my cursor; (5) share and permission checks; (6) restore from trash or history.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `namespace` | namespace\_id, owner\_id, type (PERSONAL/SHARED\_FOLDER), quota\_bytes | Unit of sharing and sync; shared folders are their own namespace |
| `node` (file or folder) | node\_id, namespace\_id, parent\_id, type, name, current\_version\_id, deleted\_at, updated\_at | Partial UQ(namespace\_id, parent\_id, name) WHERE deleted\_at IS NULL; IDX(parent\_id). Moving a folder = one `parent_id` update |
| `file_version` | version\_id, node\_id, version\_no, size\_bytes, content\_hash, created\_by, created\_at | UQ(node\_id, version\_no) |
| `block` | block\_hash, size\_bytes, object\_key, refcount or last\_referenced\_at | PK(block\_hash) — content-addressed; \~4 MB blocks (or content-defined chunking) |
| `version_block` | version\_id, block\_index, block\_hash | PK(version\_id, block\_index) — the manifest |
| `permission` | namespace\_id or node\_id, principal\_id, role | PK(resource\_id, principal\_id); inherit down the tree, resolved by walking parents or a materialized path |
| `change_log` | namespace\_id, seq, node\_id, op (CREATE/UPDATE/MOVE/DELETE), version\_id, created\_at | PK(namespace\_id, seq) — sync cursor reads `seq > cursor` |
| `device_cursor` | user\_id, device\_id, namespace\_id, last\_seq | PK(user\_id, device\_id, namespace\_id) |
| `upload_session` | session\_id, node\_id, base\_version\_id, blocks\_received, expires\_at | Resumable uploads; commit is conditional on `base_version_id` |

**Invariants:** names are unique within a live folder; a commit succeeds only if `base_version_id` still equals the current version, otherwise create a **conflict copy** (optimistic concurrency); change-log entries and metadata updates commit together so sync never misses a change.

**Failure and scale:** Clients upload missing blocks directly to object storage after a `which blocks exist?` check, then commit the manifest. Garbage-collect unreferenced blocks with a grace period (refcounts plus mark-and-sweep). Cross-user dedup can leak whether a block exists; scope dedup per account or require proof of possession for sensitive tenants. Keep trash and old versions for a retention window.

**Follow-ups:** delta sync, large-file resumability, end-to-end encryption vs dedup, selective sync, team folders.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/folders/{id}/children?cursor=` | List a folder |
| `POST /v1/folders` | Create a folder; unique name per parent |
| `GET /v1/files/{id}` | File metadata and current version |
| `POST /v1/uploads` | Start an upload with the block hash manifest; returns which blocks are missing and presigned URLs |
| `PUT <presigned-url>` | Client uploads missing blocks straight to object storage |
| `POST /v1/uploads/{session}/commit` | Body: base\_version\_id. Creates the new version or returns a conflict |
| `GET /v1/files/{id}/download?version=` | Manifest and block URLs |
| `GET /v1/files/{id}/versions`, `POST /v1/files/{id}/versions/{vid}/restore` | History and restore |
| `POST /v1/nodes/{id}/move`, `DELETE /v1/nodes/{id}`, `POST /v1/nodes/{id}/restore` | Move, trash, undelete |
| `POST /v1/nodes/{id}/share`, `GET/DELETE /v1/nodes/{id}/permissions` | Sharing |
| `GET /v1/changes?cursor=` and `GET /v1/changes/wait?cursor=` | Incremental sync and long-poll for new changes |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `namespace` | Unit of storage, sharing and sync |
| `node` | Files and folders in a tree; unique live name per parent |
| `file_version` | Immutable version records |
| `block` | Content-addressed storage references with refcounts for GC |
| `version_block` | Ordered block manifest of each version |
| `permission` | Access control on namespaces and nodes |
| `change_log` | Ordered change feed that drives sync |
| `device_cursor` | Last change each device has applied |
| `upload_session` | Resumable upload state and conflict detection |

---

## 20. Live Comments on Broadcast

**Storage:** WebSocket gateways for fan-out; Kafka for ingest and buffering; Redis for rate limits, sequence counters and recent comments; Cassandra or object storage for replay and moderation history.

**Access patterns:** (1) post a comment; (2) stream new comments to every viewer with low latency; (3) load the last N comments on join; (4) replay comments aligned to video time; (5) moderate and delete.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `broadcast` | broadcast\_id, creator\_id, starts\_at, ended\_at, status, slow\_mode\_ms | IDX(status, starts\_at) |
| `comment` | broadcast\_id, minute\_bucket, comment\_id (time-sortable), user\_id, offset\_ms, body, moderation\_status | Cassandra PK((broadcast\_id, minute\_bucket), comment\_id); `offset_ms` = time since broadcast start for replay |
| `comment_recent` (Redis) | broadcast\_id, comment\_id, body | Capped list or ZSET per broadcast for the join snapshot |
| `comment_reaction_agg` | broadcast\_id, window\_start, reaction\_type, count | Windowed aggregation — do not write one row per reaction at scale |
| `comment_moderation` | moderation\_id, comment\_id, action, moderator\_id, reason, created\_at | Audit; a delete is also published as an event so clients remove it |
| `connection_registry` (in memory) | broadcast\_id → set of connection ids per gateway | Gateway-local subscription tables plus pub/sub between gateways |
| `comment_counter` | broadcast\_id, latest\_seq, total | Redis INCR, or use the Kafka partition offset (partition by `broadcast_id`) as the sequence |

**Invariants:** ordering is per broadcast (partition key = `broadcast_id`); spam and slow-mode checks run before accept; deletes are events, never silent mutations.

**Failure and scale:** At peak, not every viewer needs every comment: **sample or batch** (send up to N comments per tick), coalesce reactions into counts, and shed load by dropping low-priority traffic. Gateways batch writes to sockets; a hot broadcast gets its own pub/sub shard and a tree of relay gateways. Persisting accepted comments asynchronously is fine; losing a few during overload is usually acceptable (state the policy).

**Follow-ups:** geographic fan-out, moderation latency, replay storage cost, creator-only or follower-only modes.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/broadcasts`, `POST /v1/broadcasts/{id}/end` | Create and end a broadcast |
| `POST /v1/broadcasts/{id}/comments` | Post a comment; rate limit, slow mode and spam checks; returns comment\_id and sequence |
| `WS /v1/broadcasts/{id}/stream` | Subscribe; receives batched comments, reaction counts and delete events |
| `GET /v1/broadcasts/{id}/comments?before=&limit=` | Recent comments on join |
| `GET /v1/broadcasts/{id}/replay?from_ms=&to_ms=` | Replay comments aligned to video time |
| `POST /v1/comments/{id}/reactions` | React to a comment (aggregated) |
| `DELETE /v1/comments/{id}` | Moderator delete; published as an event |
| `POST /v1/broadcasts/{id}/slow-mode` | Set slow-mode interval |
| `POST /v1/users/{id}/mute`, `POST /v1/comments/{id}/report` | Moderation actions |

**Table / key responsibilities**

| Table / key | Responsibility |
| --- | --- |
| `broadcast` | Stream identity, status and slow-mode setting |
| `comment` | Durable comment history with replay offset |
| `comment_recent` | Fast join snapshot of the latest comments |
| `comment_reaction_agg` | Windowed reaction counts instead of one row per reaction |
| `comment_moderation` | Audit trail of moderator actions |
| `connection_registry` | Gateway-local map from broadcast to connections |
| `comment_counter` | Sequence generator per broadcast |

---

## 21. Auction / Bidding

**Storage:** PostgreSQL for auction state and bid correctness; Redis for a current-price cache; Kafka for bid and outcome events and notifications.

**Access patterns:** (1) place a bid; (2) current price and bid history; (3) auctions ending soon; (4) close an auction exactly once; (5) a user's bids and watchlist.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `auction` | auction\_id, seller\_id, item\_id, starts\_at, ends\_at, status, start\_price\_minor, reserve\_minor, min\_increment\_minor, currency, version | IDX(status, ends\_at) — closing scan and 'ending soon' |
| `bid` | bid\_id, auction\_id, bidder\_id, amount\_minor, max\_amount\_minor (proxy bids), created\_at, idempotency\_key | UQ(bidder\_id, idempotency\_key); IDX(auction\_id, amount\_minor DESC, created\_at) |
| `auction_leader` | auction\_id, bid\_id, bidder\_id, current\_amount\_minor, bid\_count, version | PK(auction\_id) — serialization point |
| `bid_event` | event\_id, auction\_id, bid\_id, event\_type, created\_at | Outbox / event log (P3) |
| `auction_winner` | auction\_id, bid\_id, bidder\_id, final\_amount\_minor, decided\_at | **PK(auction\_id)** — one winner per auction |
| `watchlist` | user\_id, auction\_id | PK(user\_id, auction\_id); IDX(auction\_id) for notifications |

**Invariant: bids are accepted in a serial order per auction, and only if they beat the current price by the minimum increment.**

```sql
BEGIN;
UPDATE auction_leader
   SET bid_id = :b, bidder_id = :u, current_amount_minor = :amt,
       bid_count = bid_count + 1, version = version + 1
 WHERE auction_id = :a AND current_amount_minor + :min_inc <= :amt
   AND EXISTS (SELECT 1 FROM auction WHERE auction_id = :a
               AND status = 'OPEN' AND now() < ends_at);   -- server clock only
-- rowcount 1 => INSERT bid + bid_event, COMMIT; rowcount 0 => reject (outbid or closed)
```

**Failure and scale:** Use the **server** clock only. Anti-sniping extends `ends_at` when a bid lands in the final window, so the closing job must re-read `ends_at` before closing (or reschedule). Closing inserts `auction_winner` idempotently (PK) and triggers payment authorization. Ties resolve by `(amount, created_at, bid_id)`. Per-auction hot rows are fine: serialization per auction is the point; shard by `auction_id`. Redis holds the price for display only.

**Follow-ups:** proxy (max) bidding, reserve price semantics, buy-it-now, fraud and shill bidding detection.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/auctions` | Seller creates an auction (item, start price, reserve, increment, schedule) |
| `GET /v1/auctions?status=&category=&ending_before=` | Browse and ending-soon lists |
| `GET /v1/auctions/{id}` | Current price, leader (masked), end time, bid count |
| `POST /v1/auctions/{id}/bids` | Body: amount (or max for proxy bidding). Idempotency-Key. Returns accepted or outbid; 409 if below the minimum or closed |
| `GET /v1/auctions/{id}/bids` | Bid history |
| `WS /v1/auctions/{id}/stream` | Live price and end-time updates |
| `PUT /v1/auctions/{id}/watch`, `DELETE ...` | Watchlist and notifications |
| `POST /v1/auctions/{id}/cancel` | Seller cancel, restricted by rules |
| `GET /v1/users/me/bids` | The caller's bids and statuses |
| `POST /v1/auctions/{id}/pay` | Winner pays |
| `POST /internal/auctions/{id}/close` | Idempotent close; re-checks `ends_at` |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `auction` | Definition, schedule, rules and lifecycle status |
| `bid` | Every accepted bid with idempotency key |
| `auction_leader` | Current winning bid; the serialization point for new bids |
| `bid_event` | Event log for notifications and audit |
| `auction_winner` | Exactly one winner per auction; idempotent close |
| `watchlist` | Who to notify about an auction |

---

## 22. News Aggregator

**Storage:** Relational/document store for publishers, feeds and article metadata; a fetch queue; a search index for full text; object storage for extracted content and images; Redis for trending and hot feeds.

**Access patterns:** (1) ingest new articles from many feeds on adaptive schedules; (2) personalized feed by topics and sources; (3) topic and keyword search; (4) show one story with multiple sources; (5) trending.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `publisher` | publisher\_id, name, domain, crawl\_policy, trust\_score | UQ(domain) |
| `feed` | feed\_id, publisher\_id, feed\_url, etag, last\_modified, last\_fetched\_at, next\_fetch\_at, poll\_interval\_s | UQ(feed\_url); IDX(next\_fetch\_at) — scheduler pulls due feeds. Use conditional GET (ETag / Last-Modified) and adapt the interval to the publishing rate |
| `article` | article\_id, publisher\_id, canonical\_url, url\_hash, title, summary, content\_object\_key, content\_fingerprint, cluster\_id, published\_at, discovered\_at | UQ(url\_hash); IDX(published\_at DESC); IDX(cluster\_id) |
| `story_cluster` | cluster\_id, representative\_article\_id, topic\_id, first\_seen\_at, article\_count | Groups near-duplicate articles (SimHash/MinHash similarity) |
| `article_topic` | article\_id, topic\_id, confidence | PK(article\_id, topic\_id); IDX(topic\_id, confidence) |
| `user_subscription` | user\_id, source\_type (PUBLISHER/TOPIC), source\_id, created\_at | PK(user\_id, source\_type, source\_id) |
| `user_read_state` | user\_id, article\_id, read\_at, saved | PK(user\_id, article\_id) |
| `article_rank_snapshot` | region, window, rank, cluster\_id, score, computed\_at | PK(region, window, rank) — trending projection |

**Invariants:** dedupe by canonical URL first, then by content fingerprint into a cluster; ingestion is idempotent (`url_hash` unique) so refetching a feed never creates duplicates; the search index is derived from the DB.

**Failure and scale:** Respect robots.txt and per-host politeness; back off on errors. Store a snippet and link (not full text) where licensing requires it. Ranking blends freshness decay, source trust and engagement. Archive old article bodies to cheaper storage; keep metadata.

**Follow-ups:** paywalls, personalization cold start, breaking-news fast path, language detection.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/feed?topics=&cursor=` | Personalized feed of story clusters |
| `GET /v1/articles/{id}` | Article summary, source link, cluster |
| `GET /v1/stories/{clusterId}` | All sources for one story |
| `GET /v1/search?q=&from=&to=&topic=` | Full-text search |
| `GET /v1/trending?region=` | Trending clusters from the rank snapshot |
| `PUT /v1/subscriptions`, `DELETE /v1/subscriptions` | Follow publishers or topics |
| `POST /v1/articles/{id}/read`, `POST /v1/articles/{id}/save`, `GET /v1/saved` | Read state and saved list |
| `POST /v1/publishers`, `POST /v1/feeds` | Register sources (admin) |
| `PUT /v1/feeds/{id}` | Change poll interval or disable |
| `POST /v1/feeds/{id}/fetch-now` | Force an immediate fetch |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `publisher` | Source identity, crawl policy and trust score |
| `feed` | Feed URL, conditional-GET state and next fetch time |
| `article` | Canonical article record, deduped by URL hash and fingerprint |
| `story_cluster` | Groups near-duplicate articles into one story |
| `article_topic` | Topic labels with confidence |
| `user_subscription` | What each user follows |
| `user_read_state` | Read and saved flags per user |
| `article_rank_snapshot` | Precomputed trending ranking |

---

## 23. Web Crawler

**Storage:** Relational/config store for policy and URL metadata; a distributed frontier (queues partitioned by host); object storage for fetched pages; search/link-graph pipelines downstream.

**Access patterns:** (1) pick the next URLs to fetch while honoring per-host politeness; (2) mark URLs seen and avoid duplicates; (3) store content and extract links; (4) re-crawl by change frequency; (5) robots.txt lookup.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `crawl_seed` | seed\_id, url, priority, enabled | IDX(enabled, priority) |
| `url_frontier` | url\_hash (64–128-bit hash of the normalized URL), normalized\_url, host, priority, depth, next\_fetch\_at, status, retry\_count | PK(url\_hash); IDX(host, next\_fetch\_at); frontier is **sharded by hash(host)** so one worker owns a host's politeness |
| `crawl_lease` | url\_hash, worker\_id, lease\_until | Lease-based claiming (P4); expired leases return to the queue |
| `robots_policy` | host, robots\_body, crawl\_delay\_ms, fetched\_at, expires\_at | PK(host); cached \~24 h |
| `host_crawl_state` | host, next\_allowed\_at, in\_flight, failure\_count, backoff\_until | PK(host) — the politeness gate |
| `fetch_result` | url\_hash, fetched\_at, http\_status, content\_hash, object\_key, content\_type, content\_length | Partition by date; IDX(url\_hash, fetched\_at DESC); bodies live in object storage |
| `link_edge` | src\_url\_hash, dst\_url\_hash, discovered\_at | Wide-column / columnar store for graph jobs (PageRank), not OLTP |
| `seen_set` | url\_hash | Bloom filter in memory in front of the persistent store (false positives skip a URL; false negatives never happen) |

**Design details:** Mercator-style frontier = front queues by priority, back queues one-per-host, a heap keyed by `next_allowed_at`. Normalize URLs before hashing (lowercase host, drop fragments, sort query params, resolve relative links). Detect traps with depth limits and per-host URL budgets. Detect duplicate content with SimHash on `content_hash` families.

**Invariants:** a URL is enqueued once (unique hash); at most one in-flight fetch per host unless the crawl delay allows more; each fetch is idempotent (same `url_hash` + `fetched_at` bucket).

**Failure and scale:** Workers can crash mid-fetch: leases expire and the URL is retried with exponential backoff. Re-crawl scheduling is adaptive: compare `content_hash`, shorten the interval for pages that change, lengthen it for pages that do not. Cache DNS.

**Follow-ups:** JS-rendered pages, deduplication at web scale, priority via PageRank or freshness, legal and ethical limits.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /internal/seeds` | Add seed URLs with priority |
| `POST /internal/frontier/urls` | Batch-enqueue discovered links; normalizes, hashes and dedupes (Bloom filter plus store) |
| `POST /internal/frontier/lease` | Body: worker\_id, n. Returns URLs whose host is allowed now, with lease\_until |
| `POST /internal/frontier/leases/{id}/complete` | Report fetch result and extracted links |
| `POST /internal/frontier/leases/{id}/fail` | Report failure; schedules retry with backoff |
| `GET /internal/robots/{host}` | Cached robots rules and crawl delay |
| `POST /internal/results` | Store fetch metadata and the object-storage key of the body |
| `GET /v1/urls/{hash}/status` | Crawl status and last fetch |
| `POST /v1/recrawl` | Force recrawl of a URL or pattern |
| `PUT /v1/hosts/{host}/policy` | Override crawl delay or block a host |
| `GET /v1/stats` | Throughput, errors and per-host rates |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `crawl_seed` | Starting points and their priority |
| `url_frontier` | Every known URL with schedule, status and retries; sharded by host |
| `crawl_lease` | Time-bound claims so crashed workers do not lose URLs |
| `robots_policy` | Cached robots.txt and crawl delay per host |
| `host_crawl_state` | Politeness gate: next allowed time, in-flight count, backoff |
| `fetch_result` | Metadata of each fetch and pointer to the stored body |
| `link_edge` | Link graph for ranking and analysis jobs |
| `seen_set` | Fast duplicate-URL check in front of the store |

---

## 24. Distributed Job Scheduler

**Storage:** PostgreSQL for job definitions and run state with transactional claims at moderate scale; partitioned scheduler shards with leases at large scale; Kafka for run events; object storage for logs and artifacts.

**Access patterns:** (1) generate runs at the right time for cron/interval/one-off jobs; (2) claim due runs without double execution; (3) heartbeat and recover from worker crashes; (4) retries with backoff; (5) dependencies and run history.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `job_definition` | job\_id, owner\_id, schedule\_type, cron\_expr, tz, payload\_ref, max\_retries, timeout\_s, catch\_up\_policy, enabled | IDX(owner\_id) |
| `job_schedule_cursor` | job\_id, next\_fire\_at, last\_enqueued\_at, version | PK(job\_id); IDX(next\_fire\_at) — advanced in the **same transaction** that inserts the run |
| `job_run` | run\_id, job\_id, scheduled\_for, status (READY/RUNNING/SUCCEEDED/FAILED/DEAD), priority, attempt\_no, run\_at, lease\_until, worker\_id, fencing\_token | **UQ(job\_id, scheduled\_for)** — one logical run per fire time; IDX(status, run\_at, priority) for the claim query |
| `job_attempt` | run\_id, attempt\_no, worker\_id, started\_at, finished\_at, outcome, error | PK(run\_id, attempt\_no) — history; v1 mixed attempts into the run uniqueness, which allowed concurrent duplicates |
| `run_dependency` | run\_id, depends\_on\_run\_id | PK(run\_id, depends\_on\_run\_id); run becomes READY when all dependencies SUCCEEDED |
| `worker` | worker\_id, region, capabilities, status, last\_heartbeat | IDX(status, last\_heartbeat) |
| `job_log_ref` | run\_id, attempt\_no, object\_key | Logs in object storage |

**Invariant: a scheduled occurrence runs to completion at most once concurrently, and is never silently skipped.** Schedule generation is atomic (cursor advance + run insert, unique `(job_id, scheduled_for)` as the backstop). Claiming uses P4 with `SKIP LOCKED`. Every claim increments a **fencing token**; result writes include it in the WHERE clause so a zombie worker whose lease expired cannot overwrite a newer attempt.

**Failure and scale:** Delivery is **at-least-once**, so handlers must be idempotent (give them `run_id` as the idempotency key). Retries use exponential backoff with jitter and land in DEAD after `max_retries` for alerting. Define missed-fire policy per job (skip, run once, run all). At scale, hash-partition jobs across scheduler shards, each owned by a leader via lease; keep a small in-memory timing wheel of the next few minutes per shard for sub-second accuracy.

**Follow-ups:** priorities and fairness between tenants, rate limits per worker pool, exactly-once illusions, DAG workflows.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/jobs` | Create a job (schedule, payload ref, retries, timeout, catch-up policy) |
| `GET /v1/jobs/{id}`, `PUT /v1/jobs/{id}`, `DELETE /v1/jobs/{id}` | Read, update, delete |
| `POST /v1/jobs/{id}/pause`, `/resume` | Stop and restart schedule generation |
| `POST /v1/jobs/{id}/trigger` | Run now (manual run) |
| `GET /v1/jobs/{id}/runs?cursor=` | Run history |
| `GET /v1/runs/{id}`, `GET /v1/runs/{id}/logs` | Run detail and logs from object storage |
| `POST /v1/runs/{id}/cancel`, `POST /v1/runs/{id}/retry` | Operator actions |
| `POST /v1/jobs/{id}/dependencies` | Declare upstream jobs |
| `POST /v1/workers/register`, `POST /v1/workers/{id}/claim` | Worker joins and claims due runs with a lease |
| `POST /v1/runs/{id}/heartbeat` | Extend the lease; body includes fencing\_token |
| `POST /v1/runs/{id}/complete` | Report outcome with fencing\_token; stale tokens are rejected |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `job_definition` | What to run, when, and retry and timeout policy |
| `job_schedule_cursor` | Next fire time; advanced atomically with run creation |
| `job_run` | One logical occurrence per fire time; state, lease and fencing token |
| `job_attempt` | History of each execution attempt |
| `run_dependency` | Gates a run on upstream runs |
| `worker` | Registry and health of executors |
| `job_log_ref` | Pointer to logs for each attempt |

---

## 25. Payment System

**Storage:** PostgreSQL (sharded) for payment state, idempotency records and the double-entry ledger; outbox → Kafka for asynchronous workflows; provider adapters; daily reconciliation.

**Access patterns:** (1) create a payment exactly once per client request; (2) call a provider and record the outcome; (3) handle webhooks and timeouts; (4) refunds; (5) merchant balances and settlement; (6) reconcile with provider reports.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `idempotency_record` | scope (merchant\_id), idem\_key, request\_hash, payment\_id, response\_payload, status, created\_at, expires\_at | PK(scope, idem\_key) — P1. Same key + different hash = reject |
| `payment` | payment\_id, merchant\_id, payer\_id, amount\_minor, currency, status (CREATED/PROCESSING/AUTHORIZED/CAPTURED/FAILED/IN\_DOUBT/REFUNDED), provider, provider\_ref, created\_at, updated\_at | IDX(merchant\_id, created\_at DESC); UQ(provider, provider\_ref) WHERE NOT NULL |
| `payment_attempt` | attempt\_id, payment\_id, provider, provider\_request\_id, status, response\_code, started\_at, completed\_at | UQ(provider, provider\_request\_id); IDX(payment\_id) |
| `provider_webhook_event` | provider, provider\_event\_id, payload, received\_at, processed\_at | PK(provider, provider\_event\_id) — webhook dedupe; process asynchronously |
| `ledger_account` | account\_id, owner\_type, owner\_id, currency, type (MERCHANT\_PAYABLE / CLEARING / FEES / REFUNDS), status | UQ(owner\_type, owner\_id, currency, type) |
| `ledger_entry` | entry\_id, txn\_id, payment\_id, account\_id, amount\_minor, currency, created\_at | P5: append-only, each `txn_id` sums to zero; IDX(account\_id, created\_at); IDX(payment\_id) |
| `ledger_balance` | account\_id, balance\_minor, version | Projection updated in the same transaction (or computed from entries) |
| `refund` | refund\_id, payment\_id, amount\_minor, status, provider\_ref, idempotency\_key | UQ(payment\_id, idempotency\_key); invariant: SUM(refunds) ≤ captured amount |
| `settlement_record` | settlement\_id, provider, provider\_ref, amount\_minor, currency, settlement\_date, reconciliation\_status | Matches provider truth to the internal ledger |
| `outbox` | event\_id, payment\_id, event\_type, payload, published\_at | P3 |

**Invariants:** (1) a client request produces at most one payment (idempotency gate); (2) the ledger always balances; (3) money is never created by mutation, refunds and reversals are new entries; (4) card numbers are never stored, only provider tokens (PCI scope reduction).

**Failure and scale:** A provider **timeout is IN\_DOUBT, not FAILED**: retry with the same provider request id or query the provider, and reconcile against settlement files. Use sagas for visible failures and reconciliation for unknown outcomes. Persist the attempt **before** calling the provider so a crash leaves evidence. Shard by `merchant_id` (watch hot merchants) or `payment_id`; keep idempotency records in the same shard as the payment so the check and the write share a transaction.

**Follow-ups:** authorize vs capture, multi-currency and FX, chargebacks and disputes, payouts, double-spend across providers.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `POST /v1/payments` | Create a payment. Body: amount, currency, payment method token, merchant. Idempotency-Key required; same key with a different body returns 422 |
| `GET /v1/payments/{id}` | Status (including IN\_DOUBT) and attempts |
| `POST /v1/payments/{id}/capture`, `POST /v1/payments/{id}/cancel` | Capture an authorization or void it |
| `POST /v1/payments/{id}/refunds` | Refund full or partial; Idempotency-Key; total refunds cannot exceed captured |
| `GET /v1/refunds/{id}` | Refund status |
| `GET /v1/payments?merchant=&from=&status=` | Search and reporting |
| `POST /v1/payment-methods` | Tokenize a method through the provider (no card data stored) |
| `POST /v1/webhooks/{provider}` | Provider events; verify signature, dedupe by event id, process asynchronously |
| `POST /v1/webhook-endpoints` | Merchant registers URLs for outbound events |
| `GET /v1/ledger/accounts/{id}/balance`, `GET /v1/ledger/entries` | Balances and entries |
| `GET /v1/settlements`, `POST /v1/settlements/reconcile` | Settlement records and reconciliation runs |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `idempotency_record` | Guarantees one result per client request and detects key reuse |
| `payment` | Lifecycle and state machine of each payment |
| `payment_attempt` | Every provider call, written before the call, for recovery |
| `provider_webhook_event` | Deduplicates and stores provider callbacks |
| `ledger_account` | Accounts that hold money positions (merchant, clearing, fees) |
| `ledger_entry` | Append-only double-entry record; each transaction sums to zero |
| `ledger_balance` | Fast balance projection |
| `refund` | Refund lifecycle with idempotency and cap check |
| `settlement_record` | Provider settlement data for reconciliation |
| `outbox` | Reliable downstream events |

---

## 26. Ad Click Aggregator

**Storage:** Kafka for click ingestion; Flink/Spark for event-time aggregation and dedupe; ClickHouse or a warehouse for analytics; a separate, auditable pipeline for billing-grade numbers.

**Access patterns:** (1) ingest clicks at very high rate; (2) clicks and impressions per ad / campaign / dimension per minute-hour-day; (3) near-real-time dashboards; (4) billable click counts with audit; (5) fraud filtering.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `ad_campaign` / `ad` | campaign\_id, advertiser\_id, budget\_minor, start\_at, end\_at, status / ad\_id, campaign\_id, creative\_ref, target\_url | IDX(advertiser\_id); IDX(campaign\_id) — OLTP reference data |
| `click_event` | click\_id (signed by the ad server), ad\_id, campaign\_id, user\_hash, event\_time, ingest\_time, placement, country, is\_valid | Kafka keyed by `ad_id` (+ salt for hot ads); raw table partitioned by date; dedupe on `click_id` |
| `impression_event` | impression\_id, ad\_id, campaign\_id, user\_hash, event\_time | Separate stream and table from clicks |
| `ad_metric_bucket` | ad\_id, metric\_type, bucket\_start (1 min), dimension\_key, value | ClickHouse (SummingMergeTree), ORDER BY (ad\_id, bucket\_start, dimension\_key); roll up to hour/day |
| `billing_ledger` | entry\_id, campaign\_id, click\_id, billable\_amount\_minor, currency, created\_at | UQ(click\_id) — idempotent billing reference; append-only |
| `fraud_flag` | click\_id, reason, score, flagged\_at | Applied before billing; invalid clicks are excluded, not deleted |

**Invariants:** a click is billed at most once (unique `click_id` in the ledger); aggregates are derived and reproducible from raw events; raw and billing-grade data are retained separately with auditability.

**Failure and scale:** Streaming gives fast approximate results; a **batch recompute from raw events** produces the final billing numbers (lambda-style correction). Exactly-once = replayable Kafka + idempotent sinks (upsert by `click_id`) + transactional or deduplicated writes. Allow late events up to a bound (e.g. 15–60 minutes) via watermarks; later arrivals go to a correction path. Hot ads are salted and merged.

**Follow-ups:** attribution windows, bot detection, budget pacing, unique-user estimation (HyperLogLog).

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /click?ad_id=&sig=&redirect=` | Validate signature, publish the click to Kafka, redirect immediately |
| `POST /v1/impressions` | Batch impression ingest |
| `GET /v1/metrics?ad_id=&campaign_id=&from=&to=&granularity=&group_by=` | Aggregates from ClickHouse |
| `GET /v1/campaigns/{id}/spend` | Near-real-time spend vs budget |
| `GET /v1/billing/campaigns/{id}/clicks?from=&to=` | Audited, billing-grade counts |
| `POST /v1/campaigns`, `PUT /v1/campaigns/{id}`, `POST /v1/ads` | Campaign and ad management |
| `POST /v1/fraud-rules` | Configure invalid-traffic filters |
| `POST /v1/reconciliation/runs` | Recompute aggregates from raw events and compare |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `ad_campaign` / `ad` | Reference data: budgets, schedule, creatives, targets |
| `click_event` | Immutable raw clicks; basis for recomputation |
| `impression_event` | Raw impressions kept separately from clicks |
| `ad_metric_bucket` | Pre-aggregated metrics for dashboards |
| `billing_ledger` | One billable entry per valid click; append-only |
| `fraud_flag` | Marks invalid clicks so they are excluded from billing |

---

## 27. Distributed Cache

**Storage:** A cache cluster (Redis/Memcached-like) with consistent hashing or hash slots; an optional control plane for membership; the durable origin remains the source of truth.

**Access patterns:** (1) get/set/delete by key in sub-millisecond; (2) absorb read load with a high hit ratio; (3) rebalance when nodes join or leave; (4) invalidate on writes; (5) survive node loss.

| Structure | Key fields | Notes |
| --- | --- | --- |
| `cache_entry` | key, value, expires\_at, version, last\_access, size | In memory; value carries the **entity version** so stale writes can be rejected |
| `cluster_node` | node\_id, address, zone, status, capacity | Control-plane membership (gossip or a coordination service) |
| `slot_map` | hash\_slot (0–16383), primary\_node, replica\_nodes, epoch | Or a consistent-hash ring with 100+ virtual nodes per server for even spread |
| `invalidation_event` | event\_id, cache\_key, entity\_version, created\_at | From CDC/outbox for write-driven invalidation |
| `cache_stats` | node\_id, bucket\_start, hits, misses, evictions, bytes | Metrics only, not on the hot path |

**Design choices to state:** cache-aside (default; app reads and fills), write-through (consistent, slower writes), write-behind (fast, risk of loss). Eviction: LRU/LFU with TTL and jitter. On writes, **delete** the key rather than update it, and prefer versioned values or CDC-driven invalidation to close the race where a stale read refills after the delete.

**Failure patterns and fixes:** *stampede* (many misses for one key) → single-flight/locking, stale-while-revalidate; *penetration* (misses for nonexistent keys) → negative caching or a Bloom filter; *avalanche* (mass expiry) → TTL jitter; *hot key* → local L1 cache or key replication; *node loss* → replicas plus a cold-start plan (the origin must survive the miss storm, so protect it with rate limiting).

**Follow-ups:** cluster resharding without downtime, consistency vs availability for the cache, multi-region invalidation, memory fragmentation.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET key` / `MGET keys` | Read one or many values; returns hit or miss |
| `SET key value [EX ttl] [NX]` | Write with TTL; NX supports locks and single-flight |
| `DEL key` | Invalidate |
| `EXPIRE key ttl`, `TTL key` | Manage lifetime |
| `INCR key` / `DECR key` | Atomic counters |
| `POST /v1/invalidate` | Control plane: invalidate by key list or tag with entity version |
| `POST /v1/nodes`, `DELETE /v1/nodes/{id}` | Add or remove a node |
| `GET /v1/cluster/slots` | Current slot map and epoch for client routing |
| `POST /v1/cluster/rebalance` | Move slots between nodes |
| `GET /v1/stats` | Hit ratio, evictions, memory per node |
| `PUT /v1/config` | Max memory and eviction policy |

**Structure responsibilities**

| Structure | Responsibility |
| --- | --- |
| `cache_entry` | Holds the cached value with TTL, version and access metadata |
| `cluster_node` | Membership and health of cache servers |
| `slot_map` | Routing from hash slot to primary and replicas, versioned by epoch |
| `invalidation_event` | Drives write-triggered invalidation from the source of truth |
| `cache_stats` | Operational metrics only |

---

## 28. Distributed Key-Value Store

**Storage:** A replicated, partitioned storage engine (LSM-tree with write-ahead log); a metadata/control plane for membership and placement; quorum (Dynamo-style) or consensus (Raft-style) depending on the consistency model.

**Access patterns:** (1) put/get/delete by key; (2) optional range scan within a partition; (3) add or remove nodes with minimal data movement; (4) repair divergent replicas; (5) backup and restore.

| Structure | Key fields | Notes |
| --- | --- | --- |
| `kv_record` | partition\_key, item\_key, value, version (timestamp or vector clock), tombstone, ttl\_at | Logical schema; placement = hash(partition\_key); item\_key orders items within a partition |
| `cluster_node` | node\_id, address, rack, zone, status, tokens | Membership via gossip or a coordination service |
| `token_range` | range\_start, range\_end, replica\_set, epoch | Consistent hashing with virtual nodes; epoch fences stale routers |
| `write_ahead_log` | log\_seq, partition\_key, item\_key, op, version | Durability before acknowledging; replayed on restart |
| `sstable_manifest` | sstable\_id, level, min\_key, max\_key, bloom\_ref, created\_at | LSM structure: memtable → SSTable → compaction |
| `hinted_handoff` | target\_node, partition\_key, item\_key, version, payload, expires\_at | Holds writes for an unavailable replica temporarily |
| `merkle_tree` | range, level, hash | Anti-entropy repair compares tree hashes per range |
| `snapshot_manifest` | snapshot\_id, node\_id, object\_key, created\_at | Backups; replication does not replace backups |

**Consistency contract first.** With N replicas, write quorum W and read quorum R: R + W > N gives read-your-writes on the quorum path; W=1/R=1 maximizes availability. Conflict resolution: last-write-wins by timestamp (simple, can lose writes) or vector clocks / CRDTs (keep siblings, merge). The consensus alternative (Raft per range) gives linearizability with leader-based writes and lower availability under partition.

**Failure handling:** sloppy quorum + hinted handoff keeps writes available; read repair fixes stale replicas on reads; Merkle-tree anti-entropy fixes cold data; tombstones persist for a grace period longer than the longest expected outage or deleted data can resurrect; bloom filters avoid disk reads for absent keys; compaction reclaims space and bounds read amplification.

**Follow-ups:** hot partitions, range queries, secondary indexes, multi-region replication and conflict policy.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `PUT /v1/kv/{partitionKey}/{itemKey}` | Write. Body: value, optional ttl, if\_version. Query: consistency (one, quorum, all). Returns version |
| `GET /v1/kv/{partitionKey}/{itemKey}?consistency=` | Read; may return multiple siblings if using vector clocks |
| `DELETE /v1/kv/{partitionKey}/{itemKey}` | Writes a tombstone |
| `GET /v1/kv/{partitionKey}?start=&end=&limit=` | Range scan within one partition |
| `POST /v1/kv/batch` | Multi-key operations within a partition |
| `POST /internal/replicate` | Coordinator to replica write |
| `POST /internal/hints` | Deliver hinted handoff data when a node returns |
| `GET /internal/merkle/{range}` and `POST /internal/repair` | Anti-entropy comparison and repair |
| `POST /v1/cluster/nodes`, `DELETE /v1/cluster/nodes/{id}`, `GET /v1/cluster/ring` | Membership and token ranges |
| `POST /v1/snapshots`, `POST /v1/snapshots/{id}/restore` | Backup and restore |

**Structure responsibilities**

| Structure | Responsibility |
| --- | --- |
| `kv_record` | The user data with version, tombstone and TTL |
| `cluster_node` | Membership and rack awareness |
| `token_range` | Placement of partitions on replicas, versioned by epoch |
| `write_ahead_log` | Durability before acknowledging writes |
| `sstable_manifest` | Tracks LSM files and levels for reads and compaction |
| `hinted_handoff` | Temporary storage of writes for down replicas |
| `merkle_tree` | Efficient detection of divergent replicas |
| `snapshot_manifest` | Backup inventory |

---

## 29. Google Maps-like Mapping

**Storage:** Object storage plus CDN for versioned map tiles; a graph store (in-memory, partitioned) for the road network; a geo index for places; Redis/NoSQL for live traffic; Kafka for the GPS ping stream.

**Access patterns:** (1) fetch tiles by zoom/x/y; (2) route between two points with traffic-aware ETA; (3) search places near a location; (4) ingest GPS probes and update speeds; (5) publish a new map version safely.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `map_feature` | feature\_id, feature\_type, geometry\_ref, attributes, map\_version | Canonical source data; tiles are built from it |
| `map_tile` | map\_version, zoom, tile\_x, tile\_y, object\_key, checksum | PK(map\_version, zoom, tile\_x, tile\_y); vector tiles mostly; served via CDN with long cache lifetimes because the version is in the URL |
| `road_node` | graph\_version, node\_id, lat, lon, region\_id | PK(graph\_version, node\_id) |
| `road_edge` | graph\_version, edge\_id, from\_node\_id, to\_node\_id, distance\_m, road\_class, free\_flow\_speed, restrictions | IDX(graph\_version, from\_node\_id) — directed edges; turn restrictions need edge-to-edge costs or expanded nodes |
| `road_shortcut` | graph\_version, shortcut\_id, from\_node\_id, to\_node\_id, via\_ref, base\_cost | Contraction hierarchies, built offline |
| `edge_speed_profile` | graph\_version, edge\_id, day\_type, time\_bucket, speed | Historical speeds by day type and time of day; ETA baseline |
| `traffic_segment_speed` | edge\_id, time\_bucket (e.g. 5 min), median\_speed, sample\_count, computed\_at | PK(edge\_id, time\_bucket); TTL for live data |
| `place` | place\_id, name, category, lat, lon, geo\_cell, address | Geo index (S2/H3/geohash cell or R-tree) plus text index |
| `gps_ping` | anonymous\_id, event\_time, lat, lon, speed, heading, geo\_cell | Kafka event; short retention; anonymize and rotate ids |

**Invariants:** map and graph data are **versioned and immutable**: build version N+1 offline, validate, then switch the pointer atomically. Traffic changes **edge weights, not topology**. Graph partitions by region with border nodes; long routes use the contraction hierarchy or hierarchical routing instead of plain Dijkstra over the whole graph.

**Failure and scale:** ETA = sum over edges of travel time from live speed (falling back to the historical profile, then to free-flow). GPS pings are map-matched to edges (HMM), aggregated per edge per bucket, and low-sample segments fall back to history. Never retain identifiable trails.

**Follow-ups:** rerouting in flight, incident ingestion, offline maps, transit and multimodal routing.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/tiles/{version}/{z}/{x}/{y}.pbf` | Map tile, CDN-cached with long TTL because the version is in the path |
| `POST /v1/routes` | Body: origin, destination, mode, depart\_at, avoid options. Returns route geometry, distance, ETA and alternatives |
| `GET /v1/routes/{id}/eta?position=` | Live re-estimation during navigation |
| `GET /v1/places/search?q=&lat=&lon=&radius=` | Place search |
| `GET /v1/places/{id}` | Place details |
| `GET /v1/geocode?address=`, `GET /v1/reverse-geocode?lat=&lon=` | Address and coordinate conversion |
| `POST /v1/probes` | Batch GPS pings from consenting devices |
| `GET /v1/traffic/tiles/{z}/{x}/{y}` and `GET /v1/traffic/segments?bbox=` | Traffic overlay |
| `POST /v1/map-versions`, `POST /v1/map-versions/{id}/promote` | Build and atomically publish a map or graph version |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `map_feature` | Canonical map data used to build tiles |
| `map_tile` | Pointer to each prebuilt tile per version |
| `road_node` / `road_edge` | The versioned road graph |
| `road_shortcut` | Precomputed shortcuts for fast long-distance routing |
| `edge_speed_profile` | Historical speeds by day and time for baseline ETA |
| `traffic_segment_speed` | Live speed per edge and time bucket |
| `place` | Searchable places with geo cell |
| `gps_ping` | Short-lived, anonymized probe input for traffic |

---

## 30. IRCTC / Train Booking

**Storage:** PostgreSQL for schedules, quotas, passengers and booking transactions; Redis for availability cache and temporary holds; Kafka/outbox for notifications and status updates.

**Access patterns:** (1) search trains between two stations on a date; (2) availability for a class and journey; (3) book with seat/berth allocation; (4) cancel and promote the waitlist; (5) PNR status.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `train` / `station` | train\_id, train\_no, name / station\_code, name, city | UQ(train\_no); PK(station\_code) |
| `train_run` | run\_id, train\_id, journey\_date, status | UQ(train\_id, journey\_date) |
| `train_stop` | train\_id, stop\_seq, station\_code, arrival\_time, departure\_time, day\_offset | PK(train\_id, stop\_seq); IDX(station\_code) — the search path from station to trains |
| `coach` / `seat` | coach\_id, run\_id, coach\_no, class\_type / seat\_id, coach\_id, berth\_no, berth\_type | IDX(run\_id, class\_type) |
| `seat_leg_inventory` | run\_id, seat\_id, leg\_no, quota, booking\_id (NULL = free), version | **PK(run\_id, seat\_id, leg\_no)** — `leg_no` = the segment between stop `i` and stop `i+1`. A booking from stop `a` to `b` claims legs `a ... b-1` of one seat |
| `booking` | pnr, user\_id, run\_id, from\_stop\_seq, to\_stop\_seq, class\_type, quota, status (CONFIRMED/RAC/WL/CANCELLED), total\_fare\_minor, idempotency\_key, created\_at | PK(pnr); IDX(user\_id, created\_at DESC); UQ(user\_id, idempotency\_key) |
| `passenger` | passenger\_id, pnr, name, age, gender, berth\_pref, seat\_id, status | IDX(pnr) |
| `waitlist_entry` | run\_id, class\_type, quota, seq\_no, pnr | PK(run\_id, class\_type, quota, seq\_no) — promoted FIFO on cancellation |
| `fare_rule` | train\_id, class\_type, distance\_from, distance\_to, base\_fare\_minor | Fare = distance slab + class + quota surcharges, snapshot into `booking` |
| `payment` / `outbox` | payment\_id, pnr, status, provider\_ref, idempotency\_key / event\_id, ... | P1, P3 |

**Invariant: a seat is allocated to at most one passenger on any leg.** v1 modelled one row per seat per run, which wrongly blocks the seat for the whole journey. Claim by legs, all-or-nothing:

```sql
-- find a seat that is free on every leg of the requested interval
SELECT seat_id FROM seat_leg_inventory
 WHERE run_id = :r AND quota = :q AND leg_no BETWEEN :a AND :b - 1 AND booking_id IS NULL
   AND seat_id IN (SELECT seat_id FROM seat WHERE class_type = :c)
 GROUP BY seat_id HAVING COUNT(*) = :b - :a LIMIT 1 FOR UPDATE SKIP LOCKED;
-- then UPDATE ... SET booking_id = :pnr WHERE run_id=:r AND seat_id=:s AND leg_no BETWEEN :a AND :b-1
--      AND booking_id IS NULL;  -- rowcount must equal (b - a)
```

**Failure and scale:** Availability shown on search is a cached, approximate count per (run, class, from, to); the claim above is authoritative. The opening-minute spike (e.g. Tatkal) needs admission control and a queue; shard by `run_id` so one booking touches one shard. On cancellation, free the legs and promote the first waitlist entry whose interval fits, in the same transaction. Hold seats during payment with an expiry (P7).

**Follow-ups:** quota rules (general, Tatkal, ladies, senior), RAC semantics, chart preparation, refunds by cancellation time.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/trains/search?from=&to=&date=` | Trains between stations with times and classes |
| `GET /v1/trains/{no}/schedule` | Stops and timings |
| `GET /v1/trains/{no}/availability?date=&from=&to=&class=&quota=` | Cached approximate availability (available, RAC, waitlist) |
| `GET /v1/fare?train=&from=&to=&class=&quota=` | Fare quote |
| `POST /v1/bookings` | Body: passengers, journey, class, quota. Idempotency-Key. Claims seat legs and holds them with an expiry; returns pnr and payment details, or a waitlist position |
| `POST /v1/bookings/{pnr}/pay` | Pay and confirm |
| `GET /v1/bookings/{pnr}` | PNR status |
| `POST /v1/bookings/{pnr}/cancel` | Cancel all or some passengers; frees legs and triggers waitlist promotion |
| `GET /v1/bookings?user=me` | Booking history |
| `GET /v1/trains/{no}/chart?date=` | Reservation chart after preparation |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `train` / `station` | Reference data |
| `train_run` | A train on a specific date |
| `train_stop` | Ordered stops; drives search and leg numbering |
| `coach` / `seat` | Physical layout per run |
| `seat_leg_inventory` | Per seat and per leg ownership; prevents overlap while allowing reuse on other legs |
| `booking` | The PNR and its lifecycle |
| `passenger` | Travellers, berth preference and allocated seat |
| `waitlist_entry` | Ordered waitlist per class and quota |
| `fare_rule` | Distance, class and quota fare computation |
| `payment` / `outbox` | Payment attempts and reliable notifications |

---

## 31. Flash Sale

**Storage:** Relational DB for orders, payments and final inventory truth; Redis atomic counters and queues for admission control and fast reservation; Kafka for asynchronous order processing; CDN for static product pages.

**Access patterns:** (1) millions of users hit one product at the same second; (2) admit a bounded number of buyers; (3) reserve stock atomically; (4) confirm payment within a window; (5) release unpaid stock.

| Table | Key columns | Keys / indexes / notes |
| --- | --- | --- |
| `sale_event` | sale\_id, product\_id, starts\_at, ends\_at, status, per\_user\_limit | IDX(status, starts\_at) |
| `sale_inventory` | sale\_id, sku, total\_qty, reserved\_qty, sold\_qty, version | PK(sale\_id, sku); **CHECK(reserved\_qty + sold\_qty <= total\_qty)**. For one extremely hot SKU split into N bucket rows (`bucket_no`) to spread row contention |
| `sale_admission` | sale\_id, user\_id, token, admitted\_at, expires\_at | UQ(sale\_id, user\_id); waiting-room token, random lottery or FIFO |
| `flash_order` | order\_id, sale\_id, user\_id, sku, quantity, status, idempotency\_key, created\_at | UQ(sale\_id, user\_id) enforces the per-user limit of one order; UQ(idempotency\_key); IDX(sale\_id, status) |
| `inventory_reservation` | reservation\_id, order\_id, sale\_id, sku, quantity, expires\_at, status | IDX(status, expires\_at); expiry returns stock |
| `payment` | payment\_id, order\_id, amount\_minor, status, provider\_ref, idempotency\_key | P1 |
| `inventory_event` | event\_id, sale\_id, sku, delta, reason, created\_at | Audit trail for reconciliation |

**Invariant: never sell more than `total_qty`, never let one user exceed the limit.** Two layers:

```lua
-- Redis (fast path, atomic): reserve only if stock remains and the user has not bought
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then return -1 end   -- per-user limit
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock >= tonumber(ARGV[2]) then
  redis.call('DECRBY', KEYS[1], ARGV[2]); redis.call('SADD', KEYS[2], ARGV[1]); return 1
end
return 0
```

The database is the **final authority**: the order write uses P2 plus the CHECK constraint, and a periodic job reconciles Redis counters against durable inventory.

**Failure and scale:** Protect the backend first: serve the product page statically from the CDN, put a virtual waiting room in front, rate limit and bot-filter. Orders flow Redis reservation → Kafka → DB write, so the database sees only admitted traffic. Unpaid reservations expire after a payment window (e.g. 10 minutes) and `INCR` the stock back. If Redis restarts, rebuild counters from `sale_inventory` before reopening the sale.

**Follow-ups:** fairness and lottery, bot defense, pre-warming, partial refunds when reconciliation finds oversell.

**APIs**

| Endpoint | Purpose and key details |
| --- | --- |
| `GET /v1/sales/{id}` | Sale details; static and CDN-served |
| `POST /v1/sales/{id}/queue` | Join the waiting room; returns token and position |
| `GET /v1/sales/{id}/queue/status` | Poll or stream admission status |
| `POST /v1/sales/{id}/orders` | Body: sku, quantity. Needs the admission token and an Idempotency-Key. Redis atomic reserve, then 202 with order\_id |
| `GET /v1/orders/{id}` | Order status (pending, paid, expired) |
| `POST /v1/orders/{id}/payments` | Pay within the window |
| `POST /v1/orders/{id}/cancel` | Cancel and return stock |
| `GET /v1/sales/{id}/stock` | Approximate remaining stock for display |
| `POST /v1/sales` | Admin: create a sale |
| `POST /v1/sales/{id}/inventory/preload` | Load counters into Redis before the start |
| `POST /v1/sales/{id}/reconcile` | Compare Redis counters with durable inventory |
| `POST /v1/sales/{id}/close` | End the sale |

**Table responsibilities**

| Table | Responsibility |
| --- | --- |
| `sale_event` | Schedule, status and per-user limit |
| `sale_inventory` | Durable stock truth with a CHECK against overselling |
| `sale_admission` | Waiting-room tokens that cap traffic and enforce one entry per user |
| `flash_order` | Orders; unique per user and sale; idempotent |
| `inventory_reservation` | Time-boxed stock claims that expire and return stock |
| `payment` | Payment attempts and results |
| `inventory_event` | Audit trail for reconciliation |

---

# Part F — Cross-system interview checklist

| Concern | What to say / model |
| --- | --- |
| Source of truth | Name the durable owner of each business fact. Redis, Kafka, CDN and search indexes are usually not the canonical record |
| Idempotency | Persist an idempotency key with a unique constraint and a request hash; return the prior result on safe retry (P1) |
| Concurrency | Use conditional updates, optimistic versions, row locks, exclusion constraints or a single-writer partition for invariants such as seats, bids and inventory |
| Outbox / CDC | Commit business state and the outbox row in one transaction; publish asynchronously; consumers dedupe (P3) |
| Denormalization | Build query-specific projections for feeds, search, counters and timelines; make them rebuildable (P10) |
| Cassandra | One table per query; partition key first; clustering for ordered/range reads; bucket large histories; no joins; avoid unbounded partitions |
| Redis | Define key format, TTL, eviction, atomicity and failover behaviour; say what happens when it is down |
| Kafka | Partition key defines ordering scope; consumers tolerate retries and duplicates; monitor lag and DLQ |
| Search index | Derived; handle updates and deletes; enforce authorization after search |
| Time-series / events | Partition by time plus a high-cardinality entity key; define retention, late events, dedupe and rollups |
| Multi-region | State what is region-local, what replicates, the conflict policy, and whether writes are single-region or multi-writer |
| Privacy / security | Model ACL and tenant boundaries; avoid logging secrets or unnecessary personal location/activity data; plan deletion (GDPR) for derived stores too |

# Part G — Common anti-patterns (and the fix)

| Anti-pattern | Why it fails | Fix |
| --- | --- | --- |
| Relational schema presented as a Cassandra schema | Needs joins and unbounded scans | One table per query, partition key plus bucket |
| Redis as the only copy of a business fact | Eviction, failover and restarts lose data | DB authority, Redis as hold/cache/projection |
| Check-then-act in application code (`SELECT` then `UPDATE`) | Race between the two statements | Conditional `UPDATE ... WHERE` or a unique/exclusion constraint |
| Floats for money | Rounding errors | Integer minor units plus currency |
| Recomputing history from current data (price, address, title) | Old orders change | Snapshot at write time |
| Counters as `COUNT(*)` on hot paths | Slow and contended | Denormalized counter with periodic reconciliation |
| One global hot key (rate limiter, counter, auction row for a flash sale) | Single point of contention | Shard into N sub-keys and merge; or accept serialization where it is the invariant (per auction) |
| Deleting facts to correct them (ledger, audit) | Breaks audit and reconciliation | Append compensating entries |
| Treating a timeout as failure | Duplicate charges, lost money | IN\_DOUBT state, idempotent retry, reconcile |
| Unbounded partitions or lists (chat history, timelines) | Hot, huge partitions | Bucket by time or sequence; cap lists |
| Trusting search results for authorization | Index can be stale | Re-check ACL against the source after retrieval |

# Part H — Pacing a 45-minute interview

| Minutes | Do |
| --- | --- |
| 0–5 | Clarify scope, scale and the top 3 access patterns. Say your assumptions aloud |
| 5–15 | Draw the 4–6 core entities with keys and relationships. Name the critical invariant |
| 15–25 | Show the mechanism that protects the invariant (conditional update, unique constraint, lease, single writer) and the main query's index or partition key |
| 25–35 | Add read models (cache, search, timeline, aggregates) and storage boundaries. Explain how they are rebuilt |
| 35–45 | Failure modes, scaling the hot path, retention and privacy, then the interviewer's deep-dive. Do not draw every table in the first 5 minutes |

# Part I — Pattern-to-system index

| Pattern | Used in |
| --- | --- |
| P1 Idempotency gate | 01, 02, 03, 18, 21, 25, 30, 31 |
| P2 Conditional update | 02, 03, 05, 18, 21, 30, 31 |
| P3 Transactional outbox | 02, 03, 14, 25, 30 |
| P4 Lease-based claiming | 12, 14, 23, 24 |
| P5 Append-only ledger | 18, 25, 26 |
| P6 Bucketed wide-column partition | 04, 06, 08, 09, 20 |
| P7 Temporary hold, durable confirm | 02, 03, 10, 30, 31 |
| P8 Range exclusion | 10, 30 (alternative form) |
| P9 State-machine guard | 03, 05, 12, 18, 25 |
| P10 Rebuildable projection | 04, 09, 12, 15, 16, 17, 22 |

# Quick whiteboard template

For each system, draw in this order:

1. **Core entities:** the nouns from the requirements.
2. **Relationships:** 1:1, 1:N, M:N and ownership.
3. **Critical invariant:** e.g. one booking per seat, one idempotent payment result, ordered messages per conversation.
4. **Primary query:** the exact lookup, list, range, geo, feed or search path.
5. **Indexes / partitioning:** only those that serve those queries.
6. **Read model:** cache, search index, timeline, aggregate or materialized view.
7. **Failure recovery:** retry, dedupe, outbox, reconciliation, rebuild or compensation.

**Important:** start with the 4–6 core entities and expand only when the interviewer asks about a specific access pattern or correctness boundary.
