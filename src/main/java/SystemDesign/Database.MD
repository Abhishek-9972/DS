# Chapter II — Databases & DBMS

## System Design Interview Notes

These are the **interview-focused notes** I'd recommend memorizing. The goal is not to memorize every implementation detail, but to be able to **explain the concept, tradeoffs, and when you'd use it**.

---

# 1. Databases and DBMS

### What is a database?

A database is a system used to **store, organize, retrieve and modify data**.

### What is a DBMS?

A **Database Management System (DBMS)** is the software that manages the database.

It provides things like:

* Data storage and retrieval
* Query execution
* Transactions
* Concurrency control
* Indexing
* Recovery
* Security
* Replication

Examples:

```text
Relational:
PostgreSQL
MySQL
Oracle

NoSQL:
MongoDB
Cassandra
DynamoDB
Redis
```

---

# 2. SQL Databases

SQL databases are generally **relational databases**.

Data is represented using:

```text
Tables
  ↓
Rows
  ↓
Columns
```

Example:

```text
Users

id | name    | email
---|---------|----------------
1  | Abhishek| a@gmail.com
2  | Rahul   | r@gmail.com
```

Relationships can be represented using keys:

```text
Users
  |
  | user_id
  ↓
Orders
```

### Strengths

* Strong relationships
* JOINs
* ACID transactions
* Strong data integrity
* Constraints
* Mature query capabilities

### Examples

* PostgreSQL
* MySQL
* Oracle

### Good use cases

* Banking
* Payments
* Booking
* Orders
* Inventory

---

# 3. NoSQL Databases

NoSQL is a broad category of databases designed around models other than the traditional relational model.

Common types:

### Key-value

```text
user123 → profile data
```

Example:

**Redis, DynamoDB**

### Document

```json
{
  "userId": 123,
  "name": "Abhishek",
  "orders": [...]
}
```

Example:

**MongoDB**

### Wide-column

Data is distributed across rows/columns and designed for large-scale distributed workloads.

Example:

**Cassandra**

### Graph

Designed around:

```text
Nodes + Edges + Relationships
```

Example:

**Neo4j**

---

# 4. SQL vs NoSQL

## How do you choose?

Don't say:

> "NoSQL is faster."

or:

> "SQL doesn't scale."

Instead:

> **"I'd choose based on the data model, access patterns, consistency, transaction and integrity requirements, followed by throughput, latency, availability and scalability."**

### Decision framework

```text
Data model
    ↓
Access patterns
    ↓
Consistency
    ↓
Transactions
    ↓
Integrity
    ↓
Throughput
    ↓
Latency
    ↓
Availability
    ↓
Scalability
```

### Data model

How naturally does the data fit:

* Relational
* Document
* Key-value
* Wide-column
* Graph

### Access patterns

How will the application query the data?

Examples:

```text
Get user by ID
Get all orders for user
Search by email
Find nearby users
```

### Consistency

How fresh does the data need to be?

### Transactions

Do multiple operations need to succeed atomically?

### Integrity

How important are:

* Constraints
* Foreign keys
* Uniqueness
* Validation

### Throughput

How many operations per second?

### Latency

How quickly must each operation complete?

### Availability

How resilient does the system need to be to failures?

### Scalability

How well can the database handle increasing:

* Traffic
* Data
* Users

---

## Typical database choices

| Requirement                | Strong candidate     |
| -------------------------- | -------------------- |
| Complex joins              | SQL                  |
| Complex transactions       | SQL                  |
| Strong integrity           | SQL                  |
| Simple key-value lookup    | Redis                |
| Document model             | MongoDB              |
| Flexible schema            | MongoDB              |
| Very high write throughput | Cassandra            |
| Massive distributed scale  | Cassandra / DynamoDB |
| Graph traversal            | Graph DB             |
| Caching                    | Redis                |

**Important:** These aren't absolute rules. Modern databases overlap heavily in capabilities.

