# Live Comments on a Live Broadcast: System Design

> Viewers type short comments, and every viewer watching the stream sees new comments in near real time.
> This document covers requirements, capacity estimation, the architecture, a step-by-step walkthrough of the write path and the read path, why each technology was chosen, and exactly how data is laid out in Cassandra, Redis and Kafka.
>
> The companion diagram is `live_comments_HLD.png`. Step IDs here (W1-W5, R1-R4, J1-J2) match the badges in that diagram.

---

## Table of Contents

1. [Requirements (FR and NFR)](#1-requirements)
2. [Capacity Estimation](#2-capacity-estimation)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Write Path, Step by Step](#4-write-path-step-by-step)
5. [Read Path, Step by Step](#5-read-path-step-by-step)
6. [Late Join and Reconnect Flow](#6-late-join-and-reconnect-flow)
7. [Moderation and Delete Flow](#7-moderation-and-delete-flow)
8. [Protocol Choices and Why](#8-protocol-choices-and-why)
9. [Data Storage Design](#9-data-storage-design)
10. [Sampling and Batching Algorithm](#10-sampling-and-batching-algorithm)
11. [Reliability and Failure Handling](#11-reliability-and-failure-handling)
12. [Multi-Region](#12-multi-region)
13. [Trade-offs Summary](#13-trade-offs-summary)
14. [Monitoring and SLOs](#14-monitoring-and-slos)
15. [Possible Extensions](#15-possible-extensions)

---

## 1. Requirements

### 1.1 Functional Requirements (FR)

| ID | Requirement |
|---|---|
| FR1 | A viewer can post a short text comment (max 200 characters) on a live stream. |
| FR2 | Every viewer of that stream sees new comments within about 1-2 seconds. |
| FR3 | A viewer who joins mid-stream immediately sees the most recent N comments (for example 50). |
| FR4 | A viewer who briefly disconnects resumes without losing the recent history. |
| FR5 | The platform blocks spam and abuse (rate limits, filters, bans). |
| FR6 | The broadcaster and moderators can delete comments, ban users, and set slow mode or followers-only mode. |
| FR7 | Comments are stored durably so they can be replayed in sync with the video later (VOD). |
| FR8 | A user's own comment appears on their screen instantly (optimistic UI). |

**Out of scope for this design:** threaded replies, rich media, DMs, and payments or paid comments (these are covered under extensions).

### 1.2 Non-Functional Requirements (NFR)

| ID | Requirement | Target |
|---|---|---|
| NFR1 | **Latency** (post to visible on another viewer's screen) | p50 < 500 ms, p99 < 2 s |
| NFR2 | **Availability** | 99.99% for delivery. Posting can degrade before delivery does. |
| NFR3 | **Scalability** | Horizontal scaling at every tier. Handle a single stream with 1M+ viewers. |
| NFR4 | **Consistency** | Eventual consistency is fine. Viewers may see slightly different orderings or samples. |
| NFR5 | **Durability** | An accepted comment must not be permanently lost (it is in Kafka and Cassandra). Live delivery itself may drop messages. |
| NFR6 | **Read-heavy, skewed load** | Reads (deliveries) outnumber writes by 3-6 orders of magnitude, and one stream can dominate traffic. |
| NFR7 | **Graceful degradation** | Under overload, reduce sampling rate and increase batch interval before failing. |
| NFR8 | **Safety** | Abuse must be controllable within seconds (delete propagation, bans, slow mode). |
| NFR9 | **Cost efficiency** | Bandwidth is the dominant cost, so sampling and batching are mandatory. |

---

## 2. Capacity Estimation

### 2.1 Assumptions

| Item | Assumption |
|---|---|
| Peak concurrent viewers (platform) | 10 million |
| Concurrent live streams (peak) | 100,000 |
| Hottest single stream | 1 million viewers, 5,000 comments/sec |
| Platform comment rate | avg 15,000/sec, peak 50,000/sec |
| Comment size (stored) | about 250 bytes (id, stream, user, name, text, timestamps, flags) |
| Comment size (on the wire to viewers) | about 150 bytes (minimal JSON) |
| Per-viewer delivery cap | 10 comments/sec (above this we sample) |
| Platform-wide avg delivered per viewer | 3 comments/sec |
| Average watch session | 20 minutes |
| Retention in Cassandra | 30 days hot (older archived to object storage) |

### 2.2 Write Throughput

- Peak writes: **50K comments/sec**.
- Average writes: **15K comments/sec**, so 15K x 86,400 = about **1.3 billion comments/day**.
- API ingress at peak: 50K x about 300 B (request incl. headers/metadata) = **about 15 MB/s**. This is small.

### 2.3 Why we cannot deliver everything (the key insight)

Hot stream, naive fan-out: 5,000 comments/s x 1,000,000 viewers = **5 billion deliveries/sec**, which is about 750 GB/s of egress. That is impossible, and pointless, because no human can read 5,000 comments per second.

With a per-viewer cap of 10 comments/sec:

- Hot stream: 1M x 10 = **10 million deliveries/sec**, about 1.5 GB/s, about **12 Gbps** egress for one stream (before compression).
- Platform: 10M viewers x 3/s = **30 million deliveries/sec**, about 4.5 GB/s, about **36 Gbps** average egress.

Batching (one HTTP/2 frame carries several comments) and compression cut this further. **Sampling plus batching reduces cost by orders of magnitude and the viewer experience is better.**

### 2.4 Connections and Gateway Fleet

- Concurrent SSE connections: **10 million**.
- Each gateway holds about 50K connections (about 10-20 KB buffer per connection, so about 0.5-1 GB RAM per gateway for sockets).
- Minimum gateways: 10M / 50K = **200**. Run at about 60% target utilization, so plan for **about 350-400 gateways**.
- Hot stream (1M viewers): 1M / 50K = 20 gateways minimum, so about **30** with headroom.
- Per gateway flush rate: 50K connections x about 2.5 flushes/sec (every 400 ms) = **about 125K socket writes/sec**. This is feasible with an epoll-based runtime (Netty, Go, Rust).

### 2.5 Kafka

| Metric | Estimate |
|---|---|
| Ingress at peak | 50K msg/s x 300 B = about 15 MB/s |
| With replication factor 3 | about 45 MB/s disk writes |
| Partitions | 64 for the main topic (hot streams further sub-sharded) |
| Retention | 3 days |
| Storage | avg 15K msg/s x 300 B = 4.5 MB/s, which is about 390 GB/day. Over 3 days is about 1.2 TB, and x3 RF is about **3.5 TB** |
| Fan-out workers | 10-20 consumers (CPU-light) |

### 2.6 Redis

**Recent comments cache:** keep the last 200 comments per live stream.

- 100,000 streams x 200 comments x 250 B = **about 5 GB**. With one replica, about 10 GB. Tiny.

**Pub/Sub:** the fan-out worker micro-batches comments for 100 ms per stream, then publishes one message per batch.

- Hot stream: at most 10 publishes/sec per stream, delivered to about 30 gateways = **about 300 deliveries/sec** (instead of 5K x 30 = 150K without batching).
- Platform: well under 100K deliveries/sec across the cluster.
- A **6-shard Redis Cluster** (each with a replica) is ample, sized for headroom and HA rather than throughput.

**Rate limiter and moderation state:** about 1M active posters x about 100 B = **about 100 MB**.

**Join traffic:**

- Joins: 10M viewers / 1,200 s = about 8.3K joins/sec on average, with **a peak of about 40K/sec** (a 5x spike when a big stream starts).
- Each join fetches 50 comments x 150 B = about 7.5 KB, so peak is about **300 MB/s** from Redis.
- Mitigation: cache the `/recent` response at the edge/CDN or gateway for 1-2 seconds. Then Redis sees about 1 request/sec per stream, not 40K/sec.

### 2.7 Cassandra

| Metric | Estimate |
|---|---|
| Write rate | 50K inserts/sec at peak |
| Row size | about 250 B |
| Per day | 1.3B x 250 B = about 324 GB |
| 30 days raw | about 9.7 TB |
| With RF=3 | about **29 TB** |
| Nodes (per region) | at 10K writes/s per node, about 5 nodes for write throughput. For storage at about 2 TB per node, about 15 nodes. Plan **18 nodes (3 racks x 6)** |

### 2.8 Latency Budget (target p50 about 400 ms, p99 under 1.5 s)

| Hop | Typical time |
|---|---|
| Client to edge to API Gateway | 30-50 ms |
| Comment Service (auth, rate limit, filter) | 5-15 ms |
| Kafka produce and ack | 5-10 ms |
| Fan-out worker consume | 5-20 ms |
| Micro-batch wait at fan-out | up to 100 ms |
| Redis Pub/Sub | 1-3 ms |
| Gateway batch wait | up to 250-500 ms (average about half) |
| Network to viewer | 30-80 ms |
| **Total** | **p50 about 300-450 ms, p99 under 1.5 s** |

The batching waits dominate the budget. They are a deliberate trade of a few hundred milliseconds for a large drop in packets, syscalls and bandwidth.

---

## 3. High-Level Architecture

```
                       WRITE PATH  (HTTPS POST, stateless)
 ┌────────┐ W1 ┌───────────┐ W2 ┌─────────────────┐  W3   ┌──────────────────┐
 │ Viewer │───►│ API GW +  │───►│ Comment Service │──────►│                  │
 │(Poster)│    │ L7 LB     │    │ validate/filter │       │      KAFKA       │
 └────────┘    └───────────┘    │ Snowflake ID    │       │  topic: comments │
      ▲                         └───┬─────────┬───┘       │  key = stream_id │
      │ optimistic render           │         │           │  (SHARED by both │
      │ (instant)             ┌─────▼───┐ ┌───▼──────┐    │   paths)         │
      │                       │ Rate    │ │Moderation│    └───┬──────────┬───┘
      │                       │ limiter │ │sync + ML │    W4  │          │ R1
      │                       │ (Redis) │ └──────────┘        ▼          ▼
      │                       └─────────┘             ┌─────────────┐ ┌────────────┐
      │                                               │ Persistence │ │ Fan-out    │
      │                                               │ Writer      │ │ Workers    │
      │                                               └──────┬──────┘ └──┬──────┬──┘
      │                                                   W5 │        R2 │      │ J2
      │                                                      ▼           ▼      ▼
      │                                               ┌───────────┐ ┌─────────┐ ┌─────────────┐
      │                                               │ Cassandra │ │ Redis   │ │ Redis recent│
      │                                               │ /DynamoDB │ │ Pub/Sub │ │ cache (ZSET)│
      │                                               └───────────┘ └────┬────┘ └──────▲──────┘
      │                                                                R3 │ SUBSCRIBE   │
      │                                                                    ▼             │ J1
      │            R4  SSE over HTTP/2 (batched, sampled)           ┌───────────────┐    │
      └─────────────────────────────────────────────────────────────│ Realtime      │    │
        Viewers (Watchers) ◄────────────────────────────────────────│ Gateways x N  │    │
                  │            READ PATH (long-lived SSE)           └───────────────┘    │
                  └──────── on join: GET /recent?limit=50 (HTTPS) ───────────────────────┘
```

**Design principle: CQRS.** The write path and read path are independent. Kafka is the only thing they share, and it is the durable source of truth. A spike in viewers cannot slow down posting, and a spike in posting cannot overload delivery.

| Component | Responsibility |
|---|---|
| **API Gateway / L7 LB** | TLS termination, authentication, per-IP limits, routing |
| **Comment Service** | Validation, rate limiting, filtering, ID generation, publish to Kafka |
| **Kafka** | Durable, ordered (per stream) event log. Decouples write from read. |
| **Persistence Writer** | Consumes Kafka and batch-writes to Cassandra |
| **Fan-out Workers** | Consume Kafka, micro-batch, publish to Redis Pub/Sub, and maintain the recent cache |
| **Redis Pub/Sub** | Instant, ephemeral broadcast to all gateways serving a stream |
| **Realtime Gateways** | Hold SSE connections, subscribe per stream, sample, batch, push to viewers |
| **Redis recent cache** | Last N comments per stream for joins and resumes |
| **Cassandra** | Durable history, replay, and audit |

---

## 4. Write Path, Step by Step

Goal: accept a comment safely, cheaply and durably, and tell the poster "got it" as fast as possible.

### W1. Client sends the comment (HTTPS POST)

```
POST /v1/streams/{stream_id}/comments
Authorization: Bearer <token>
{ "text": "Great goal!", "client_msg_id": "c-7f3a..." }
```

- `client_msg_id` is a client-generated unique ID, so retries are safe (idempotency).
- **Why HTTPS POST and not a WebSocket message?** Writes are infrequent per user (about 1 per few seconds). A stateless POST is easy to authenticate, rate-limit, load balance and scale on its own, without tying the write path to long-lived connections.

### W2. API Gateway authenticates and routes (REST/gRPC)

- Verifies the token, applies per-IP limits, and terminates TLS.
- Forwards to any stateless Comment Service instance.
- **Why:** keeps cross-cutting concerns (auth, TLS, coarse abuse limits) out of business logic.

### W2a. Comment Service runs its checks

In order, cheapest first:

1. **Validate:** length (at most 200 chars), non-empty, stream is live, user is not banned (Redis `SISMEMBER ban:{stream_id} user_id`).
2. **Stream modes:** slow mode, followers-only, or subscribers-only (Redis `HGETALL modes:{stream_id}`).
3. **Rate limit:** Redis token bucket per user (for example 1 comment per 1-2 seconds, small burst allowed).
4. **Dedup:** Redis `SET dedup:{user_id}:{client_msg_id} 1 NX EX 60`. If it already exists, return the original result (idempotent retry).
5. **Cheap synchronous filters:** blocklist, regex, link rules, repeated-character spam.
6. **Generate a Snowflake `comment_id`:** a 64-bit, time-sortable ID (timestamp + machine ID + sequence).

**Why moderate partly asynchronously?** Heavy ML moderation (toxicity models) would add 50-300 ms to every comment. So we publish immediately and let async ML retract bad comments via a DELETE event (see section 7). High-risk streams can switch to hold-and-review (pre-moderation), trading latency for safety.

### W3. Publish to Kafka (produce)

- Topic `comments.raw`, **key = `stream_id`**, so all comments of a stream land in the same partition and keep their order.
- Ack level `acks=all`, so the comment survives a broker loss.
- **Why Kafka?** It gives durability, replay, ordering per partition, and decoupling. The Persistence Writer and the Fan-out Workers consume independently, and either can fall behind or restart without affecting the other.
- **Hot stream mitigation:** a mega-stream's single partition can become a hotspot. For streams flagged hot, use key `hash(stream_id, shard_k)` with `k` in 0..7. Strict global order is not needed. Time-sortable IDs let consumers approximately merge.

### W3a. Return the response

- `202 Accepted` with `{comment_id}` as soon as Kafka acks. **Do not wait for Cassandra or fan-out.**
- The client has **already rendered its own comment optimistically** (FR8), so the poster sees zero latency. It reconciles the local item with the returned `comment_id`.

### W4. Persistence Writer consumes Kafka

- A consumer group reads `comments.raw` and groups writes into batches (for example 100-500 rows or every 50-100 ms).

### W5. Write to Cassandra (CQL)

- Inserts into `comments_by_stream` (section 9.1).
- Idempotent: re-processing the same `comment_id` just overwrites the same row, so Kafka replays are safe.
- **Why Cassandra?** Huge write throughput, linear horizontal scaling, time-ordered clustering, and a natural fit for "all comments of stream X in time range Y".
- This path is **off the live delivery path**. If Cassandra is slow, the live feed is unaffected.

---

## 5. Read Path, Step by Step

Goal: get each comment to every viewer of that stream quickly and cheaply, without drowning anyone.

### R1. Fan-out Workers consume Kafka

- A **separate consumer group** from the Persistence Writer, so each path scales and fails independently.
- Each comment is read **once**, no matter how many viewers exist.
- Workers apply any moderation state (skip comments from banned users or already-deleted IDs).

### R2. Publish to Redis Pub/Sub

- Workers **micro-batch for up to 100 ms per stream**, then run `PUBLISH live:{stream_id} <batch>`.
- **Why micro-batch here?** A 5K/s stream would otherwise cause 5K publishes/s, each delivered to about 30 gateways. Batching cuts that to at most 10 publishes/s.
- Workers also fill the recent cache (J2 below).

### R3. Gateways subscribe to the stream channel

- Each gateway **subscribes once per stream** (`SUBSCRIBE live:{stream_id}`) when its first local viewer of that stream connects, and unsubscribes when the last one leaves.
- A gateway keeps an in-memory map: `stream_id -> set of connections`.
- **Why this scales:** Redis delivers one message per gateway (about 30 for a 1M-viewer stream), not one per viewer (1M). The gateway then loops over its own local sockets. This **two-level fan-out** scales linearly with the number of gateways.

### R4. Gateways sample, batch and push to viewers (SSE over HTTP/2)

For each stream on each gateway:

1. Collect incoming comments into a buffer.
2. Every **250-500 ms**, build one batch:
   - If the buffer fits the per-viewer budget (for example 10/s), send everything.
   - Otherwise **sample** (section 10): keep priority items (broadcaster, moderators, verified users), randomly drop the rest, and report `dropped: N` so the client can show "high chat volume".
3. Serialize the batch **once**, then write the same bytes to all local connections.

```
id: 1850000000123456789
event: comments
data: {"c":[{"id":"185...","u":"u_42","n":"Alice","t":"Great goal!","ts":1729400000123}],"dropped":37}
```

- **Why SSE and not WebSocket?** Viewers only receive. SSE is simple, one-directional, auto-reconnects, supports `Last-Event-ID` resume natively, runs over standard HTTP/2, and passes through proxies and CDNs easily. Use WebSocket only if you add two-way features (presence, typing indicators, reactions) on the same channel.
- **Slow consumers:** if a client's send buffer fills up, the gateway drops messages for that client or disconnects it. One slow client must never stall the gateway.

---

## 6. Late Join and Reconnect Flow

### J1. Viewer joins (or reconnects)

1. Client calls `GET /v1/streams/{id}/comments/recent?limit=50` (HTTPS) and renders those comments immediately.
2. Client then opens `GET /v1/streams/{id}/comments/stream` (SSE).
3. On reconnect, the client sends `Last-Event-ID: <last comment_id seen>`.
4. The gateway replays anything newer from the recent cache (`ZRANGEBYSCORE`), then continues with live delivery. Because IDs are time-sortable, the client can also de-duplicate by ID where the two sources overlap.

### J2. Cache is filled by the fan-out workers

- On each batch, workers run `ZADD` and trim the sorted set (section 9.2) so joins and resumes never touch Cassandra.

**Join-storm protection (a big stream starts and 100K people join in seconds):**

- Cache the `/recent` response at the CDN or gateway for 1-2 seconds (same content for everyone).
- Clients reconnect with **jittered exponential backoff**, preventing a thundering herd after a gateway failure.

---

## 7. Moderation and Delete Flow

| Control | How it works |
|---|---|
| **Rate limits** | Per user and per IP (token bucket in Redis) |
| **Slow mode / followers-only / subs-only** | Per-stream flags in Redis `modes:{stream_id}`, checked in W2a |
| **Banned users** | Redis set `ban:{stream_id}` (plus a global set), also mirrored durably in Cassandra |
| **Keyword filters** | Per-stream and global blocklists, loaded into the Comment Service memory |
| **Async ML moderation** | A consumer on `comments.raw` scores each comment. If flagged, it emits a DELETE event |

**Delete propagation (M1-M5):**

1. **M1:** A moderator (or the ML consumer) calls `DELETE /v1/streams/{id}/comments/{comment_id}`.
2. **M2:** The service writes a delete event to Kafka topic `comments.moderation` (key = `stream_id`).
3. **M3:** Fan-out Workers publish `{"type":"delete","id":...}` on the same Pub/Sub channel, so gateways push it to clients, which remove the comment from the UI.
4. **M4:** The recent cache is updated (`ZRANGEBYSCORE` by exact score then `ZREM`).
5. **M5:** Cassandra gets a **soft delete**: `UPDATE ... SET status = 1` (not a real `DELETE`, to avoid tombstone buildup in hot partitions). Replays and VOD skip rows where `status != 0`.

**Why soft delete?** In Cassandra, real deletes create tombstones that slow reads and need careful handling. A normal column update is cheap, and it keeps an audit trail.

---

## 8. Protocol Choices and Why

| Hop | Protocol | Why | Alternatives considered |
|---|---|---|---|
| Client to server (post) | **HTTPS POST (HTTP/2)** | Stateless, easy to authenticate and rate limit, scales independently of readers | WebSocket (stateful, couples write and read scaling) |
| Server to client (live) | **SSE over HTTP/2** | One-way, auto-reconnect, `Last-Event-ID` resume, CDN/proxy friendly | WebSocket (more features, harder through some LBs); long polling (reconnect overhead); polling (high latency or load) |
| Gateway to Comment Service | **REST / gRPC** | Standard internal RPC with low overhead | - |
| Service to Kafka | **Kafka produce** (`acks=all`) | Durable, ordered per stream, replayable | Direct DB write (no decoupling, no replay) |
| Kafka to consumers | **Kafka consumer groups** | Independent scaling and failure of persistence vs fan-out | - |
| Fan-out to gateways | **Redis Pub/Sub** | Very low latency, trivial subscribe/unsubscribe for short-lived gateways. Message loss is acceptable for live chat. | Redis Streams (durable but heavier), Kafka direct (consumer-group overhead for ephemeral gateways), NATS |
| History | **Cassandra CQL** | Massive write throughput, time-range scans | DynamoDB (managed equivalent), a relational DB (does not scale for this write rate) |
| Recent cache | **Redis ZSET** | O(log N) inserts, range by score (for resume), cheap trimming | Redis List (cannot range by ID efficiently) |

**Why Redis Pub/Sub is safe here:** it is at-most-once and does not persist. That is fine because Kafka and Cassandra hold the durable copy, viewers are already seeing a sampled feed, and clients can recover missed comments via the recent cache and `Last-Event-ID`.

---

## 9. Data Storage Design

### 9.1 Cassandra

#### Main table: `comments_by_stream`

```sql
CREATE TABLE live.comments_by_stream (
    stream_id        bigint,
    bucket           int,        -- minute bucket: floor(epoch_seconds / 60)
    shard            smallint,   -- 0 for normal streams, 0..S-1 for hot streams
    comment_id       bigint,     -- Snowflake (time-sortable, unique)
    user_id          bigint,
    display_name     text,
    text             text,
    video_offset_ms  int,        -- offset from stream start, for replay sync
    status           tinyint,    -- 0 = visible, 1 = deleted, 2 = hidden (held by review)
    badges           set<text>,  -- e.g. {'mod','verified','sub'}
    created_at       timestamp,
    PRIMARY KEY ((stream_id, bucket, shard), comment_id)
) WITH CLUSTERING ORDER BY (comment_id ASC)
  AND default_time_to_live = 2592000          -- 30 days
  AND compaction = {
        'class': 'TimeWindowCompactionStrategy',
        'compaction_window_unit': 'HOURS',
        'compaction_window_size': 1
      }
  AND gc_grace_seconds = 86400;
```

**Why this design:**

| Choice | Reason |
|---|---|
| **Partition key = (stream_id, bucket, shard)** | A partition per stream per minute keeps partitions bounded. A single partition per stream would grow without limit and become a hotspot. |
| **Minute bucket** | Normal stream: 10 comments/s x 250 B = about 150 KB per minute partition (fine). Hot stream: 5K/s x 250 B x 60 = about **75 MB per minute**, which is too big, so we also add `shard`. |
| **`shard`** | For hot streams use S = 8 shards: about 9 MB per partition (within the recommended 10-100 MB). Shard = `hash(comment_id) % S`. S is stored in the stream metadata (Redis `stream:{id}:meta`, and DB). Readers fan out to S partitions in parallel and merge by `comment_id`. |
| **Clustering key = `comment_id` ASC** | Time-sortable, so "comments after X" is a cheap range scan. |
| **TWCS + 30-day TTL** | Data is write-once and time-ordered. TWCS drops whole expired SSTables cheaply, with no tombstone churn. |
| **`video_offset_ms`** | Lets VOD replay show comments in sync with the video. |
| **`status` column (soft delete)** | Avoids tombstones from real deletes. |

**Example rows:**

| stream_id | bucket | shard | comment_id | user_id | display_name | text | offset_ms | status |
|---|---|---|---|---|---|---|---|---|
| 9001 | 29490000 | 0 | 1850000000123456789 | 42 | Alice | Great goal! | 612345 | 0 |
| 9001 | 29490000 | 0 | 1850000000123499012 | 77 | Bob | LOL | 612401 | 0 |
| 9001 | 29490001 | 0 | 1850000000187700233 | 42 | Alice | Unreal | 672010 | 1 |

**Queries:**

```sql
-- Replay: comments for a time window (query each (bucket, shard) in parallel)
SELECT comment_id, user_id, display_name, text, video_offset_ms, badges
FROM live.comments_by_stream
WHERE stream_id = 9001 AND bucket = 29490000 AND shard = 0
  AND comment_id > 1850000000123456789
LIMIT 500;

-- Write (batched per partition by the Persistence Writer, idempotent upsert)
INSERT INTO live.comments_by_stream
  (stream_id, bucket, shard, comment_id, user_id, display_name, text,
   video_offset_ms, status, badges, created_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?);

-- Soft delete (moderation)
UPDATE live.comments_by_stream SET status = 1
WHERE stream_id = ? AND bucket = ? AND shard = ? AND comment_id = ?;
```

**Consistency levels:**

- Writes: `LOCAL_QUORUM` (durable within the region, tolerates one replica down).
- Replay reads: `LOCAL_ONE` (older data, speed matters more).
- Replication: RF = 3 per region (NetworkTopologyStrategy).

#### Supporting tables

```sql
-- Find all comments by a user in a stream (bulk delete when a user is banned)
CREATE TABLE live.comments_by_user_stream (
    stream_id   bigint,
    user_id     bigint,
    comment_id  bigint,
    bucket      int,
    shard       smallint,
    PRIMARY KEY ((stream_id, user_id), comment_id)
) WITH default_time_to_live = 2592000;

-- Durable ban list per stream (Redis holds the hot copy)
CREATE TABLE live.bans_by_stream (
    stream_id   bigint,
    user_id     bigint,
    banned_by   bigint,
    reason      text,
    banned_at   timestamp,
    expires_at  timestamp,
    PRIMARY KEY ((stream_id), user_id)
);

-- Per-stream settings (slow mode, shard count, etc.), also mirrored into Redis
CREATE TABLE live.stream_settings (
    stream_id        bigint PRIMARY KEY,
    shards           smallint,
    slow_mode_sec    int,
    followers_only   boolean,
    subs_only        boolean,
    started_at       timestamp
);
```

#### Archival

After 30 days the TTL removes rows from Cassandra. If long-term replay is needed, a batch job exports each finished stream to object storage (Parquet or JSONL) partitioned by `stream_id`, before the TTL expires.

---

### 9.2 Redis

Use **separate Redis deployments** so a problem in one cannot hurt another:

| Cluster | Contents | Eviction policy |
|---|---|---|
| **Cache + Pub/Sub cluster** | Recent comments, Pub/Sub channels | `volatile-ttl` or TTL-driven |
| **State cluster** | Rate limits, dedup keys, bans, stream modes | `noeviction` (state must not silently vanish) |

Key names use **hash tags** `{stream_id}` so keys of one stream live on the same shard in Redis Cluster.

#### 1. Recent comments: `recent:{<stream_id>}` (Sorted Set)

- **Score** = `comment_id` (Snowflake, so score order = time order).
- **Member** = compact JSON of the comment.

```text
ZADD   recent:{9001} 1850000000123456789 '{"id":"1850000000123456789","u":42,"n":"Alice","t":"Great goal!","ts":1729400000123,"b":["sub"]}'
ZREMRANGEBYRANK recent:{9001} 0 -201      # keep only the newest 200
EXPIRE recent:{9001} 3600                  # drop an hour after activity stops
```

| Operation | Command |
|---|---|
| Join (latest 50) | `ZREVRANGE recent:{9001} 0 49` |
| Resume after `Last-Event-ID` | `ZRANGEBYSCORE recent:{9001} (<lastId> +inf LIMIT 0 200` |
| Delete a comment | `ZRANGEBYSCORE recent:{9001} <cid> <cid>` then `ZREM recent:{9001} <member>` (score is unique, so this finds the exact member) |

**Why ZSET and not LIST?** A list cannot be queried "everything after ID X" efficiently. A sorted set gives O(log N) insert and range-by-score for resume, plus trivial trimming.

**Size:** 200 entries x about 250 B = about 50 KB per stream. 100K live streams is about 5 GB.

#### 2. Pub/Sub channel: `live:{<stream_id>}` (not stored)

```text
PUBLISH live:{9001} '{"c":[{...},{...}],"t":1729400000123}'    # one message per 100 ms batch
SUBSCRIBE live:{9001}                                          # done once per gateway per stream
```

- Nothing is persisted. Redis forwards the message to the currently connected subscribers.
- On Redis Cluster 7+, prefer **sharded Pub/Sub** (`SPUBLISH` / `SSUBSCRIBE`) so messages stay on the shard that owns the channel instead of being broadcast across the whole cluster.

#### 3. Rate limiting: `rl:{<user_id>}` (Hash, token bucket via a Lua script)

```text
HSET rl:{42} tokens 3 ts 1729400000123
EXPIRE rl:{42} 60
```

Atomic Lua script (pseudo):

```text
local data   = HMGET key tokens ts
local now    = ARGV[1]; local rate = ARGV[2]; local burst = ARGV[3]
tokens = min(burst, (data.tokens or burst) + (now - (data.ts or now)) * rate)
if tokens >= 1 then tokens = tokens - 1; allowed = 1 else allowed = 0 end
HSET key tokens tokens ts now ; EXPIRE key 60
return allowed
```

Other limit keys:

| Key | Type | Purpose |
|---|---|---|
| `rl:ip:{<ip>}` | Hash/String | Per-IP limit |
| `slow:{<stream_id>}:{<user_id>}` | String, `SET ... NX PX <slow_ms>` | Slow mode: one comment per N seconds per user in a stream |

#### 4. Idempotency: `dedup:{<user_id>}:<client_msg_id>` (String)

```text
SET dedup:{42}:c-7f3a 1850000000123456789 NX EX 60
```

- If `SET NX` fails, the comment was already accepted, so return the stored `comment_id` (use `GET`).

#### 5. Moderation state

| Key | Type | Example | Purpose |
|---|---|---|---|
| `ban:{<stream_id>}` | Set | `SADD ban:{9001} 666` | Banned users in this stream. Check with `SISMEMBER`. |
| `ban:global` | Set | `SADD ban:global 666` | Platform-wide bans |
| `modes:{<stream_id>}` | Hash | `HSET modes:{9001} slow 5 followers_only 1 subs_only 0` | Stream chat modes |
| `stream:{<stream_id>}:meta` | Hash | `HSET stream:{9001}:meta status live shards 8 started_at 1729390000` | Live status, hot-stream shard count |

All of these are **write-through**: the durable copy is in Cassandra, and Redis is the fast read path. On a Redis restart they are rebuilt from Cassandra.

#### 6. Gateway in-memory structures (not Redis)

```text
streams:   Map<stream_id, StreamState>
StreamState {
    connections : Set<Connection>        // local SSE sockets for this stream
    buffer      : List<Comment>          // comments collected since the last flush
    ring        : RingBuffer<Comment>    // last ~5-10 s, for quick Last-Event-ID resume
    subscribed  : boolean                // Redis subscription active?
}
```

---

### 9.3 Kafka

| Topic | Key | Value | Partitions | Retention |
|---|---|---|---|---|
| `comments.raw` | `stream_id` (or `stream_id,shard_k` for hot streams) | Comment event (Avro/Protobuf) | 64 | 3 days |
| `comments.moderation` | `stream_id` | Delete / ban event | 16 | 3 days |

**Comment event schema:**

```json
{
  "comment_id": 1850000000123456789,
  "stream_id": 9001,
  "user_id": 42,
  "display_name": "Alice",
  "text": "Great goal!",
  "badges": ["sub"],
  "client_msg_id": "c-7f3a...",
  "video_offset_ms": 612345,
  "created_at": 1729400000123
}
```

**Moderation event schema:**

```json
{ "type": "delete", "stream_id": 9001, "comment_id": 1850000000123456789, "by": 5, "reason": "spam" }
{ "type": "ban",    "stream_id": 9001, "user_id": 666, "by": 5, "expires_at": null }
```

Consumer groups:

| Group | Purpose |
|---|---|
| `persistence-writer` | Writes to Cassandra |
| `fanout-workers` | Publishes to Redis Pub/Sub and fills the recent cache |
| `ml-moderation` | Scores comments and emits delete events |
| `analytics` | Comment-rate and hype signals (optional) |

### 9.4 Where does each fact live?

| Data | Durable? | Store | Why there |
|---|---|---|---|
| Accepted comments (source of truth in motion) | Yes (3 days) | Kafka | Ordered, replayable, decouples consumers |
| Comment history for replay | Yes (30 days) | Cassandra | Write-heavy, time-range reads |
| Last N comments per stream | No (cache) | Redis ZSET | Fast joins and resumes |
| Live broadcast to gateways | No | Redis Pub/Sub | Lowest latency, ephemeral |
| Rate limits, dedup | No (short TTL) | Redis | Atomic counters, low latency |
| Bans, stream modes | Yes (mirrored) | Cassandra + Redis | Durable plus fast checks |
| Active connections | No | Gateway memory | Belongs to the process holding the socket |

---

## 10. Sampling and Batching Algorithm

**Goal:** every viewer gets a readable feed of at most about 10 comments/sec, regardless of how fast the chat is moving.

Every flush interval (250-500 ms) for each (gateway, stream):

```text
budget = per_viewer_rate * flush_interval            # e.g. 10/s * 0.4 s = 4 comments

incoming = drain(buffer)
if len(incoming) <= budget:
    batch = incoming; dropped = 0
else:
    priority = [c for c in incoming if c.is_broadcaster or c.is_mod or c.is_verified]
    rest     = [c for c in incoming if c not in priority]
    rest     = dedup_identical_text(rest)            # collapse "LOL LOL LOL" spam
    keep     = priority[:budget]
    keep    += random_sample(rest, budget - len(keep))   # or rank by reputation/engagement
    batch    = sort_by_comment_id(keep)
    dropped  = len(incoming) - len(batch)

payload = serialize_once(batch, dropped)             # one serialization for ALL viewers here
for conn in stream.connections:
    conn.write(payload)                              # non-blocking; drop if send buffer is full
```

**Key optimizations:**

- **Sample per gateway and stream, not per viewer.** One serialization and one sample is reused for all 50K local viewers, so CPU stays tiny. Different gateways produce different samples, which is acceptable.
- **Dedup identical spam** before sampling to make the visible feed more diverse.
- **Priority tiers** ensure the broadcaster and moderators are always visible.
- **Adaptive:** under high load, lengthen the flush interval or lower the budget (graceful degradation).

---

## 11. Reliability and Failure Handling

| Failure | Impact | Handling |
|---|---|---|
| **Gateway crashes** | Its viewers disconnect | Clients auto-reconnect with **jittered exponential backoff** to another gateway, then resume with `Last-Event-ID` from the recent cache |
| **Redis Pub/Sub node fails** | Brief gap in live delivery for some streams | Replica promotes. Gateways re-subscribe. Clients recover any gap on their next resume. Durable copy is intact. |
| **Redis recent cache lost** | Joins can't show history briefly | Fan-out workers repopulate within seconds from new traffic, or rebuild from Cassandra for the latest N |
| **Fan-out worker dies** | Consumer group rebalances | Another worker continues from the committed offset. A short delivery pause, no data loss. |
| **Persistence Writer lags or Cassandra slow** | History delayed | Live feed unaffected (independent consumer group). Kafka buffers for 3 days. Writes are idempotent, so catch-up is safe. |
| **Kafka broker fails** | Partition leader moves | `acks=all` plus RF=3 means no loss. A short produce stall during leader election. |
| **Comment Service fails** | Posting errors | Stateless, so the LB routes around it. Clients retry safely with the same `client_msg_id`. |
| **Hot stream overloads one partition** | Consumer lag for that stream | Sub-partition with `hash(stream_id, shard_k)` |
| **Slow client** | Gateway buffer growth | Drop messages for it, or disconnect it. Never block others. |
| **Traffic surge / overload** | Latency grows | Shed load: lower the sample rate, lengthen the batch interval, disable replay-on-join, and only then reject posts |
| **Poison message / bad payload** | Consumer stuck | Dead-letter queue topic after N retries |

**Delivery guarantee summary:**

- **Post accepted** means durable (Kafka `acks=all`).
- **Live delivery** is best-effort, at-most-once, and sampled.
- **History** is eventually complete in Cassandra (at-least-once with idempotent writes).

---

## 12. Multi-Region

- Viewers connect to the **nearest region** (GeoDNS or anycast) for lowest latency.
- A comment is written to the **local** region's Kafka and delivered locally right away.
- Kafka is replicated **asynchronously** to other regions (MirrorMaker 2 or Cluster Linking), where remote fan-out workers publish into their local Pub/Sub. Cross-region viewers see comments after a small extra delay (typically 100-300 ms).
- Cassandra uses NetworkTopologyStrategy with `LOCAL_QUORUM`. History is replicated across regions asynchronously.
- For a mega-event, a **regional aggregator tier** can sit between the central fan-out and regional gateways, so one copy of a batch crosses each region boundary instead of one per gateway.
- Region failure: GeoDNS shifts viewers to a healthy region. Clients reconnect and resume. Posting continues in the new region.

---

## 13. Trade-offs Summary

| Decision | Chosen | Alternative | Reason / cost |
|---|---|---|---|
| Delivery completeness | **Sample + batch** | Deliver every comment | Orders of magnitude less bandwidth and a more readable feed, at the cost of exact completeness |
| Moderation timing | **Post-moderation (async ML) by default** | Pre-moderation | Lower latency. Risk of brief exposure, mitigated by fast delete propagation. Pre-moderation available per stream. |
| Fan-out transport | **Redis Pub/Sub** | Redis Streams / Kafka direct | Lowest latency and simplest for ephemeral gateways, but at-most-once. Acceptable here. |
| Client transport (read) | **SSE** | WebSocket | Simpler and cheaper, one-way. Switch to WebSocket if two-way features are needed. |
| Client transport (write) | **HTTPS POST** | WebSocket | Stateless and independently scalable |
| Ordering | **Approximate (time-sortable IDs)** | Strict global order | Strict order needs a bottleneck. Approximate order is invisible to users. |
| Consistency | **Eventual** | Strong | Matches the product need and enables availability and scale |
| Delete implementation | **Soft delete (status column)** | Real DELETE | Avoids Cassandra tombstones |
| Recent cache structure | **ZSET** | List | Range-by-ID resume |
| Batching waits | **Up to about 100 ms + 250-500 ms** | Send immediately | A few hundred ms of delay for big savings in packets, syscalls and Redis load |

---

## 14. Monitoring and SLOs

**SLOs**

- End-to-end delivery latency: p99 < 2 s.
- Post success rate: > 99.9%.
- Gateway connection success rate: > 99.9%.

**Key metrics**

| Area | Metrics |
|---|---|
| Write path | POST rate, 4xx/5xx rate, rate-limit rejections, Kafka produce latency |
| Kafka | Consumer lag per group (`persistence-writer`, `fanout-workers`), partition skew, hot partitions |
| Fan-out | Publish rate, batch size, time from Kafka timestamp to Pub/Sub publish |
| Gateways | Active connections, flush duration, dropped (sampled) count, slow-client disconnects, reconnect rate |
| Redis | Pub/Sub throughput, memory, ZSET sizes, command latency |
| Cassandra | Write latency p99, partition size, pending compactions |
| Product | Comments/sec per stream, sample ratio (`dropped / incoming`), delete latency |

**End-to-end latency probe:** inject synthetic comments into a few test streams and measure the time until a synthetic viewer receives them.

**Alerts:** consumer lag above threshold, p99 latency above 2 s, gateway connection saturation above 75%, sudden spike in reconnects (likely a gateway or network issue).

---

## 15. Possible Extensions

- **Reactions / emoji bursts:** aggregate counts server-side and push one summary per second instead of one event per reaction.
- **Pinned comments and highlights:** a separate small channel or a flag on the comment with priority delivery.
- **Q&A mode with upvotes:** a ranked list, backed by a Redis sorted set keyed by votes.
- **Translation:** async translation workers attach translated text, delivered per viewer language.
- **Analytics and hype detection:** comment rate per stream as a real-time signal, via a Kafka consumer into a stream-processing job.
- **Personalized ranking:** prioritize comments from people the viewer follows (needs per-viewer data, so use carefully at scale).
- **Paid or highlighted comments:** a priority lane that bypasses sampling.

---

### Quick Recap

- **Write path (blue):** HTTPS POST, then validate, rate limit, filter and ID, then Kafka, then respond 202. Cassandra persistence happens asynchronously.
- **Read path (green):** Kafka, then fan-out workers, then Redis Pub/Sub, then gateways (subscribe once per stream), then sample and batch, then SSE to viewers.
- **Join and reconnect:** recent cache (Redis ZSET) plus `Last-Event-ID` resume.
- **The core idea:** durable log in the middle (Kafka), a cheap and disposable fast lane on top (Redis Pub/Sub plus gateways), and **sampling at the edge** so cost and UX stay sane at 1M viewers.
