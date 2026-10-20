# Distributed Job Scheduler: A Deep Dive

> A complete, step-by-step walkthrough of the high-level design (HLD): what each component does, what gets stored where, which scheduler node picks a job and how, and how the job finally gets executed. Written for someone new to the topic, but with the depth expected in a Staff / Principal engineer system design interview.

---

## Table of Contents

1. [What problem are we solving?](#1-what-problem-are-we-solving)
2. [Requirements and numbers](#2-requirements-and-numbers)
3. [The big picture](#3-the-big-picture)
4. [Core concepts you must understand first](#4-core-concepts-you-must-understand-first)
5. [Data model (what is stored where)](#5-data-model-what-is-stored-where)
6. [Our running example](#6-our-running-example)
7. [Step-by-step flow](#7-step-by-step-flow)
   - [Phase A: Define a job (steps 1-3)](#phase-a-define-a-job-steps-1-3)
   - [Phase B: Fire on time (steps 4-6)](#phase-b-fire-on-time-steps-4-6)
   - [Phase C: Execute safely (steps 7-9)](#phase-c-execute-safely-steps-7-9)
   - [Phase D: Recover from failures (step 10)](#phase-d-recover-from-failures-step-10)
8. [Is the Dispatch Queue Kafka?](#8-is-the-dispatch-queue-kafka)
9. [The full life of one run (timeline)](#9-the-full-life-of-one-run-timeline)
10. [Failure scenarios, one by one](#10-failure-scenarios-one-by-one)
11. [Scheduling edge cases](#11-scheduling-edge-cases)
12. [Scaling and capacity math](#12-scaling-and-capacity-math)
13. [Multi-tenancy, fairness and security](#13-multi-tenancy-fairness-and-security)
14. [Observability and SLOs](#14-observability-and-slos)
15. [Alternatives and why we did not pick them](#15-alternatives-and-why-we-did-not-pick-them)
16. [Interview playbook](#16-interview-playbook)
17. [Glossary](#17-glossary)

---

## 1. What problem are we solving?

A **job scheduler** runs tasks at the right time, without a human pressing a button.

Examples:

| You say | The scheduler does |
|---|---|
| "Every day at 2 AM, generate the billing report" | Calls the billing service at 02:00 every day |
| "Once, at 5:30 PM tomorrow, send the launch email" | Fires exactly one time |
| "Every 5 minutes, refresh the cache" | Fires 288 times a day |

On one machine this is trivial (that is what Linux `cron` does). It becomes hard when:

- There are **millions of jobs**, not dozens.
- One machine is a **single point of failure**: if it dies at 01:59, the 02:00 job never runs.
- If you simply run the scheduler on 3 machines for safety, **all 3 fire the same job**, so the customer is billed three times.
- **Workers crash in the middle** of a job. Did it finish? Should it run again?
- Every job in the company is set for **the top of the hour**, so load spikes 10x for a few seconds.

So the real problem is:

> Fire every job **on time**, **at least once**, **never accidentally in duplicate**, survive machine failures at every step, and scale horizontally.

---

## 2. Requirements and numbers

### Functional

- Create, update, pause, resume, delete jobs.
- Schedule types: **cron** (`0 2 * * *`), **fixed interval** (every 5 min), **one-time** (at a timestamp).
- Trigger a job manually ("run now").
- Retries with backoff, per-job timeout, priority, per-job concurrency policy.
- Query job and run status and history.

### Non-functional

| Property | Target (example) |
|---|---|
| Scale | 100 million jobs, about 100 million runs/day |
| Scheduling precision | Fire within **1 second** of the due time (p99) |
| Availability | No single point of failure; survives node, zone, worker failures |
| Delivery guarantee | **At-least-once** delivery, **exactly-once effect** via idempotency |
| Durability | A created job and every state change survives crashes |
| Fairness | One noisy tenant cannot starve others |

### Back-of-envelope (illustrative numbers)

```
100M jobs x 1 run/day average        = 100M runs/day
100M / 86,400 s                      = ~1,160 runs/s on average
Top-of-minute / top-of-hour spike    = ~10x  ->  ~10,000 runs/s peak
Partitions                           = 256
Peak runs per partition              = 10,000 / 256 = ~40 runs/s
```

~40 fires/second per partition is trivial for one process. That is the whole point of partitioning: no single machine ever sees the full load.

---

## 3. The big picture

The architecture image (`job_scheduler_hld_1920x1080.png`) shows ten numbered arrows. Here is the same picture in text:

```
                          CONTROL PLANE (define + schedule)
 +--------+   1   +---------+   2   +----------+   3   +--------------------------+
 | Client |<----->| API     |<----->| Job API  |<----->|   JOB STORE (sharded SQL) |
 +--------+       | Gateway |       |(stateless)|       |  jobs | job_runs | status |
                  +---------+       +----------+       +--------------------------+
                                                            ^  5 poll + txn   ^
                                                            |                 |
                                         4 lease            v                 |
                      +-----------+ <-------------> +-----------------+       |
                      | etcd / ZK |                 | Scheduler Nodes |       |
                      +-----------+                 | (own partitions)|       |
                                                    | + Dispatcher    |       |
                                                    +--------+--------+       |
                                                             | 6 publish run_id
                                                             v                |
                          DATA PLANE (execute + recover)  +----------------+  |
                                                          | Dispatch Queue |  |
                                                          | high/normal/low|  |
                                                          +-------+--------+  |
                                                                  | 7 pull    |
 +---------------+  10 expired leases        8 claim/heartbeat/   v           |
 | Lease Monitor |<------------------------->   complete   +---------------+   |
 |   (Reaper)    |    (reads/writes Job Store)  <---------->| Worker Fleet  |<--+
 +---------------+                                          +-------+-------+
                                                                    | 9 run job
                                                                    v
                                                            +---------------+
                                                            |  Job Targets  |
                                                            +---------------+
```

### The one design principle that makes everything else work

> **The Job Store (database) is the single source of truth. The queue is only a delivery hint.**

Every important fact ("this run exists", "this worker owns it", "it succeeded") is a row in the database, changed by **atomic conditional updates**. The queue just tells workers "go look at run X". If the queue duplicates a message, loses a message, or delivers it late, the database state machine still guarantees correctness. This is what separates a Staff-level answer from a junior one, who usually trusts the queue to be perfect.

---

## 4. Core concepts you must understand first

### 4.1 Job vs Run

- A **Job** is the *definition*: "every day at 02:00 call the billing service". It lives for months.
- A **Run** is *one firing* of that job at *one specific time*: "the 02:00 firing on 2026-10-21". It lives for seconds or minutes.

One job produces many runs. Keeping them as separate rows lets each firing be tracked, retried, cancelled and audited independently.

### 4.2 Partition

We split all jobs into **256 buckets** using a stable hash of the job ID:

```
partition_id = crc32(job_id) % 256
```

The partition number is computed once when the job is created and **never changes**. All rows for a job (the job itself and all its runs) share the same `partition_id`. Each partition is owned by **exactly one scheduler node** at any moment. That ownership is what prevents two schedulers from firing the same job.

### 4.3 Lease

A lease is a **temporary claim with an expiry time**, like a library book with a due date.

- Scheduler nodes hold leases on **partitions** (stored in etcd).
- Workers hold leases on **runs** (stored in the Job Store).

If the holder crashes, it stops renewing, the lease expires, and someone else can safely take over. No one has to *detect* a crash; time passing is enough.

### 4.4 Heartbeat

A heartbeat is the holder saying "still alive, extend my lease". A worker running a long job sends one every 10 seconds, extending a 30-second lease.

### 4.5 Fencing token

Leases alone have a nasty hole. Imagine a worker freezes for 40 seconds (a long garbage-collection pause). Its lease expires, another worker takes the run. Then the first worker **wakes up and thinks it still owns the run**.

A **fencing token** is a number that increases every time a run is claimed:

```
claim #1 by worker A  -> token 1
lease expires, claim #2 by worker B -> token 2
worker A wakes up, tries to write with token 1 -> REJECTED (current token is 2)
```

Every write by a worker includes its token and the database accepts it only if the token is still current. The stale worker is "fenced off".

### 4.6 Idempotent

An operation is **idempotent** if doing it twice has the same effect as doing it once.

- "Set balance to 100" is idempotent. "Add 10 to balance" is not.
- We pass every target an **idempotency key** (derived from `job_id + scheduled_time`). The target remembers keys it has processed and ignores repeats.

### 4.7 At-least-once vs exactly-once

True exactly-once across a network is impossible. If a worker calls a target, and the target does the work but the response is lost, the worker cannot know whether it happened. Retrying risks a duplicate; not retrying risks a miss.

So the industry answer is:

> **At-least-once delivery + idempotent targets = exactly-once effect.**

We guarantee the job runs at least once, and we give the target what it needs to ignore duplicates.

### 4.8 Timing wheel (or delay heap)

An in-memory structure that holds "things due in the next few seconds" and wakes up precisely when each is due, without scanning everything every millisecond. A simple min-heap sorted by due time also works at our per-partition scale (~40 fires/s).

### 4.9 Outbox pattern

When you must (1) update a database and (2) send a message, you can not do both atomically across two systems. **Outbox**: write the "to be sent" fact into the database in the same transaction (our run row with status `READY`), and let a separate loop (the **Dispatcher**) send it and mark it done. If the sender crashes, the fact is still in the database and the loop simply sends it again.

---

## 5. Data model (what is stored where)

Three places hold state:

| Store | Holds | Why |
|---|---|---|
| **Job Store (sharded SQL)** | `jobs`, `job_runs` | Durable, transactional, the source of truth |
| **etcd / ZooKeeper** | Which scheduler node owns which partition (small, tiny data) | Strongly consistent leases and watches |
| **Dispatch Queue** | Short-lived messages containing only a `run_id` | Fast fan-out to workers; disposable |

### 5.1 `jobs` table (the definitions)

```sql
CREATE TABLE jobs (
  job_id            TEXT PRIMARY KEY,            -- e.g. 'job_8f3a91c2'
  tenant_id         TEXT NOT NULL,               -- who owns it (for quotas, fairness)
  name              TEXT NOT NULL,
  partition_id      SMALLINT NOT NULL,           -- crc32(job_id) % 256, fixed forever
  status            TEXT NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | PAUSED | COMPLETED | DELETED
  schedule_type     TEXT NOT NULL,               -- CRON | INTERVAL | ONE_TIME
  cron_expr         TEXT,                        -- '0 2 * * *'
  timezone          TEXT DEFAULT 'UTC',
  next_run_time     TIMESTAMPTZ,                 -- THE key column: when it should fire next
  target_type       TEXT NOT NULL,               -- HTTP | GRPC | CONTAINER | QUEUE_MESSAGE
  target_config     JSONB NOT NULL,              -- URL, method, headers, secret refs
  payload           JSONB,                       -- body to send
  priority          TEXT NOT NULL DEFAULT 'normal',  -- high | normal | low
  timeout_seconds   INT  NOT NULL DEFAULT 600,
  max_attempts      INT  NOT NULL DEFAULT 5,
  backoff_base_ms   INT  NOT NULL DEFAULT 10000,
  backoff_cap_ms    INT  NOT NULL DEFAULT 600000,
  misfire_policy    TEXT NOT NULL DEFAULT 'FIRE_ONCE_NOW', -- SKIP | FIRE_ONCE_NOW | FIRE_ALL
  concurrency_policy TEXT NOT NULL DEFAULT 'SKIP',  -- ALLOW | SKIP | QUEUE | CANCEL_PREVIOUS
  version           BIGINT NOT NULL DEFAULT 0,   -- optimistic-lock counter
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- The hot query: "which ACTIVE jobs in MY partitions are due soon?"
CREATE INDEX idx_jobs_due ON jobs (partition_id, next_run_time)
  WHERE status = 'ACTIVE';
```

The partial index only contains active jobs, so paused and finished jobs add no cost to the hot polling query.

### 5.2 `job_runs` table (the firings)

```sql
CREATE TABLE job_runs (
  run_id          TEXT PRIMARY KEY,      -- DETERMINISTIC: job_id + '_' + scheduled_time
  job_id          TEXT NOT NULL,
  tenant_id       TEXT NOT NULL,
  partition_id    SMALLINT NOT NULL,
  scheduled_time  TIMESTAMPTZ NOT NULL,  -- the LOGICAL due time (from cron), not when it actually fired
  status          TEXT NOT NULL,         -- READY | QUEUED | RUNNING | SUCCEEDED | FAILED | DEAD | CANCELLED
  attempt         INT  NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMPTZ NOT NULL,  -- when it becomes eligible to dispatch (used for backoff)
  queued_at       TIMESTAMPTZ,
  worker_id       TEXT,
  lease_expiry    TIMESTAMPTZ,           -- when the current worker's claim runs out
  fencing_token   BIGINT NOT NULL DEFAULT 0,
  started_at      TIMESTAMPTZ,
  finished_at     TIMESTAMPTZ,
  last_error      TEXT,
  result          JSONB,
  UNIQUE (job_id, scheduled_time)        -- a time slot can only ever fire ONCE
) PARTITION BY RANGE (scheduled_time);   -- daily partitions; drop old ones cheaply

-- Dispatcher: "READY runs of my partitions that are eligible now"
CREATE INDEX idx_runs_dispatch ON job_runs (partition_id, next_attempt_at)
  WHERE status = 'READY';
-- Sweeper for lost messages: "QUEUED for too long without being claimed"
CREATE INDEX idx_runs_queued ON job_runs (partition_id, queued_at)
  WHERE status = 'QUEUED';
-- Reaper: "RUNNING runs whose lease expired"
CREATE INDEX idx_runs_lease ON job_runs (lease_expiry)
  WHERE status = 'RUNNING';
```

Two details worth noticing:

1. **`run_id` is deterministic** (`job_id` + scheduled time). Even without the unique constraint, the same time slot always yields the same ID, so a duplicate insert collides.
2. **`UNIQUE (job_id, scheduled_time)`** is the single most important line in the schema. It makes "fire the same slot twice" physically impossible, no matter how many buggy or duplicate schedulers exist.

### 5.3 The run state machine

```
                       +-----------------------------------------------+
                       |                                               |
                       v                                               |
   scheduler      +---------+  dispatcher  +--------+  worker   +-----------+
   inserts  ----> |  READY  | -----------> | QUEUED | --claim-> |  RUNNING  |
                  +---------+              +--------+           +-----+-----+
                       ^                                              |
                       |       lease expired / job failed,            |
                       |       attempts left: back off ---------------+
                       |                                              |
                       |                         +--------------------+------------------+
                       |                         |                    |                  |
                       |                   +-----------+        +----------+       +----------+
                       +-------------------| (retry)   |        | SUCCEEDED|       |   DEAD   |
                                           +-----------+        +----------+       | (give up)|
                                                                                   +----------+
```

Only these transitions are legal, and **every transition is a conditional `UPDATE ... WHERE status = <expected> [AND fencing_token = <expected>]`**. If the condition is not met, zero rows change and the caller knows someone else got there first. That is the entire concurrency-control mechanism of the system.

### 5.4 etcd contents (tiny)

```
/sched/nodes/sched-1        -> {"addr":"10.0.1.11"}   (ephemeral, lease TTL 10s)
/sched/nodes/sched-2        -> ...
/sched/nodes/sched-3        -> ...
/sched/nodes/sched-4        -> ...

/sched/partitions/0         -> {"owner":"sched-2"}    (lease TTL 10s)
...
/sched/partitions/147       -> {"owner":"sched-3"}    (key's create_revision = 90412 = epoch)
...
/sched/partitions/255       -> {"owner":"sched-1"}
```

256 small keys plus a handful of node keys. etcd is **not** on the data path of individual jobs; it only answers "who owns partition 147?".

---

## 6. Our running example

We will follow one job from creation to execution, using real-looking values. Today is **Tuesday, 2026-10-20**.

### The job

| Field | Value |
|---|---|
| Tenant | `acme-finance` |
| Job ID | `job_8f3a91c2` |
| Name | `daily-billing-report` |
| Schedule | cron `0 2 * * *`, timezone `UTC` (every day at 02:00 UTC) |
| Target | `POST https://billing.acme.internal/v1/reports/daily` |
| Payload | `{"report":"daily","region":"eu"}` |
| Priority | `normal` |
| Timeout | 600 s |
| Retry | max 5 attempts, backoff base 10 s, cap 10 min |
| Created at | 2026-10-20 09:15:00 UTC |
| First firing | **2026-10-21 02:00:00 UTC** |

### The cluster (small example)

- 4 scheduler nodes: `sched-1` ... `sched-4`
- 256 partitions, so about 64 partitions per node
- Job Store shards: partition ranges map to shards (more on this in section 12)
- Worker pool `http-normal` with workers `w-http-01` ... `w-http-40`
- Lease TTL = 30 s, heartbeat interval = 10 s

### Partition math for our job

```
crc32("job_8f3a91c2") = 3021615507
3021615507 % 256      = 147
```

So the job belongs to **partition 147**, and the current owner of partition 147 is **`sched-3`** (decided in step 4). Remember these two facts: *partition 147, owner sched-3*.

---

## 7. Step-by-step flow

Numbers below match the numbered arrows in the architecture image.

---

### Phase A: Define a job (steps 1-3)

#### Step 1: Client to API Gateway

The user (or a service, or CI) sends:

```http
POST /v1/jobs HTTP/1.1
Host: scheduler.acme.com
Authorization: Bearer eyJhbGciOi...
Idempotency-Key: create-daily-billing-2026-10-20
Content-Type: application/json

{
  "name": "daily-billing-report",
  "schedule": { "type": "CRON", "expr": "0 2 * * *", "timezone": "UTC" },
  "target": {
    "type": "HTTP",
    "method": "POST",
    "url": "https://billing.acme.internal/v1/reports/daily"
  },
  "payload": { "report": "daily", "region": "eu" },
  "priority": "normal",
  "timeout_seconds": 600,
  "retry": { "max_attempts": 5, "backoff_base_ms": 10000, "backoff_cap_ms": 600000 }
}
```

What the **API Gateway** does here (and why):

| Action | Why |
|---|---|
| Verifies the token, extracts `tenant_id = acme-finance` | Authentication; every later row is tagged with the tenant |
| Checks quota: "acme-finance has 41,200 of 50,000 allowed jobs" | Stops one tenant from creating billions of jobs |
| Rate limit: "max 100 create requests/s per tenant" | Protects the control plane |
| Routes to any healthy Job API instance | Load balancing; Job API is stateless |

#### Step 2: Gateway to Job API

The **Job API** is a stateless service, so you can run 3 or 300 copies. It does the real validation:

1. **Idempotency check.** Look up `Idempotency-Key = create-daily-billing-2026-10-20`. If this key was already used, return the earlier response. This protects against a client retrying after a network timeout and creating two jobs.
2. **Validate the cron expression** `0 2 * * *` (syntax, and reject absurd ones such as every second if policy forbids).
3. **Validate the target.** For HTTP targets: scheme must be allowed, host must be on the tenant's allowlist (this blocks SSRF attacks such as targeting the cloud metadata endpoint).
4. **Generate the ID**: `job_8f3a91c2`.
5. **Compute the partition**: `crc32("job_8f3a91c2") % 256 = 147`.
6. **Compute the first `next_run_time`.** Using a cron library: the next time `0 2 * * *` matches after now (2026-10-20 09:15:00 UTC) is **2026-10-21 02:00:00 UTC**.

#### Step 3: Job API to Job Store

The Job API writes one row. Because the partition is computed up front, this write goes to exactly one shard.

```sql
INSERT INTO jobs (
  job_id, tenant_id, name, partition_id, status,
  schedule_type, cron_expr, timezone, next_run_time,
  target_type, target_config, payload, priority,
  timeout_seconds, max_attempts, backoff_base_ms, backoff_cap_ms
) VALUES (
  'job_8f3a91c2', 'acme-finance', 'daily-billing-report', 147, 'ACTIVE',
  'CRON', '0 2 * * *', 'UTC', '2026-10-21 02:00:00+00',
  'HTTP', '{"method":"POST","url":"https://billing.acme.internal/v1/reports/daily"}',
  '{"report":"daily","region":"eu"}', 'normal',
  600, 5, 10000, 600000
);
```

The database now holds:

```
jobs
+--------------+--------------+------+-----------+------------------------+--------+---------+
| job_id       | tenant_id    | part | status    | next_run_time          | cron   | version |
+--------------+--------------+------+-----------+------------------------+--------+---------+
| job_8f3a91c2 | acme-finance | 147  | ACTIVE    | 2026-10-21 02:00:00+00 | 0 2 *  |    0    |
+--------------+--------------+------+-----------+------------------------+--------+---------+
job_runs  (no rows for this job yet)
```

Response to the client: `201 Created {"job_id":"job_8f3a91c2","next_run_time":"2026-10-21T02:00:00Z"}`.

**Key insight:** the job is now just a row waiting for its time. There is **no timer, thread, or memory** held for it. 100 million waiting jobs cost disk space only, not RAM or CPU. A scheduler wakes up for it later, because the row says it is due.

---

### Phase B: Fire on time (steps 4-6)

This is the heart of the system. Three questions:

1. Which scheduler node is responsible for job `job_8f3a91c2`? (step 4) -> We will use consistent Hashing
2. How does that node notice the job is due and create the run? (step 5)
3. How does the run reach a worker? (step 6)

#### Step 4: Scheduler nodes acquire partition leases from etcd

**Nobody "assigns" a job to a scheduler.** Jobs belong to *partitions* (fixed at creation: partition 147). Scheduler nodes own *partitions*. A node picks up a job simply because the job's partition is in the node's owned set.

##### 4a. Deciding who *should* own each partition

Each node knows the live membership list from etcd (`/sched/nodes/*`, each key tied to a 10 s lease). To decide the preferred owner of partition `p`, every node runs the same deterministic function, **rendezvous (highest-random-weight) hashing**:

```
owner(p) = the node n that maximizes  hash(p, n)
```

Every node computes this independently and gets the same answer, with no coordinator. In our example with nodes `sched-1..4`, partition 147 is won by **`sched-3`**. Across all 256 partitions the split is 63 / 63 / 67 / 63, which is nicely balanced.

Why rendezvous hashing and not `p % numNodes`? Because when a node is added, only the partitions the new node wins move. Adding a 5th node in our simulation moves **51 of 256 partitions (about 20%, the theoretical 1/5)**. With plain modulo, nearly every partition would move, causing a storm of ownership changes.

##### 4b. Actually acquiring the lease (mutual exclusion)

Knowing who *should* own it is not enough; we need a guarantee that **only one node acts at a time**. That is etcd's job, using a conditional transaction:

```
sched-3 runs (atomically in etcd):
  IF   key /sched/partitions/147 does not exist      (create_revision == 0)
  THEN put /sched/partitions/147 = {"owner":"sched-3"}  WITH LEASE (TTL 10s)
  ELSE fail
```

etcd is strongly consistent (Raft), so if two nodes race, exactly one wins. The winner then sends a **keepalive** about every 3 seconds to renew the 10-second TTL.

The key's **`create_revision`** (a number etcd increments cluster-wide on every change) becomes the partition's **epoch**. Say it is `90412`. A later owner always gets a larger epoch.

##### 4c. What happens when a node dies

```
t = 0 s      sched-3 crashes. Keepalives stop.
t = 0..10 s  Its leases are still "valid" in etcd (TTL not yet expired).
t = 10 s     etcd deletes /sched/partitions/147 (and ~63 others owned by sched-3).
t = 10 s     Other nodes are watching /sched/partitions/ and see the delete events.
             Whichever node is the next-highest-hash for each orphaned partition
             (rendezvous order) tries the create-if-absent transaction.
t = 10.1 s   sched-2 wins partition 147 (it is the runner-up in the rendezvous order). New epoch = 90977 (larger than 90412).
t = 10.2 s   sched-2 starts polling partition 147 and picks up where sched-3 left off.
```

Worst case, partition 147's jobs are not scheduled for about **10 seconds**. That is why the scheduler polls a lookahead window of ~30 s: jobs due during the gap are fired late by up to ~10 s (the misfire policy decides what happens, see section 11), but never lost and never doubled.

##### 4d. The subtle danger: a "zombie scheduler"

What if `sched-3` is not dead, only **partitioned from etcd** (network issue) or paused (long GC)? It still thinks it owns partition 147, while `sched-1` has taken over. Two owners!

We defend against this with **two independent layers**:

1. **Self-fencing.** A scheduler must stop firing a partition as soon as it cannot prove it still holds the lease. Rule: *if the last successful keepalive was more than 7 seconds ago (TTL 10 s minus a 3 s safety margin), stop acting on that partition immediately.* The node removes itself from the race before etcd expires its lease.
2. **Database-level protection (the real safety net).** Even if a zombie does fire, step 5's transaction uses optimistic locking (`version` compare-and-set) and the `UNIQUE (job_id, scheduled_time)` constraint. A second attempt to fire the same slot **fails in the database**. (Shown in step 5.)

We use both: self-fencing keeps duplicated *work* rare; the database constraint makes duplicated *effects* impossible. This "belt and suspenders" thinking is exactly what interviewers look for.

##### 4e. Where the epoch is used

The scheduler stores its epoch and includes it in every dispatch so that downstream components can see which ownership generation produced a run. Optionally, a small `partition_owner(partition_id, epoch)` table can be consulted in the firing transaction (`WHERE epoch = :mine`) to reject writes from an older owner outright. Even if you skip this table, correctness holds because of the unique key and CAS.

---

#### Step 5: The owning scheduler polls the Job Store and fires the run

This step has three parts: **find due jobs**, **wait until the exact moment**, **fire in one transaction**.

##### 5a. Find jobs that are due soon (the polling loop)

Every ~5 seconds, each owned partition runs this query against its shard:

```sql
SELECT job_id, cron_expr, timezone, next_run_time, version, priority, tenant_id
FROM   jobs
WHERE  partition_id = 147
  AND  status = 'ACTIVE'
  AND  next_run_time <= now() + interval '30 seconds'   -- LOOKAHEAD window
ORDER  BY next_run_time
LIMIT  500;
```

This uses `idx_jobs_due (partition_id, next_run_time)`, so it reads only the handful of rows actually due, never the 100 million jobs. Why a lookahead of 30 seconds instead of "due right now"?

- Polling "due right now" every 5 s would make every job up to 5 s late.
- With lookahead, the scheduler **already has the job in memory** before it is due, so it can fire at the exact second.

For our example: polls on partition 147 run every 5 seconds. The first poll that can see our job is the one at **2026-10-21 01:59:30**: the row has `next_run_time = 02:00:00`, which is `<= 01:59:30 + 30 s = 02:00:00`, so it is returned and held in memory for the next 30 seconds.

##### 5b. Hold it in an in-memory timing wheel

`sched-3` places the entry `{job_8f3a91c2, due=02:00:00.000, version=0}` in an in-memory **timing wheel** (or a min-heap ordered by due time). A single timer thread sleeps until the earliest due time, wakes, and fires. Precision is typically within tens of milliseconds.

A job already in memory is remembered by `(job_id, version)` so the next poll, which also sees it (it is still in the window), does not insert it twice.

##### 5c. Fire: one atomic database transaction

At **02:00:00.000**, the timer pops our entry. `sched-3` computes two values with its cron library:

- `scheduled_time` = **02:00:00 on 2026-10-21** (this is the *logical* due time taken from the job row, not the wall clock; important for uniqueness)
- the **next** occurrence after it: **2026-10-22 02:00:00**

Then, in a single transaction on the shard:

```sql
BEGIN;

-- 1. Advance the job to its next occurrence, but ONLY if nobody else changed it.
UPDATE jobs
SET    next_run_time = '2026-10-22 02:00:00+00',
       version       = version + 1,
       updated_at    = now()
WHERE  job_id = 'job_8f3a91c2'
  AND  version = 0;                       -- optimistic lock: the version we read
-- If this affected 0 rows: someone else (another scheduler, or an API update) got there first.
-- ROLLBACK and do nothing. (Another owner already fired it, or the job was edited.)

-- 2. Create the run. The unique key makes a second insert for the same slot impossible.
INSERT INTO job_runs (
  run_id, job_id, tenant_id, partition_id, scheduled_time,
  status, attempt, next_attempt_at, fencing_token
) VALUES (
  'run_8f3a91c2_20261021T020000Z', 'job_8f3a91c2', 'acme-finance', 147,
  '2026-10-21 02:00:00+00',
  'READY', 0, now(), 0
)
ON CONFLICT (job_id, scheduled_time) DO NOTHING;

COMMIT;
```

Both statements either commit together or not at all. State after commit (02:00:00.012):

```
jobs
| job_8f3a91c2 | ACTIVE | next_run_time = 2026-10-22 02:00:00+00 | version = 1 |

job_runs
| run_id                          | job_id       | scheduled_time         | status | attempt | next_attempt_at     | fencing_token |
| run_8f3a91c2_20261021T020000Z   | job_8f3a91c2 | 2026-10-21 02:00:00+00 | READY  |    0    | 2026-10-21 02:00:00 |       0       |
```

**Why this transaction is the heart of correctness.** Suppose two schedulers fire the same job simultaneously (a zombie or a split brain):

| Scheduler | `UPDATE ... WHERE version = 0` | Result |
|---|---|---|
| sched-3 (first) | 1 row affected, version becomes 1 | Proceeds, inserts run, commits |
| zombie (second) | 0 rows affected (version is already 1) | Rolls back, fires nothing |

And even if the version check were somehow bypassed, `UNIQUE (job_id, scheduled_time)` rejects the duplicate run. **Two independent mechanisms, both enforced by the database, both making a double firing impossible.**

##### 5d. Why `scheduled_time` is logical and not `now()`

If the scheduler was 3 seconds late (slow poll), `scheduled_time` is still exactly `02:00:00`. That gives us:

- A stable, predictable identity for the slot (`run_..._20261021T020000Z`).
- **Scheduling lag** is measurable: `lag = actual_fire_time - scheduled_time`.
- Clock skew between machines cannot change the identity of a run.

---

#### Step 6: The Dispatcher publishes the run to the queue

Creating the run row is **not** the same as telling a worker. The **Dispatcher** (a loop inside each scheduler node) bridges them using the **outbox pattern** described earlier.

```sql
-- Every ~200 ms, for each owned partition:
SELECT run_id, priority, tenant_id
FROM   job_runs r JOIN jobs j USING (job_id)
WHERE  r.partition_id = 147
  AND  r.status = 'READY'
  AND  r.next_attempt_at <= now()       -- respects retry backoff delays
ORDER  BY r.next_attempt_at
LIMIT  1000;
```

For each row, in order:

1. **Publish** a tiny message to the queue lane matching the priority:

   ```json
   {
     "run_id": "run_8f3a91c2_20261021T020000Z",
     "job_id": "job_8f3a91c2",
     "tenant_id": "acme-finance",
     "priority": "normal",
     "attempt_hint": 1
   }
   ```

   The message carries **only identifiers, not the payload or the URL**. Why? The message might be delivered late, twice, or after the job was edited. The worker always reads the freshest definition from the database after claiming. This keeps messages tiny and removes any chance of acting on stale data.

2. **Mark it queued:**

   ```sql
   UPDATE job_runs
   SET    status = 'QUEUED', queued_at = now()
   WHERE  run_id = 'run_8f3a91c2_20261021T020000Z' AND status = 'READY';
   ```

Now consider every crash point:

| Crash point | What survives | What happens next |
|---|---|---|
| After step 5, before publishing | Run row is `READY` | Dispatcher (this node or the new owner) publishes it later |
| After publishing, before marking `QUEUED` | Run is `READY` **and** a message is already in the queue | It is published a second time. Harmless: the worker's claim (step 8) lets only one winner through; the other gets 0 rows and discards its message |
| Queue silently loses the message | Run is `QUEUED` with an old `queued_at` | A **sweeper** inside the Dispatcher re-publishes any run `QUEUED` for more than 60 s without being claimed |

The sweeper query:

```sql
SELECT run_id FROM job_runs
WHERE  partition_id = 147 AND status = 'QUEUED'
  AND  queued_at < now() - interval '60 seconds';
-- re-publish them and set queued_at = now()
```

This is why the queue does not have to be perfect.

##### Dispatcher-side fairness (brief)

Before publishing, the Dispatcher enforces **per-tenant in-flight limits** (for example a tenant may have at most 5,000 runs `QUEUED` or `RUNNING` at once) and a token-bucket publish rate. So a tenant that creates 10 million jobs for midnight cannot fill the queue and starve everyone else. Section 13 covers fairness in depth.

At **02:00:00.040** `sched-3` publishes our message to the `normal` lane and marks the run `QUEUED` at 02:00:00.055.

---

### Phase C: Execute safely (steps 7-9)

#### Step 7: A worker pulls the message from the queue

Workers are a pool of stateless processes (`w-http-01` ... `w-http-40` in our example). Each worker has a number of **execution slots** (say 50). Its loop:

```
while running:
    free = number of empty slots
    if free == 0: wait; continue            # natural backpressure
    msgs = queue.receive(lanes, max=free, wait=20s)   # long polling
    for m in msgs: executor.submit(handle, m)
```

Details that matter:

- **Lane order.** The worker checks lanes with weighted priority, for example high : normal : low = 6 : 3 : 1, so `high` is served first but `low` is never starved forever.
- **Worker pools.** Different job types go to different queues and pools (for example `runs.normal.http` and `runs.normal.container`) because HTTP calls and heavy container jobs need very different machines.
- **Visibility timeout.** When a worker receives a message, the queue hides it from other workers for a short time (say 60 s). If the worker dies before finishing the claim, the message becomes visible again and another worker gets it.
- **Autoscaling signal.** The fleet scales on **queue depth** and **age of the oldest message**, not on CPU. "Oldest message is 45 s old" means we are behind.

At **02:00:00.090**, worker `w-http-07` receives our message from the `normal` lane.

#### Step 8: The worker claims the run, heartbeats, and reports the result

This step is the **worker-side concurrency control**. All three operations are conditional updates against the Job Store.

##### 8a. Claim (one atomic statement)

```sql
UPDATE job_runs
SET    status        = 'RUNNING',
       worker_id     = 'w-http-07',
       attempt       = attempt + 1,
       fencing_token = fencing_token + 1,
       lease_expiry  = now() + interval '30 seconds',   -- evaluated by the DATABASE clock
       started_at    = COALESCE(started_at, now())
WHERE  run_id = 'run_8f3a91c2_20261021T020000Z'
  AND  status IN ('READY', 'QUEUED')                    -- only claimable states
RETURNING attempt, fencing_token, lease_expiry;
```

Two outcomes:

| Rows affected | Meaning | Worker action |
|---|---|---|
| **1** | I won. I now own this run. | Read the job definition, start executing |
| **0** | Someone else already claimed it (a duplicate message, a faster worker) or it was cancelled | **Discard** the message (acknowledge it) and move on |

Why `lease_expiry = now() + 30s` is computed **by the database**: all lease comparisons then use **one clock**. If each worker used its own clock, a worker with a clock running 40 s slow would think it still held a lease that everyone else considers expired.

State after the claim (02:00:00.098):

```
job_runs
| run_id                        | status  | attempt | worker_id | fencing_token | lease_expiry            |
| run_8f3a91c2_20261021T020000Z | RUNNING |    1    | w-http-07 |       1       | 2026-10-21 02:00:30.098 |
```

The worker then acknowledges the queue message (deletes it). From this moment, recovery is the job of the **database lease**, not the queue.

It then fetches what to run:

```sql
SELECT target_type, target_config, payload, timeout_seconds, max_attempts,
       backoff_base_ms, backoff_cap_ms
FROM   jobs WHERE job_id = 'job_8f3a91c2';
```

Reading the definition *after* the claim guarantees it is fresh, so an edit made at 01:59 is honoured at 02:00.

##### 8b. Heartbeats (extend the lease while working)

A background thread in the worker runs every 10 seconds (one third of the TTL):

```sql
UPDATE job_runs
SET    lease_expiry = now() + interval '30 seconds'
WHERE  run_id = 'run_8f3a91c2_20261021T020000Z'
  AND  fencing_token = 1          -- my token
  AND  status = 'RUNNING'
RETURNING lease_expiry;
```

- **1 row returned**: lease extended; keep working.
- **0 rows**: I lost the run (lease expired and someone else took over, or the run was cancelled). The worker **must abort the work immediately**.

Using a heartbeat interval of one third of the TTL tolerates two missed heartbeats before the lease expires, so a brief network blip does not trigger a false failover.

(For very short jobs, say under 30 s, heartbeating can be skipped: the initial lease alone covers the job. This cuts database write load a lot.)

##### 8c. Complete (fenced write)

```sql
UPDATE job_runs
SET    status      = 'SUCCEEDED',
       finished_at = now(),
       result      = '{"http_status":200,"rows":18342}'
WHERE  run_id = 'run_8f3a91c2_20261021T020000Z'
  AND  fencing_token = 1          -- must still be MY token
  AND  status = 'RUNNING';
```

If another worker has since claimed the run, the token in the database is 2, this statement affects 0 rows, and a zombie worker **cannot overwrite** the newer worker's state.

#### Step 9: The worker executes the job against the target

For our HTTP job the worker sends:

```http
POST /v1/reports/daily HTTP/1.1
Host: billing.acme.internal
Content-Type: application/json
Idempotency-Key: run_8f3a91c2_20261021T020000Z
X-Scheduler-Run-Id: run_8f3a91c2_20261021T020000Z
X-Scheduler-Attempt: 1
X-Scheduler-Signature: sha256=9f2c...   (HMAC so the target knows the call is genuine)
X-Scheduler-Timeout: 600

{"report":"daily","region":"eu"}
```

##### The idempotency key is the exactly-once trick

Notice the key is the **run ID**, which is the same for every attempt of the same slot. Attempt 1 and a retry (attempt 2) carry the **same** key. The billing service keeps a table:

```
idempotency_keys
| key                            | status | stored_response |
| run_8f3a91c2_20261021T020000Z  | DONE   | {200, 18342 rows} |
```

If it sees the key again, it returns the stored response instead of generating a second report. This is how **at-least-once delivery becomes an exactly-once effect**.

> If the key included the attempt number, every retry would look like a brand new request and the protection would be useless. The key must be stable across retries.

##### How different outcomes are handled

| Target response | Classification | What the worker does |
|---|---|---|
| `2xx` | Success | Complete (8c) as `SUCCEEDED` |
| `5xx`, timeout, connection reset | Retryable | Set `READY` with backoff (below) |
| `429` (rate limited) | Retryable | Honor `Retry-After` header if present, else backoff |
| `4xx` other than 408/429 (e.g. 400, 404) | Permanent error, retrying will not help | Mark `DEAD` immediately (with error message) |
| Worker's own timeout (600 s) reached | Retryable | Cancel the call, then retry path |

Retry handled by the worker itself (a *reported* failure, as opposed to a crash):

```sql
UPDATE job_runs
SET    status = 'READY',
       next_attempt_at = now() + interval '6 seconds',   -- computed backoff with jitter
       last_error = 'HTTP 503 from billing.acme.internal',
       worker_id = NULL, lease_expiry = NULL
WHERE  run_id = '...' AND fencing_token = 1 AND status = 'RUNNING' AND attempt < 5;
```

The Dispatcher then notices the `READY` run once `next_attempt_at` has passed and republishes it, restarting the cycle at step 6.

##### Long-running and container jobs

- **Async HTTP pattern.** The target replies `202 Accepted` plus a status URL. The worker keeps heartbeating and polls the status URL until the target says done (or the job's timeout passes). The scheduler never holds a socket open for an hour.
- **Container jobs.** The worker starts a Kubernetes Job / container with `RUN_ID` and `ATTEMPT` environment variables, watches it, heartbeats, and records the exit code.
- **Resource isolation.** Per-tenant concurrency limits and per-job CPU/memory limits protect the fleet from a runaway job.
- **Graceful shutdown (deploys).** On `SIGTERM` the worker stops pulling new messages, finishes (or hands back) in-flight runs: handing back means `UPDATE ... SET status='READY', next_attempt_at=now()` so another worker picks it up immediately rather than after a lease timeout.

##### After completion: events for everyone else

Run state changes are also emitted as events (via the outbox / change-data-capture from the Job Store) to a stream such as **Kafka**, which feeds audit logs, billing, analytics and notifications ("job failed, page the owner"). This is the right place for Kafka in this system. It is *not* on the critical path of running a job.

At **02:00:47.310** the target returns `200`, and the worker marks the run `SUCCEEDED`.

---

### Phase D: Recover from failures (step 10)

#### Step 10: The Lease Monitor (Reaper) rescues abandoned runs

Workers can die in ways that give no notice: kernel panic, spot instance reclaimed, network partition, out-of-memory kill. A dead worker cannot report anything. The Reaper's whole job is to notice **the absence of heartbeats**.

##### 10a. The scan

Every ~5 seconds:

```sql
SELECT run_id, attempt, fencing_token, job_id
FROM   job_runs
WHERE  status = 'RUNNING'
  AND  lease_expiry < now()           -- uses idx_runs_lease
LIMIT  1000;
```

##### 10b. The decision for each abandoned run

Look up the job's retry policy, then:

**Case 1: attempts remain** (`attempt < max_attempts`): put it back, with a delay.

```sql
UPDATE job_runs
SET    status = 'READY',
       next_attempt_at = now() + interval '7 seconds',    -- backoff, see below
       last_error = 'lease expired (worker w-http-07 presumed dead)',
       worker_id = NULL, lease_expiry = NULL
WHERE  run_id = 'run_8f3a91c2_20261021T020000Z'
  AND  fencing_token = 1               -- CAS: only if nobody changed it meanwhile
  AND  status = 'RUNNING';
```

**Case 2: out of attempts**: give up loudly.

```sql
UPDATE job_runs SET status = 'DEAD', finished_at = now(),
       last_error = 'max attempts (5) exceeded: lease expired'
WHERE  run_id = '...' AND fencing_token = ... AND status = 'RUNNING';
-- plus: raise an alert, make it visible in the dead-letter list for manual replay
```

Because of the `fencing_token` and `status` conditions, **two Reapers racing, or a Reaper racing a worker that finally finished, cannot both win.** Only one UPDATE affects a row. So Reapers need no leader election; they can be simple stateless replicas that each scan a slice (or all) of the data.

##### 10c. Backoff with jitter

Delay before attempt *n+1*, using "full jitter":

```
window(n) = min(cap, base * 2^(n-1))
delay     = random(0, window(n))
```

With `base = 10 s`, `cap = 600 s`:

| Failed attempt n | Window | Actual delay (random in window) |
|---|---|---|
| 1 | 0 - 10 s | e.g. 7 s |
| 2 | 0 - 20 s | e.g. 13 s |
| 3 | 0 - 40 s | e.g. 31 s |
| 4 | 0 - 80 s | e.g. 52 s |
| 5 | 0 - 160 s | (it was the last attempt; would be DEAD) |

Why jitter? If a downstream service fails at 02:00 and 50,000 runs all retry after exactly 10 s, they hammer it again in perfect sync (**a retry storm / thundering herd**). Random delays spread the retries out.

##### 10d. Walkthrough: worker crash mid-run

Same job, but suppose `w-http-07` crashes. Timeline:

| Time | Event | DB state of the run |
|---|---|---|
| 02:00:00.098 | w-http-07 claims | `RUNNING`, attempt 1, token 1, lease until 02:00:30.098 |
| 02:00:10.1 | Heartbeat OK | lease until 02:00:40.1 |
| 02:00:14 | **w-http-07 crashes** (no more heartbeats) | unchanged: `RUNNING`, lease until 02:00:40.1 |
| 02:00:40.1 | Lease expires (nobody renews it) | still `RUNNING`, but `lease_expiry < now()` |
| 02:00:45 | Reaper scan finds it | CAS update: `READY`, `next_attempt_at = 02:00:52`, worker cleared |
| 02:00:52.3 | Dispatcher on `sched-3` sees `READY` and eligible, publishes | `QUEUED` |
| 02:00:52.4 | `w-http-02` claims it | `RUNNING`, **attempt 2, token 2**, new lease |
| 02:01:41 | Target returns 200 (idempotency key protected against a double effect if attempt 1 had already partly run) | `SUCCEEDED` |

**Detection delay = lease TTL (30 s) + scan interval (5 s)**, roughly 35 s at worst. That is a tunable trade-off:

- Shorter TTL means faster recovery but more heartbeat traffic and more false positives (healthy but slow workers declared dead).
- Longer TTL means slower recovery but a calmer system.

##### 10e. Walkthrough: the zombie worker

Now suppose the worker did not crash, but was frozen by a 40-second garbage-collection pause at 02:00:14.

| Time | Event |
|---|---|
| 02:00:40.1 | Lease expires. The Reaper resets the run at its 02:00:45 scan; after the backoff delay, `w-http-02` claims it at about 02:00:52 with **token 2**. |
| 02:00:54 | Frozen `w-http-07` wakes up, unaware. Its heartbeat: `WHERE fencing_token = 1`, but the DB says 2. **0 rows.** |
| 02:00:54 | `w-http-07` sees it lost the lease and **aborts** the work. |
| 02:01:10 | If it had instead finished the HTTP call, its completion `UPDATE ... WHERE fencing_token = 1` also affects 0 rows. The stale result is discarded. |

One honest caveat: between 02:00:14 and 02:00:54 `w-http-07` may already have **called the billing service**. The fencing token protects *our database state*, but it cannot recall an HTTP request already sent. This is why the **target-side idempotency key is not optional**: it is the last line of defence for the real-world side effect.

---

## 8. Is the Dispatch Queue Kafka?

**Short answer: it does not have to be, and I would not make it Kafka by default.** The design works with *any* at-least-once transport because the database, not the queue, is the source of truth. But the choice still matters for performance and operability, so here is the reasoning in full.

### 8.1 What the queue must do in this system

1. Carry tiny `run_id` messages from the Dispatcher to many workers.
2. Let **many workers share the load** and each message go to (about) one worker.
3. Handle **tasks of very different lengths** (a 200 ms HTTP call next to a 40-minute container job).
4. Support **priority lanes** and **retry delays**.
5. Redeliver a message if a worker takes it and dies before claiming.

### 8.2 What Kafka is really built for

Kafka is a **distributed append-only log**. Consumers read in order, and track progress with an **offset** (a bookmark, one per partition). It is superb at:

- Huge throughput (millions of messages/s).
- Replay (rewind the offset and re-read history).
- Strict ordering **within a partition**.
- Many independent consumer groups reading the same stream.

Those are properties of an **event stream**. A job queue wants different properties: **per-message acknowledgement** and **independent task lifetimes**.

### 8.3 Where Kafka fits badly as a task queue

| Need | Kafka | SQS-style queue (or RabbitMQ) |
|---|---|---|
| Ack each message independently | **No.** You commit an offset ("everything up to here is done") | **Yes.** Ack/delete one message |
| Long tasks of varying length | **Head-of-line blocking:** within a partition, messages are processed in order. One 30-minute task delays everything behind it in that partition | Messages are independent; a slow one blocks nothing |
| Parallelism | At most **1 consumer per partition** in a group. 100 workers need 100+ partitions | Add workers freely |
| "Hide this message for 60 s; if no ack, redeliver" (visibility timeout) | **No** equivalent | **Native** |
| Delayed delivery (retry in 30 s) | **No native delay.** Needs separate retry topics ("retry-10s", "retry-1m") with extra consumers | Native delay (SQS up to 15 min) |
| Priority | Separate topics per priority; no per-message priority | Separate queues (SQS) or native priorities (RabbitMQ) |
| Dead-letter queue | A convention you build yourself | Built in (redrive policies) |
| Pausing when workers are busy | Must call `pause()` while still polling to stay in the group, otherwise the consumer-group rebalance kicks you out | Just do not request more messages |
| Consumer-group rebalances | Stop-the-world pauses when workers join/leave (autoscaling causes churn) | Not applicable |

The classic failure story: a tenant's 200 slow jobs land in the same Kafka partition; every other tenant's fast jobs in that partition wait behind them. Spreading across more partitions reduces the problem but never removes it.

### 8.4 My recommendation

> **Use an SQS-class queue** (SQS itself on AWS; **RabbitMQ** or **Redis Streams** if self-hosted) with three queues: `runs.high`, `runs.normal`, `runs.low` (and per-worker-pool variants). Use **Kafka for the event stream of run lifecycle events** (audit, analytics, billing, notifications), where its strengths apply.

Reasons, in the order I would say them in an interview:

1. Per-message ack + visibility timeout is exactly the semantic we need for step 7.
2. No head-of-line blocking for mixed-duration jobs.
3. Horizontal worker scaling is free (no partition ceiling).
4. Operationally simpler for this purpose.

### 8.5 "But my company already runs Kafka. Can I use it?"

Yes, and the design still works, because the queue is only a hint. The trick is to **decouple Kafka's offset lifecycle from the job's lifetime**:

- Topic per lane (`runs.high`, `runs.normal`, `runs.low`) with many partitions (e.g., 256), message key = `partition_id`.
- A worker's Kafka consumer does **only the claim** (step 8a, a few milliseconds), then **commits the offset immediately** and hands the long-running work to a separate thread pool.
- Because the offset is committed after the *claim* (not after the *job*), partitions never block on slow jobs, and a crash after the claim is recovered by the **database lease and Reaper**, not by Kafka redelivery.
- Use `pause()`/`resume()` for backpressure when execution slots are full.
- Retry delays are handled by the database (`next_attempt_at`), **not** by retry topics, so you need no delay tiers.

With that discipline Kafka works. The cost is extra consumer-side complexity that a queue with native acks avoids.

### 8.6 "Why not skip the queue and let workers poll the database?"

That is a legitimate simpler design: workers run

```sql
SELECT run_id FROM job_runs
WHERE status = 'READY' AND next_attempt_at <= now()
ORDER BY next_attempt_at
FOR UPDATE SKIP LOCKED LIMIT 50;
```

`SKIP LOCKED` lets many workers pull disjoint rows without blocking each other. It is correct and fine up to moderate scale, and many real systems stop there. We add the queue to **offload tens of thousands of polling queries per second from the database** and to give push-style, low-latency delivery. In an interview, present the no-queue version as the MVP and the queue as the scaling step.

### 8.7 Summary of messaging roles

| Role | Technology | Critical for correctness? |
|---|---|---|
| Run state and truth | Job Store (SQL) | **Yes** |
| Partition ownership | etcd / ZooKeeper | Yes (leases) |
| Dispatch of run_ids to workers | SQS / RabbitMQ / Redis Streams (Kafka possible) | **No** (hint only; sweeper repairs loss) |
| Run lifecycle events (audit, analytics) | Kafka | No (observability and integrations) |

---

## 9. The full life of one run (timeline)

One run of `job_8f3a91c2` on **2026-10-21**, everything in one table:

| Time (UTC) | Who | What happens | Run state |
|---|---|---|---|
| 2026-10-20 09:15:00 | Job API | Job inserted: partition 147, `next_run_time = 10-21 02:00:00` | (no run yet) |
| 10-21 01:59:30 | sched-3 (owns partition 147) | Poll finds the job (due within the 30 s lookahead); puts it in the timing wheel | (no run yet) |
| 02:00:00.000 | sched-3 timer | Timer pops the entry | |
| 02:00:00.012 | sched-3 + Job Store | Transaction commits: `next_run_time` advanced to 10-22 02:00, run row inserted | `READY` |
| 02:00:00.040 | sched-3 Dispatcher | Publishes `{run_id,...}` to queue lane `normal` | `READY` |
| 02:00:00.055 | sched-3 Dispatcher | Marks run queued | `QUEUED` |
| 02:00:00.090 | w-http-07 | Receives the message | `QUEUED` |
| 02:00:00.098 | w-http-07 + Job Store | Claim succeeds: attempt 1, token 1, lease until :30.098 | `RUNNING` |
| 02:00:00.105 | w-http-07 | Acks and deletes the queue message; loads job definition | `RUNNING` |
| 02:00:00.150 | w-http-07 | `POST` to billing service with `Idempotency-Key` | `RUNNING` |
| 02:00:10.098 / :20.098 / :30.098 / :40.098 | w-http-07 | Heartbeats extend the lease | `RUNNING` |
| 02:00:47.310 | Billing service | Returns `200 {rows: 18342}` | `RUNNING` |
| 02:00:47.320 | w-http-07 | Completion write with token 1 | `SUCCEEDED` |
| 02:00:47.400 | CDC / outbox | `run.succeeded` event goes to Kafka for audit, metrics | `SUCCEEDED` |

**Scheduling lag** for this run: `fired (02:00:00.012) - scheduled (02:00:00.000) = 12 ms`. **Queue wait**: `claimed (02:00:00.098) - queued (02:00:00.055) = 43 ms`. Both are exactly the metrics you would alert on (section 14).

And the sequence as a diagram:

```
 Scheduler(sched-3)     Job Store        Queue        Worker(w-http-07)     Billing svc
        |                    |              |                 |                  |
        | poll (01:59:30)    |              |                 |                  |
        |------------------->|              |                 |                  |
        |<--- due jobs ------|              |                 |                  |
        |  [wait in timing wheel until 02:00:00.000]          |                  |
        | txn: advance next_run + INSERT run                   |                  |
        |------------------->|              |                 |                  |
        |<------- commit ----|  (READY)     |                 |                  |
        | publish run_id ------------------>|                 |                  |
        | mark QUEUED ------>|              |                 |                  |
        |                    |              |--- message ---->|                  |
        |                    |<-- claim (CAS: READY/QUEUED->RUNNING, token 1) ----|                  |
        |                    |--- 1 row ----------------------->|                  |
        |                    |              |<--- ack/delete --|                  |
        |                    |              |                 |-- POST + Idem-Key->|
        |                    |<-- heartbeat every 10s (token 1) |                  |
        |                    |              |                 |<----- 200 --------|
        |                    |<-- complete (token 1) -----------|                  |
        |                    |   (SUCCEEDED)|                 |                  |
```

---

## 10. Failure scenarios, one by one

For each failure: what breaks, how it is detected, how it recovers, and **why it is safe**.

| # | Failure | Detection | Recovery | Why it is safe |
|---|---|---|---|---|
| 1 | **Scheduler node crashes before the firing transaction** | etcd lease expires (about 10 s) | Another node takes the partition (new epoch), polls, finds the job still due, fires it | Nothing was committed, so nothing is duplicated. The job fires late by about 10 s, then misfire policy applies |
| 2 | **Scheduler crashes after the transaction, before publishing** | Run row sits in `READY` | The new owner's Dispatcher publishes it | The run row is the outbox; it is never lost |
| 3 | **Dispatcher publishes but crashes before marking `QUEUED`** | Run still `READY`, message already queued | Run is published again | Duplicate message; the claim CAS lets only one worker win; the other discards |
| 4 | **Queue loses the message** | Sweeper sees run `QUEUED` > 60 s with no claim | Re-publishes | State lives in the database, not the queue |
| 5 | **Worker crashes after receiving the message, before claiming** | Visibility timeout expires | Message reappears; another worker claims | Claim CAS is the only gate |
| 6 | **Worker crashes after claiming** | No heartbeats; lease expires; Reaper scan | Reaper CAS-resets to `READY` with backoff; Dispatcher republishes | Fencing token + target idempotency key |
| 7 | **Worker freezes (GC pause), then wakes** (zombie) | Its heartbeat or completion returns 0 rows | Worker aborts; the new worker's result stands | Fencing token rejects stale writes |
| 8 | **Zombie scheduler** (partitioned from etcd, still thinks it owns partition 147) | Self-fencing after 7 s without a keepalive | It stops acting; new owner proceeds | Even if it fires, `version` CAS and `UNIQUE(job_id, scheduled_time)` reject the duplicate |
| 9 | **Two schedulers fire the same slot simultaneously** | Second transaction gets 0 rows on the version-CAS (or unique violation) | Second rolls back | Database-enforced; impossible to double-create |
| 10 | **Job Store primary fails** | Health checks / failover | Replica promoted (synchronous replication so no committed data lost). Schedulers and workers retry their conditional updates | Every write is idempotent or conditional, so retry is safe |
| 11 | **etcd cluster unavailable** | Keepalives fail | Schedulers **self-fence and stop firing** (fail-safe). Workers keep running in-flight jobs (they depend only on the Job Store). When etcd returns, schedulers re-acquire and apply misfire policy | We choose **late over duplicated**. A control-plane outage never causes double-firing |
| 12 | **Target service down** | Worker sees errors/timeouts | Retries with exponential backoff + jitter; after max attempts `DEAD` + alert | Backoff prevents a retry storm; dead-letter makes failures visible |
| 13 | **Poison job** (always fails, maybe crashes workers) | Repeated lease expiries | Attempts are capped, so it ends `DEAD`. Optionally a circuit breaker auto-pauses the job after N consecutive deaths | Bounded blast radius |
| 14 | **Duplicate API call** (client retries create) | `Idempotency-Key` seen before | Return the original response | Client retries are safe |
| 15 | **Clock skew between machines** | Monitoring (NTP offset) | NTP/chrony everywhere; leases use the **database clock**; `scheduled_time` is logical | Machine clocks only affect *how late* we fire, never uniqueness or lease correctness |
| 16 | **Whole zone lost** | Health checks | Multi-AZ deployment: etcd quorum across 3 zones, DB synchronous replica in another zone, workers and schedulers in each zone | No single zone holds a quorum |

The pattern behind all 16: **every state change is a conditional write in one strongly consistent store, every actor can crash at any line of code, and the recovery path is "look at the database and finish the job".**

---

## 11. Scheduling edge cases

### 11.1 Misfires (the scheduler was down or the partition was orphaned)

If `sched-3` died at 01:59:55 and `sched-2` takes over at 02:00:06, job `job_8f3a91c2` is already 6 s overdue. What should happen is **per-job policy** (`misfire_policy`):

| Policy | Behavior | Good for |
|---|---|---|
| `FIRE_ONCE_NOW` (default) | Fire one run immediately for the *latest* missed slot; skip the rest | Reports, syncs: "just run it" |
| `SKIP` | Do not run; advance `next_run_time` to the next future slot | Time-sensitive jobs where a late run is harmful |
| `FIRE_ALL` | Create a run for **every** missed slot (capped, e.g. 100) | Per-interval accounting, backfills |

A small **grace window** (e.g. 60 s) is treated as normal lateness, not a misfire.

### 11.2 Overlapping runs (a job is slower than its period)

A job runs every minute but takes 3 minutes. Policy `concurrency_policy`:

| Policy | Behavior |
|---|---|
| `ALLOW` | Let runs overlap |
| `SKIP` (default) | Do not create the new run if the previous is still `READY/QUEUED/RUNNING` (checked inside the firing transaction) |
| `QUEUE` | Create it, but it waits (`READY`) until the previous finishes |
| `CANCEL_PREVIOUS` | Mark the old run for cancellation (see below), start the new one |

### 11.3 Cancellation

`POST /v1/runs/{id}:cancel` sets `status = CANCEL_REQUESTED`. The worker's next **heartbeat response** carries the flag; the worker aborts the target call and writes `CANCELLED`. If the run is still `READY/QUEUED`, it is set to `CANCELLED` directly and any later claim gets 0 rows.

### 11.4 Editing a job while it is firing

An update (new cron, new target) does `UPDATE jobs SET ..., version = version + 1 WHERE job_id = ? AND version = ?`. If the scheduler's firing transaction races with it, one of them sees a version mismatch and retries after re-reading. Result: **no lost update, no firing from a stale definition**.

### 11.5 Pause, resume, delete

- **Pause**: `status = 'PAUSED'` (drops out of the partial index). In-flight runs finish.
- **Resume**: recompute `next_run_time` from *now*; the paused period is not backfilled unless requested.
- **Delete**: soft-delete (`DELETED`), cleanup later; keeps run history for audit.

### 11.6 One-time jobs

`next_run_time = T`. The firing transaction inserts the run and sets `status = 'COMPLETED', next_run_time = NULL` instead of computing a next occurrence.

### 11.7 Fixed rate vs fixed delay (interval jobs)

- **Fixed rate**: every 5 minutes on the clock (`:00, :05, :10`), regardless of how long runs take. The next occurrence is computed from the previous `scheduled_time`.
- **Fixed delay**: 5 minutes *after the previous run finishes*. The next occurrence is computed at completion, so the worker's completion step also sets the job's `next_run_time`.

### 11.8 Time zones and daylight saving time

Store the **IANA time zone** (`America/New_York`), not an offset. For `0 2 * * *` in New York:

- **Spring forward** (02:00 does not exist): run at the next valid instant (03:00).
- **Fall back** (01:30 occurs twice): run **once**, on the first occurrence.

The unique key on `scheduled_time` (stored as an absolute UTC instant) makes the "once" rule automatic.

### 11.9 Manual trigger and replay

- **Run now**: `POST /v1/jobs/{id}:trigger` inserts a run whose `scheduled_time` is now and whose `run_id` includes the caller's idempotency key, so double-clicks do not double-run.
- **Replay a DEAD run**: set it back to `READY` with `attempt = 0` after the owner fixes the cause.

### 11.10 Jitter for non-critical jobs

A job can opt in to `spread_seconds = 30`: the scheduler fires it at a deterministic pseudo-random offset (hash of `job_id`) within `[scheduled_time, scheduled_time + 30 s]`. Ten million "midnight" jobs become a smooth 30-second ramp instead of a cliff.

---

## 12. Scaling and capacity math

All numbers are **illustrative** (use them as a worked example in an interview, not as facts about any real product).

### 12.1 Sharding the Job Store by partition

- 256 logical partitions mapped to **32 database shards**, 8 partitions per shard (the mapping is a small config table, `partition_id -> shard`).
- All rows of a job and all its runs share one `partition_id`, so they live on **one shard**.
- The firing transaction (step 5c) touches only one shard, so **we never need a distributed transaction**. That is the main reason scheduler partitions and storage shards use the *same* key.
- The partition is the **unit of movement**. To rebalance storage, copy one partition's rows to another shard and flip the map entry. This is why you pick a generous partition count (256 or even 1,024) up front: changing the *number* of partitions later means re-hashing every job, which you want to avoid.

Storage options: PostgreSQL with application-level sharding, Citus, Vitess (MySQL), CockroachDB or Spanner. On DynamoDB the same design works with a conditional write and a GSI on `(partition_id, next_run_time)`; the CAS logic is identical.

### 12.2 Write load

Minimum writes per run:

| Write | Count |
|---|---|
| Firing transaction (update job + insert run) | 2 |
| Mark `QUEUED` | 1 |
| Claim | 1 |
| Complete | 1 |
| **Total (no heartbeats)** | **5** |

```
Peak: 10,000 runs/s x 5 writes   = 50,000 writes/s
Spread over 32 shards            = ~1,560 writes/s per shard   (comfortable)
```

Heartbeats:

```
Average concurrent runs = 1,160 runs/s x 30 s average duration = ~35,000
Heartbeat every 10 s    = ~3,500 writes/s   (spread over 32 shards = ~110/s each)
```

Cheap optimizations: batch the `QUEUED` marks (one `UPDATE ... WHERE run_id IN (...)` per 1,000 runs), and skip heartbeats for jobs shorter than the lease.

### 12.3 Storage

```
job_runs: ~400 bytes/row (with indexes) x 100M runs/day = ~40 GB/day
Keep 30 days hot                                         = ~1.2 TB
jobs:     ~1 KB x 100M jobs                              = ~100 GB
```

`job_runs` is partitioned by day so deleting a day is `DROP PARTITION` (instant) instead of a huge `DELETE`. Older data is archived to object storage (Parquet) for audit; a "latest N runs per job" summary stays hot for the UI.

### 12.4 Scheduler tier

```
Scheduler nodes: 16-32 (for HA and headroom; 8-16 partitions each)
Peak per node:   10,000 / 16 = ~625 fires/s      (a timer thread plus a batch writer handles this easily)
Polling load:    256 partitions / every 5 s = ~51 queries/s cluster-wide   (index-only, tiny)
In-memory window: peak 10,000/s x 30 s = ~300k entries x ~200 B = ~60 MB cluster-wide
```

The scheduler tier is **not** the bottleneck. The database and the worker fleet are.

### 12.5 Worker tier

```
~35,000 average concurrent runs / 50 slots per worker = ~700 workers on average
```

Autoscale on queue depth and oldest-message age. **Pre-scale predictively:** cron times are known in advance, so the scheduler can tell the autoscaler "a large wave is due at 00:00" and scale up *before* the spike instead of reacting to it. (This is a nice advantage of a scheduler over a generic queue consumer.)

### 12.6 Queue

```
10,000 messages/s x ~200 B = ~2 MB/s   (trivial for SQS/RabbitMQ at this size)
```

### 12.7 Reads and search

- Status/history API reads hit **read replicas**.
- Searching run history ("all failed runs of tenant X yesterday") uses a **search index** (OpenSearch) fed by the Kafka event stream, so analytic reads never touch the hot Job Store.

### 12.8 Where the system bends first as it grows

1. **Job Store write throughput**: fix by adding shards (move partitions).
2. **Heartbeat volume**: lengthen lease for long jobs, skip for short jobs.
3. **Dispatcher publish rate per partition**: more partitions or batch publishes.
4. **Thundering herd at :00**: jitter, predictive scaling, admission control.

---

## 13. Multi-tenancy, fairness and security

### 13.1 Fairness (noisy neighbour)

Problem: tenant A schedules 10 million jobs at midnight. Tenant B has 50. B's jobs must still run on time.

Layers of defence:

1. **Admission quota** at the API: max jobs per tenant, max create rate.
2. **In-flight cap per tenant** in the Dispatcher: it will not publish more than N of a tenant's runs while N are already `QUEUED/RUNNING`. Excess runs stay `READY` in the database (cheap) and drain as capacity frees.
3. **Fair dispatch ordering**: when many tenants have `READY` runs, the Dispatcher takes them in **weighted round-robin** across tenants, not strictly oldest-first.
4. **Priority lanes**: `high / normal / low` queues; premium tenants or critical jobs use `high`.
5. **Dedicated pools** for very large or very sensitive tenants (their own queues and workers).

Because the cap is enforced **before** the queue, a flood becomes a backlog in the database, not in the queue or the worker fleet.

### 13.2 Security

| Concern | Control |
|---|---|
| Who can create/modify jobs | OIDC/JWT authentication, RBAC per tenant and per job |
| Secrets (API keys for targets) | Stored in a secret manager; jobs reference a secret ID, never the plaintext. Workers fetch at execution time |
| SSRF (job URL pointing at internal metadata endpoints) | Allowlist of hosts per tenant; block link-local/metadata IP ranges; egress through a proxy |
| Target must know the call is genuine | HMAC-signed request header (`X-Scheduler-Signature`), plus a timestamp to prevent replay |
| Untrusted user code | Run in sandboxed containers with CPU/memory/network limits |
| Audit | Every create/update/trigger/cancel is logged with actor and diff |
| Data at rest / in transit | Encryption at rest on the Job Store; TLS everywhere |

---

## 14. Observability and SLOs

The question every on-call engineer asks: **"Are jobs firing on time, and are any stuck?"**

### 14.1 Service level objectives

| SLO | Target |
|---|---|
| Scheduling lag (actual fire time minus `scheduled_time`) | p99 < 1 s, p99.9 < 5 s |
| Dispatch-to-start latency (`QUEUED` to `RUNNING`) | p99 < 5 s when the fleet is healthy |
| Missed runs (runs that should exist but do not) | 0 |
| Duplicate runs for one slot | 0 (enforced by the unique key) |

### 14.2 Key metrics

| Metric | What it tells you | Alert when |
|---|---|---|
| `scheduling_lag_ms` (histogram) | Are schedulers keeping up? | p99 above 1 s for 5 min |
| `partitions_unowned` | A partition has no scheduler | **> 0 for more than 15 s** |
| `partition_owner_changes_total` | Flapping leases / unstable nodes | Sudden rise |
| `etcd_keepalive_failures_total` | Control plane trouble | Any sustained |
| `dispatch_queue_depth` and `oldest_message_age_s` | Workers behind | Oldest age above 30 s |
| `queue_wait_ms` (claimed minus queued) | Fleet capacity | p99 above 5 s |
| `runs_by_status{status}` | Pile-ups in `READY`/`QUEUED` | READY count growing without drain |
| `lease_expirations_total` | Workers dying/hanging | Spike |
| `retries_total`, `retry_ratio` | Targets unhealthy | Ratio above baseline |
| `dead_runs_total` | Jobs permanently failing | Any, routed to the job owner |
| `claim_conflicts_total` (0-row claims) | Duplicate deliveries (normal at low rate) | Large spike means queue misbehaving |
| `db_shard_write_latency_ms`, `replication_lag_s` | Store health | p99 up / lag above 5 s |
| `heartbeat_lost_total` | Zombie workers | Spike |

### 14.3 Tracing and debugging

Use the **`run_id` as the trace ID** end to end (scheduler -> queue -> worker -> target). "Why did the 02:00 billing run fail?" becomes one query: find the run row, its `attempt` history, `last_error`, `worker_id`, and the linked trace.

A **reconciliation job** runs continuously: for every ACTIVE job, verify that a run exists for every `scheduled_time` that has passed (since the last check). Any gap raises an alert. This catches bugs that metrics alone would miss ("a whole class of jobs silently stopped").

---

## 15. Alternatives and why we did not pick them

| Approach | How it works | Why not (at this scale) |
|---|---|---|
| **One server running cron** | Single process wakes every second | Single point of failure; limited throughput |
| **Primary + hot standby** | Leader election, one active scheduler | Still a single-scheduler throughput ceiling; failover gaps |
| **All schedulers poll with `SELECT ... FOR UPDATE SKIP LOCKED`** | Every scheduler grabs due jobs; row locks prevent overlap | Valid and simple. At 10k fires/s the whole cluster contends on the same hot index range and lock churn grows. Partitioning removes the contention. A good **MVP**. |
| **Quartz clustered mode** | Schedulers coordinate through DB row locks | Proven for thousands of jobs; heavy lock contention at very large scale |
| **Redis sorted set as the timer** (`ZADD` score = due time, poll `ZRANGEBYSCORE`) | Very fast timers | Durability and atomic multi-step transitions are weaker. Great as an **accelerator** in front of the DB, not as the source of truth |
| **Kafka with delayed/retry topics** | Encode delay with topic tiers | No native delay; head-of-line blocking; complex (section 8) |
| **Kubernetes CronJob** | One controller creates Jobs on schedule | Cluster-scale limits (not millions of jobs), minute resolution, single controller |
| **Airflow** | DAG workflow scheduler | Designed for dependency graphs of thousands of tasks, not millions of independent timers |
| **Temporal / Cadence** | Durable timers and workflows | Excellent and very robust, especially when jobs are multi-step workflows. Heavier to operate; it essentially embeds this same design (partitioned history shards, task queues) |
| **Managed (EventBridge Scheduler, Cloud Scheduler)** | Cloud-provided | The right answer when you do not need to build it. Mention it first in a real-world discussion |

**How to present the evolution in an interview:** MVP (single DB + `SKIP LOCKED` workers) -> add queue to offload polling -> partition schedulers with leases to remove the single-scheduler ceiling -> shard the store by the same partition key -> add fairness, retries, reconciliation.

---

## 16. Interview playbook

### 16.1 The one-page cheat sheet

Every state transition in the system, with its guard:

| Transition | Statement (simplified) | Guard that makes it safe |
|---|---|---|
| Fire a slot | `UPDATE jobs SET next_run_time=.., version=version+1 WHERE job_id=? AND version=?` **+** `INSERT job_runs ... ON CONFLICT DO NOTHING` | `version` CAS and `UNIQUE(job_id, scheduled_time)` |
| Queue a run | `UPDATE job_runs SET status='QUEUED' WHERE run_id=? AND status='READY'` | Status check; republish is harmless |
| Claim | `UPDATE ... SET status='RUNNING', fencing_token=fencing_token+1, lease_expiry=now()+30s WHERE run_id=? AND status IN ('READY','QUEUED')` | Exactly one winner |
| Heartbeat | `UPDATE ... SET lease_expiry=now()+30s WHERE run_id=? AND fencing_token=? AND status='RUNNING'` | Token proves I still own it |
| Complete | `UPDATE ... SET status='SUCCEEDED' WHERE run_id=? AND fencing_token=? AND status='RUNNING'` | Stale worker cannot overwrite |
| Reap | `UPDATE ... SET status='READY', next_attempt_at=.. WHERE run_id=? AND fencing_token=? AND status='RUNNING'` | Races with worker or other reaper are harmless |

### 16.2 A 45-minute plan

| Minutes | What to cover |
|---|---|
| 0-5 | Clarify requirements and scale (jobs, fires/s, precision, delivery guarantee). State the **at-least-once + idempotent** stance early |
| 5-10 | API and data model (`jobs` vs `job_runs`, the unique key) |
| 10-20 | High-level design on the whiteboard: API, Job Store, partitioned schedulers with leases, dispatcher, queue, workers, reaper |
| 20-35 | **Deep dive** on the hard parts: firing transaction (no duplicates), worker claim/lease/fencing, recovery. Walk a crash through the system |
| 35-42 | Scaling math, sharding, hot spots, thundering herd |
| 42-45 | Observability, trade-offs, what you would build first vs later |

### 16.3 Sentences worth saying out loud

- "The database is the source of truth; the queue is a hint."
- "Every state change is a conditional write, so every actor can crash at any line."
- "We pick at-least-once delivery and make execution idempotent. Exactly-once across a network is impossible."
- "Leases handle crashes, fencing tokens handle zombies, the unique key handles duplicates."
- "On a control-plane outage we prefer late over duplicated."
- "Partitions are the unit of ownership, storage sharding and scaling."

### 16.4 Likely follow-up questions and strong answers

**Q1. What if two schedulers fire the same job?**
Impossible to create two runs: the firing transaction does a version CAS and inserts under `UNIQUE(job_id, scheduled_time)`. The loser rolls back. Leases make it rare; the constraint makes it impossible.

**Q2. How do you guarantee exactly-once execution?**
You can not, across a network. We guarantee at-least-once and give the target a stable idempotency key (`run_id`) so repeats have no extra effect. Fencing tokens keep *our* state consistent; target idempotency protects the *real-world* effect.

**Q3. The queue loses a message. What happens?**
Nothing bad. The run is still in the database as `QUEUED`; a sweeper republishes anything `QUEUED` for over 60 s. The queue holds no truth.

**Q4. A worker dies mid-job. How is it detected and what happens?**
Heartbeats stop, the lease expires (30 s), the Reaper CAS-resets the run to `READY` with jittered backoff, the Dispatcher republishes, another worker claims with a higher fencing token. Detection delay is about lease TTL + scan interval.

**Q5. A worker is just slow (GC pause). Could it corrupt state?**
No. Its writes carry the old fencing token and affect 0 rows. For the external side effect, the target's idempotency key covers it.

**Q6. How do you avoid a single point of failure in the scheduler?**
256 partitions, each leased to exactly one node via etcd with a 10 s TTL. A node dies, its partitions are re-leased within ~10 s. Self-fencing plus database CAS handle zombies.

**Q7. What if etcd goes down?**
Schedulers stop firing (fail-safe, after the self-fence timeout); workers finish in-flight jobs since they only need the Job Store. On recovery, misfire policy decides catch-up. We deliberately choose late over duplicated.

**Q8. Why not Kafka for the queue?**
Kafka gives per-partition ordering and offset commits, but no per-message ack, no visibility timeout, no native delay, and it has head-of-line blocking for variable-length jobs. An SQS-style queue matches the need. Kafka is ideal for the run-event stream. If Kafka is mandated, commit offsets after the claim (not after the job) so partitions never block (section 8.5).

**Q9. How does it scale 10x?**
More partitions or shards: the partition is the unit of movement. Add scheduler nodes (up to the partition count), DB shards, and workers (autoscaled on queue age). Predictive pre-scaling for known cron spikes.

**Q10. How do you handle the top-of-the-hour spike?**
Lookahead preloading so firing is in-memory and exact; optional per-job jitter; per-tenant in-flight caps so a flood becomes a database backlog, not a worker outage; predictive autoscaling.

**Q11. How do you prevent one tenant from starving others?**
Quotas at the API, in-flight caps and weighted round-robin in the Dispatcher, priority lanes, dedicated pools for the largest tenants.

**Q12. Clock skew?**
`scheduled_time` is a logical value from the cron expression, so skew cannot change a run's identity. Lease times are computed by the database clock, so all comparisons use one clock. NTP keeps firing latency honest.

**Q13. A job takes longer than its interval.**
Per-job `concurrency_policy`: skip (default), allow, queue, or cancel previous, evaluated inside the firing transaction.

**Q14. How would you verify nothing was silently missed?**
A reconciliation job compares expected slots (from cron) to existing runs and alerts on gaps; plus the `partitions_unowned` and scheduling-lag alerts.

**Q15. Why is the Reaper safe without leader election?**
Every action is a CAS on `(status, fencing_token)`. If two Reapers (or a Reaper and a finishing worker) race, only one UPDATE changes a row.

### 16.5 Common mistakes to avoid

| Mistake | Better |
|---|---|
| "Use Kafka, it is reliable" and trust it for delivery | Queue is a hint; DB state machine is truth |
| Claiming exactly-once | At-least-once + idempotency |
| One scheduler leader | Partitioned ownership with leases |
| Holding millions of timers in memory | Jobs are rows; load only a lookahead window |
| Using the system clock of each worker for leases | One clock: the database |
| No plan for zombies | Fencing tokens |
| Ignoring retries' effect on the target | Backoff with jitter, circuit breakers |
| No story for outages of the coordinator | Fail safe: late, never duplicated |

### 16.6 What distinguishes Staff / Principal from Senior in this question

- You state the **guarantees up front** and the **trade-offs** behind each (late vs duplicated, lease TTL vs false positives, partition count vs flexibility).
- You think in **failure modes**, not just the happy path, and show the recovery path for every component.
- You reason about **operability**: SLOs, reconciliation, alerts, runbooks, safe deploys (graceful worker drain).
- You describe an **evolution path** (MVP to scale) instead of jumping to the final architecture.
- You know when **not to build it** (managed schedulers, Temporal) and can justify when you would.

---

## 17. Glossary

| Term | Meaning |
|---|---|
| **Job** | The recurring or one-time definition of work |
| **Run** | One firing of a job at one scheduled time |
| **Cron** | Compact schedule syntax, e.g. `0 2 * * *` |
| **Partition** | One of 256 fixed buckets of jobs (`crc32(job_id) % 256`); owned by one scheduler node |
| **Shard** | A physical database holding several partitions |
| **Lease** | Time-limited claim that must be renewed or it expires |
| **Heartbeat** | Periodic renewal of a lease |
| **Epoch** | Increasing number identifying an ownership generation of a partition |
| **Fencing token** | Increasing number per run claim; stale holders are rejected |
| **CAS (compare-and-set)** | Update only if the current value is what I expect |
| **Optimistic locking** | Detect conflicting updates with a `version` column instead of holding locks |
| **Outbox pattern** | Record "to send" in the database; a separate loop sends it, retrying until done |
| **Dispatcher** | Loop that publishes `READY` runs to the queue and re-publishes lost ones |
| **Reaper (Lease Monitor)** | Finds runs whose lease expired and retries or kills them |
| **Idempotency key** | Stable identifier letting a target ignore repeats (`run_id`) |
| **At-least-once** | A message/run is delivered one or more times, never zero |
| **Misfire** | A scheduled slot that was missed (system down, partition orphaned) |
| **DLQ / DEAD** | Terminal state for runs that exhausted retries; needs human attention |
| **Backoff + jitter** | Growing, randomized delays between retries to avoid retry storms |
| **Timing wheel** | In-memory structure to wake at precise times efficiently |
| **Head-of-line blocking** | A slow item at the front delays everything behind it |
| **Thundering herd** | Many actors acting at the same instant (every cron at `:00`) |
| **Visibility timeout** | A queue hides a received message for a period; if not acked, it reappears |
| **Rendezvous hashing** | Deterministic owner selection with minimal movement when nodes change |
| **Self-fencing** | A node stops acting when it can no longer prove it holds its lease |

---

*End of document. The accompanying image, `job_scheduler_hld_1920x1080.png`, shows the architecture with the ten numbered steps referenced throughout.*