---

# 5. Database Replication

Replication means:

> **Maintaining multiple copies of the same data on different database nodes.**

Example:

```text
             Primary
            /       \
           ↓         ↓
       Replica 1  Replica 2
```

### Why replicate?

1. High availability
2. Failover
3. Read scalability
4. Disaster recovery

---

## Read replicas

Writes go to primary:

```text
Client
  |
  ↓
Primary
```

Reads can be distributed:

```text
             ┌→ Replica 1
Application ─┼→ Replica 2
             └→ Replica 3
```

This reduces read load on the primary.

### Problem

Replication may be asynchronous.

Therefore:

```text
Primary → updated
Replica → stale temporarily
```

This creates **replication lag**.

---

# Synchronous vs Asynchronous replication

### Synchronous

Primary waits for replicas before acknowledging the write.

```text
Write
 ↓
Primary
 ↓
Replicas
 ↓
ACK
```

Pros:

* Stronger consistency

Cons:

* Higher latency
* Availability can be affected if replicas are unavailable

### Asynchronous

Primary acknowledges first:

```text
Write
 ↓
Primary
 ↓
ACK

Later:
 ↓
Replicas
```

Pros:

* Lower latency
* Better availability

Cons:

* Replication lag
* Possible data loss depending on failure/recovery design

---

# Failover

If primary fails:

```text
Primary 💥

Replica 1
Replica 2
Replica 3
```

One replica can be promoted through a **leader-election/failover mechanism**.

---

# Replication vs Sharding

### Replication

```text
Same data
 ↓
Node A
Node B
Node C
```

### Sharding

```text
Different data
 ↓
Shard 1
Shard 2
Shard 3
```

Remember:

> **Replication = copies**

> **Sharding = partitioning**

---

# 6. Indexes

An index is a data structure that helps the database **find rows more efficiently**.

Without an index:

```text
Query
 ↓
Scan row 1
Scan row 2
Scan row 3
...
```

Potentially:

```text
O(N)
```

With an appropriate index:

```text
Query
 ↓
Index
 ↓
Relevant rows
```

A common index structure is a **B-tree/B+ tree**, depending on the database.

For many equality/range lookups, the search can be much more efficient than scanning the entire table.

---

## Why do indexes make queries faster?

Because the database doesn't need to examine every row.

---

## Why not index everything?

Indexes have costs:

### Write overhead

When data changes:

```text
INSERT
UPDATE
DELETE
```

the database may also need to update indexes.

### Storage

Indexes consume additional disk/memory.

### Maintenance

More indexes mean more work for the database.

So:

> **Indexes improve read performance at the cost of additional storage and write/maintenance overhead.**

---

# 7. Normalization

Normalization organizes relational data to **reduce duplication and improve consistency**.

Instead of:

```text
Order

order_id
user_name
user_email
product
```

we can separate:

```text
Users
Orders
Products
```

and connect them using keys.

### Benefits

* Less duplicate data
* Easier updates
* Better consistency
* Better data integrity

### Cost

More JOINs may be required.

---

# 8. Denormalization

Denormalization intentionally duplicates data to improve **read performance or simplify access patterns**.

Example:

Instead of:

```text
Orders
   ↓
Users
```

we might store:

```text
Order

order_id
user_id
user_name
user_email
```

Now the application can retrieve order + user information without a JOIN.

### Benefits

* Faster reads
* Fewer JOINs
* Better read scalability

### Costs

* Duplicate data
* More storage
* Updates become more complicated
* Potential inconsistency

---

### Interview statement

> **"I'd normalize when data integrity and avoiding duplication are important, and denormalize when read performance and predictable access patterns justify duplicating data."**

---

# 9. ACID

ACID stands for:

```text
A → Atomicity
C → Consistency
I → Isolation
D → Durability
```

## Atomicity

All operations succeed or none do.

Example:

```text
Debit A
+
Credit B
```

Either:

```text
Both succeed
```

or:

```text
Both rollback
```

---

## Consistency

A transaction moves the database from one **valid state to another valid state**, respecting defined constraints/invariants.

Example:

```text
balance >= 0
```

---

## Isolation

Concurrent transactions should not interfere in ways that violate the guarantees of the chosen isolation level.

---

## Durability

Once a transaction is committed, its changes should survive failures such as crashes.

---

# 10. BASE

BASE is commonly associated with highly distributed NoSQL systems.

```text
Basically Available
Soft state
Eventually consistent
```

The idea is to favor:

* Availability
* Scalability
* Distribution

and accept that replicas may temporarily disagree.

### Important

Don't say:

> SQL = ACID
> NoSQL = BASE

That's an oversimplification.

Modern NoSQL databases can provide strong consistency and transactions, depending on the database and configuration.

---

# 11. CAP Theorem

CAP describes a distributed system under a **network partition**.

```text
C → Consistency
A → Availability
P → Partition tolerance
```

### Consistency

Every read receives the appropriate/latest value according to the system's consistency guarantee.

### Availability

Every request receives a response, even if it may not contain the latest value.

### Partition tolerance

The system continues operating despite communication failures between nodes.

---

## CAP's key idea

When a network partition occurs:

```text
P → choose C or A
```

You cannot simultaneously guarantee both strong consistency and availability across the partition.

### Example

Two nodes:

```text
Node A ←→ Node B
```

Network partition:

```text
Node A   X   Node B
```

If both continue accepting writes independently:

```text
A → value 10
B → value 20
```

they can diverge.

To preserve strong consistency, the system may have to reject/block some operations.

To remain available, it may continue serving requests with weaker consistency.

---

# 12. PACELC

PACELC extends CAP.

```text
CAP:
P → A or C
```

PACELC:

```text
P → A or C

Else → L or C
```

Meaning:

> During a partition, choose between Availability and Consistency. Otherwise, even when there is no partition, there is often a tradeoff between Latency and Consistency.

Why?

Stronger consistency may require coordination between nodes:

```text
Write
 ↓
Node A
 ↓
Node B
 ↓
ACK
```

That coordination increases latency.

---

# 13. Transactions

A transaction is:

> **A logical unit of database operations that should be treated as one unit of work.**

Example:

```text
BEGIN

Debit A
Credit B

COMMIT
```

If something fails:

```text
ROLLBACK
```

---

# 14. Isolation Levels

The four standard isolation levels:

```text
Read Uncommitted
       ↓
Read Committed
       ↓
Repeatable Read
       ↓
Serializable
```

Generally, stronger isolation provides stronger guarantees but can reduce concurrency or increase coordination costs.

---

## Dirty Read

Transaction A writes but hasn't committed.

Transaction B reads it.

```text
A → UPDATE
B → READ uncommitted value
A → ROLLBACK
```

B read data that never actually committed.

---

## Non-repeatable Read

Transaction A reads:

```text
₹5,000
```

Transaction B updates and commits:

```text
₹10,000
```

Transaction A reads again:

```text
₹10,000
```

Same transaction, same row, different value.

---

## Phantom Read

Transaction A:

```sql
SELECT * FROM orders
WHERE amount > 5000;
```

Gets:

```text
10 rows
```

Transaction B inserts a matching row and commits.

Transaction A runs the same query:

```text
11 rows
```

The new matching row is a **phantom**.

---

## Isolation table

| Isolation        | Dirty     | Non-repeatable | Phantom      |
| ---------------- | --------- | -------------- | ------------ |
| Read Uncommitted | Possible  | Possible       | Possible     |
| Read Committed   | Prevented | Possible       | Possible     |
| Repeatable Read  | Prevented | Prevented      | DB-dependent |
| Serializable     | Prevented | Prevented      | Prevented    |

---

# 15. Locks

A database can use locks to control concurrent access.

For example:

```text
Transaction A
    ↓
Lock row
    ↓
Update
    ↓
Commit
    ↓
Unlock
```

Another transaction attempting a conflicting operation may need to wait.

---

# 16. Pessimistic vs Optimistic Concurrency

## Pessimistic

Assume conflicts are likely.

```text
Lock first
 ↓
Modify
 ↓
Commit
```

Useful for highly contested resources.

Example:

> Two users booking the same seat.

---

## Optimistic

Assume conflicts are relatively rare.

Don't lock during the initial read.

Instead:

```text
Read version 10

...

UPDATE
WHERE version = 10
```

If zero rows are updated, someone else changed it.

### Interview statement

> **"Pessimistic concurrency acquires a lock before modifying data, while optimistic concurrency detects conflicts during the update using mechanisms such as version numbers or timestamps."**

---

# 17. MVCC

MVCC:

> **Multi-Version Concurrency Control**

The database maintains multiple versions of data so that transactions can read an appropriate snapshot while other transactions are modifying data.

Conceptually:

```text
Row
 ├── Version 1
 ├── Version 2
 └── Version 3
```

This allows readers and writers to work concurrently with less blocking in many situations.

### Don't confuse:

```text
MVCC
→ Multiple versions/snapshots

Optimistic concurrency
→ Detect conflict before committing update
```

They can work together but are not the same thing.

---

# 18. Distributed Transactions

A distributed transaction involves **multiple databases/services**.

Example:

```text
Booking Service
      ↓
Booking DB

Payment Service
      ↓
Payment DB
```

Problem:

```text
Payment ✅
Booking ❌
```

A normal database `ROLLBACK` cannot simply undo a successful operation in another independent database.

---

# 19. Two-Phase Commit — 2PC

2PC has a coordinator.

```text
              Coordinator
              /         \
             ↓           ↓
        Booking DB   Payment DB
```

### Phase 1 — Prepare

Coordinator asks participants:

> Can you commit?

```text
Booking → YES
Payment → YES
```

### Phase 2 — Commit

Coordinator tells everyone:

```text
COMMIT
```

If someone cannot commit:

```text
ROLLBACK
```

### Problems

* Coordination overhead
* Multiple network round trips
* Blocking
* Coordinator failure
* Reduced availability
* Higher latency

---

# 20. Saga

Saga breaks a distributed transaction into **multiple local transactions**.

Example:

```text
Reserve Seat
     ↓
Payment
     ↓
Confirm Booking
```

If something fails:

```text
Reserve Seat ✅
Payment ✅
Confirm Booking ❌
```

we execute **compensating transactions**:

```text
Refund Payment
Release Seat
```

Important:

> **Saga does not perform a database rollback across services. It performs compensating business actions.**

---

# 21. Saga: Choreography vs Orchestration

## Choreography

Services communicate through events.

```text
Booking
   ↓ event
Payment
   ↓ event
Confirmation
```

No central coordinator.

## Orchestration

A central orchestrator controls the workflow.

```text
             Orchestrator
             /     |      \
            ↓      ↓       ↓
        Booking Payment Confirmation
```

For complex workflows, orchestration can make the flow easier to understand and manage.

---

# 22. Payment succeeds, booking fails

This is a classic interview scenario.

Don't say:

> "Payment succeeded, so return success."

Instead maintain explicit states:

```text
PAYMENT = SUCCESS
BOOKING = PENDING
```

Then retry booking.

```text
Payment ✅
   ↓
Booking unavailable
   ↓
PENDING
   ↓
Retry
   ↓
CONFIRMED
```

If booking cannot eventually succeed:

```text
Booking FAILED
      ↓
Refund payment
      ↓
Payment REFUNDED
```

### Important requirements

* Durable messaging
* Retries
* Exponential backoff
* Idempotency
* Compensation
* Recovery/dead-letter mechanism
* Explicit state machine

---

# 23. Idempotency in distributed transactions

Suppose:

```text
Booking succeeds ✅
Response gets lost ❌
```

Orchestrator doesn't know booking succeeded.

It retries:

```text
Book(A1)
```

If the booking operation isn't idempotent, we could create:

```text
Booking 1
Booking 2
```

Instead use an idempotency key:

```text
request_id = ABC123
```

The Booking Service recognizes:

```text
ABC123 already processed
```

and returns the existing result.

---

# 24. Sharding

Sharding is:

> **Horizontal partitioning of data across multiple database nodes.**

Example:

```text
1 Billion users

       ↓
 ┌─────┼─────┐
 ↓     ↓     ↓
S1    S2     S3
```

Each shard contains a subset of the data.

---

# 25. Why shard?

A single database may hit limits on:

* CPU
* Memory
* Storage
* IOPS
* Network
* Throughput

Sharding allows horizontal scaling.

---

# 26. Shard Key

The shard key determines where a record goes.

Example:

```text
hash(user_id)
```

A good shard key should generally provide:

* Even data distribution
* Even traffic distribution
* High enough cardinality
* Good query locality
* Stable distribution

---

# 27. Hot Shard

Bad shard key:

```text
country
```

If 70% of traffic is from one country:

```text
India → 70% traffic 🔥
US    → 10%
UK    → 5%
...
```

One shard becomes overloaded.

This is a **hot shard/hot partition**.

---

# 28. Cross-Shard Queries

Suppose:

> Find all users whose age > 30.

If data is sharded:

```text
Shard 1 ─┐
Shard 2 ─┤
Shard 3 ─┼→ Query all → Merge results
Shard 4 ─┘
```

This is a **scatter-gather query**.

It's generally more expensive than querying one shard.

---

# 29. Cross-Shard Transactions

Suppose:

```text
Account A → Shard 1
Account B → Shard 2
```

Transfer:

```text
Debit A
Credit B
```

Now the transaction spans shards.

This becomes a distributed transaction problem.

Possible approaches include:

* 2PC
* Saga
* Application-level coordination

---

# 30. Sharding vs Replication

|                 | Sharding                | Replication                                    |
| --------------- | ----------------------- | ---------------------------------------------- |
| Data            | Different subset        | Same data                                      |
| Primary purpose | Scale dataset/workload  | Availability/read scaling                      |
| Storage         | Distributed             | Duplicated                                     |
| Writes          | Can scale horizontally  | Usually concentrated depending on architecture |
| Failover        | Not the primary purpose | Major purpose                                  |

And you can combine them:

```text
                 Database
                    |
          ┌─────────┴─────────┐
          ↓                   ↓
       Shard 1             Shard 2
          |                   |
       P  R  R              P  R  R
```

---

# 31. Consistent Hashing

A common problem with:

```text
hash(key) % N
```

is that changing `N` causes many keys to map to different nodes.

Example:

```text
hash(user123) % 4 → S2
```

After adding a node:

```text
hash(user123) % 5 → S4
```

A large proportion of keys may need remapping.

---

# 32. Hash Ring

Consistent hashing maps nodes and keys onto a ring.

```text
             S1
          ●
      ●         ●
     S4         S2
      ●         ●
          ●
             S3
```

A key is assigned to the next node clockwise.

When adding a node:

```text
Before:

S1 ───── S2 ───── S3 ───── S4


After:

S1 ───── S2 ── S5 ── S3 ───── S4
```

Only keys in the affected range need to move.

### Key point

> **Consistent hashing minimizes data movement when nodes are added or removed.**

---

# 33. Virtual Nodes

A physical server can have multiple logical positions on the hash ring.

```text
S1a  S2a  S3a  S1b  S2b  S3b  S1c  S2c  S3c
```

Where:

```text
S1a/S1b/S1c → Physical S1
```

### Why?

* Better distribution
* Less chance of hotspots
* Easier rebalancing
* More balanced ownership of the keyspace

---

# 34. Database Federation

Federation means splitting data into **independent databases based on business domains/functionality**.

Example:

```text
Application
    |
 ┌──┼──────────┐
 ↓  ↓          ↓
User DB   Order DB   Payment DB
```

Instead of:

```text
One DB
 ├── Users
 ├── Orders
 └── Payments
```

### Benefits

* Domain isolation
* Independent scaling
* Fault isolation
* Team ownership
* Reduced resource contention

### Costs

* Cross-database queries become difficult
* No simple JOIN across databases
* Distributed transactions
* More operational complexity
* More network calls

---

# 35. Federation vs Sharding vs Replication

This is worth memorizing:

```text
Replication
→ Same data copied to multiple nodes

Sharding
→ Different subsets of same dataset on different nodes

Federation
→ Different business domains stored in separate databases
```

Example:

```text
              Application
                   |
        ┌──────────┼──────────┐
        ↓          ↓          ↓
     User DB    Order DB   Payment DB
        |          |
      Shards     Shards
        |          |
      P R R      P R R
```

All three can coexist.

---

# 36. High-Value Interview Comparisons

## SQL vs NoSQL

```text
SQL
→ relationships
→ joins
→ transactions
→ integrity
→ mature relational querying

NoSQL
→ distributed workloads
→ flexible models
→ predictable access patterns
→ massive scale
→ high throughput
```

But:

> **Don't say SQL cannot scale horizontally or NoSQL cannot provide ACID.**

---

## Replication vs Sharding

```text
Replication → copies
Sharding → partitioning
```

---

## Normalization vs Denormalization

```text
Normalization
→ less duplication
→ integrity
→ more joins

Denormalization
→ duplicated data
→ fewer joins
→ faster reads
→ consistency/update complexity
```

---

## Pessimistic vs Optimistic

```text
Pessimistic
→ lock first
→ conflicts expected

Optimistic
→ don't lock initially
→ detect conflict later
```

---

## 2PC vs Saga

```text
2PC
→ global transaction
→ prepare + commit
→ stronger atomicity
→ coordination/blocking cost

Saga
→ local transactions
→ compensating actions
→ eventual consistency
→ better fit for many microservice workflows
```

---

## CAP vs PACELC

```text
CAP
→ During partition: C vs A

PACELC
→ During partition: C vs A
→ Else: C vs L
```

---

# 37. The Database Selection Framework

When designing a system, memorize this:

```text
                 Database choice
                       |
          ┌────────────┴────────────┐
          ↓                         ↓
     What is my data?         How do I use it?
          |                         |
     Data model              Access patterns
          |                         |
          └────────────┬────────────┘
                       ↓
                Correctness needs
                       |
             ┌─────────┼─────────┐
             ↓         ↓         ↓
        Consistency Transactions Integrity
                       |
                       ↓
                  Performance
                       |
             ┌─────────┼─────────┐
             ↓         ↓         ↓
         Throughput  Latency Availability
                       |
                       ↓
                  Scalability
```

The sentence to remember:

> **"I wouldn't choose a database simply based on scale or whether the data is structured. I'd first look at the data model and access patterns, then consistency, transaction and integrity requirements, followed by throughput, latency, availability and scalability. I'd then evaluate the guarantees and tradeoffs of the specific database."**

---

# 38. Must-Know Interview Questions

If you can answer these comfortably, your Chapter II foundation is strong.

### SQL / NoSQL

1. Why would you choose SQL over NoSQL?
2. When would you choose Cassandra?
3. Why is MongoDB useful for document-oriented data?
4. Is SQL unable to scale horizontally?
5. Is NoSQL always eventually consistent?

### Replication

6. Why replicate a database?
7. What are read replicas?
8. What is replication lag?
9. Synchronous vs asynchronous replication?
10. What happens when the primary fails?

### Indexes

11. Why does an index make a query faster?
12. Why can indexes hurt writes?
13. Why shouldn't you create indexes on every column?

### Transactions

14. What is a transaction?
15. Explain ACID.
16. What are isolation levels?
17. Dirty vs non-repeatable vs phantom reads?
18. What is Serializable?
19. Locks vs MVCC?
20. Optimistic vs pessimistic concurrency?

### Distributed Transactions

21. What is a distributed transaction?
22. Explain 2PC.
23. Why is 2PC problematic?
24. Explain Saga.
25. Choreography vs orchestration.
26. Payment succeeds but booking fails — what do you do?
27. Why is idempotency important?

### Sharding

28. What is sharding?
29. Sharding vs replication?
30. How do you choose a shard key?
31. What is a hot shard?
32. What is a scatter-gather query?
33. What happens when a shard becomes unavailable?
34. What are cross-shard transactions?

### Consistent Hashing

35. Why is `hash(key) % N` problematic?
36. How does consistent hashing work?
37. What are virtual nodes?
38. How does consistent hashing reduce data movement?

### Federation

39. What is database federation?
40. Federation vs sharding?
41. What are the advantages and disadvantages of federation?

---

# 39. The 15 Lines I'd Memorize

If you're short on time before the interview, these are the **highest-value statements**:

> **1.** Replication means maintaining copies of the same data; sharding means partitioning different subsets of data across nodes.

> **2.** Read replicas improve read scalability, but asynchronous replication introduces replication lag and stale reads.

> **3.** SQL databases can scale horizontally using techniques such as sharding; it's not correct to say SQL cannot scale horizontally.

> **4.** I choose SQL vs NoSQL based on data model, access patterns, consistency, transactions, integrity, throughput, latency, availability and scalability.

> **5.** An index speeds up reads by providing an efficient lookup structure, but adds storage and write/maintenance overhead.

> **6.** Normalization reduces duplication and improves integrity; denormalization trades duplication for simpler/faster reads.

> **7.** ACID provides atomicity, consistency, isolation and durability for transactions.

> **8.** Read Committed prevents dirty reads; Repeatable Read additionally prevents non-repeatable reads; Serializable provides the strongest standard isolation.

> **9.** Pessimistic concurrency locks data assuming conflicts are likely; optimistic concurrency detects conflicts using mechanisms such as version checks.

> **10.** MVCC maintains multiple versions/snapshots so readers and writers can operate concurrently with less blocking.

> **11.** CAP says that during a network partition, a distributed system must trade off consistency and availability.

> **12.** PACELC extends CAP by saying that even without a partition, there is often a consistency-versus-latency tradeoff.

> **13.** 2PC coordinates distributed participants through prepare and commit phases, but introduces coordination, blocking and availability costs.

> **14.** Saga uses multiple local transactions and compensating actions rather than a global rollback.

> **15.** Consistent hashing maps keys and nodes onto a hash ring so that adding or removing nodes causes only a relatively small portion of keys to move.

---

## Final mental model

Think of the entire chapter like this:

```text
                         DATABASE
                            |
          ┌─────────────────┼─────────────────┐
          ↓                 ↓                 ↓
        SQL               NoSQL          Federation
          |                 |                 |
     Relationships      Distribution      Domains
     Transactions       Scale             Isolation
          |
        ACID
          |
     Transactions
          |
   ┌──────┴──────┐
   ↓             ↓
Isolation     Concurrency
   |             |
   ↓          Locks / MVCC
   |
CAP / PACELC
   |
Distributed Systems
   |
   ├── Replication
   ├── Sharding
   │     ↓
   │ Consistent Hashing
   │
   └── Distributed Transactions
          |
       ┌──┴──┐
       ↓     ↓
      2PC   Saga
```

**For your system-design interviews, I'd prioritize:** **Replication → Transactions/Isolation → CAP/PACELC → Distributed Transactions/Saga → Sharding → Consistent Hashing.** Those are the areas where interviewers are most likely to keep drilling once you give the initial answer.
