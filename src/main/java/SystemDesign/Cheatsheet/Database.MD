Absolutely. Since you're preparing to **speak these in a system-design interview**, I've kept this as a **quick-revision cheat sheet**, not a textbook.

# System Design Cheat Sheet — Sections 1 & 2

## 1. Database Fundamentals

| Topic                  | What to say in interview                                                                                                                       | Key tradeoff / thing to remember                                                                              |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **Database / DBMS**    | A database stores and retrieves data; DBMS manages storage, queries, transactions, concurrency, indexes and recovery.                          | DBMS provides the mechanisms around the data.                                                                 |
| **SQL**                | Relational database with tables, relationships, joins and strong transactional capabilities.                                                   | Excellent for relationships, integrity and complex transactions.                                              |
| **NoSQL**              | Non-relational databases such as document, key-value, wide-column and graph databases.                                                         | Often useful for distributed workloads, flexible models and massive scale, but capabilities vary by database. |
| **SQL vs NoSQL**       | Choose based on **data model → access patterns → consistency → transactions → integrity → throughput → latency → availability → scalability**. | Don't say SQL = scalable / NoSQL = eventual consistency.                                                      |
| **Database selection** | "I'd choose based on the workload and requirements rather than simply asking which database is faster."                                        | Specific database guarantees matter more than SQL/NoSQL label.                                                |
| **Relationships**      | Relational DBs are strong when entities have complex relationships because joins and constraints are first-class concepts.                     | Joins can become expensive at very large distributed scale.                                                   |
| **Index**              | An index provides an efficient lookup structure so the DB doesn't have to scan every row.                                                      | Faster reads, but extra storage + slower writes/maintenance.                                                  |
| **Normalization**      | Reduce duplication by separating data into related tables.                                                                                     | Better integrity, but potentially more joins.                                                                 |
| **Denormalization**    | Intentionally duplicate data to optimize common read patterns.                                                                                 | Faster reads/fewer joins, but more storage and update complexity.                                             |

---

# 2. Replication

| Topic                        | What to say                                                                                                 | Key point                                                                        |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| **Replication**              | Maintain copies of the same data on multiple nodes.                                                         | Availability + failover + read scalability.                                      |
| **Read replicas**            | Distribute read traffic across replicas instead of sending every read to primary.                           | Asynchronous replication can cause stale reads.                                  |
| **Replication lag**          | Replica may temporarily be behind the primary.                                                              | Read-after-write can return stale data.                                          |
| **Synchronous replication**  | Primary waits for required replica acknowledgements before confirming the write.                            | Stronger consistency, but higher latency and potentially lower availability.     |
| **Asynchronous replication** | Primary acknowledges before replicas necessarily receive the write.                                         | Lower latency, but replication lag/data-loss risk depending on failure handling. |
| **Failover**                 | If primary fails, a replica can be promoted through a failover/leader-election mechanism.                   | Need to consider whether latest writes reached the promoted replica.             |
| **Replication vs sharding**  | Replication = **same data** on multiple nodes. Sharding = **different subsets** of data on different nodes. | They can be combined.                                                            |

---

# 3. ACID / BASE / Consistency

| Topic            | Interview answer                                                                                                 | Key point                                                              |
| ---------------- | ---------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| **Atomicity**    | All operations in a transaction succeed or all are rolled back.                                                  | No partial transaction.                                                |
| **Consistency**  | Transaction moves DB from one valid state to another while respecting constraints/invariants.                    | Don't confuse with distributed "read consistency".                     |
| **Isolation**    | Concurrent transactions don't interfere beyond the guarantees of the selected isolation level.                   | Isolation levels control what one transaction can observe.             |
| **Durability**   | Once committed, data survives crashes/failures.                                                                  | Usually supported using mechanisms such as transaction logs/recovery.  |
| **BASE**         | Basically Available, Soft state, Eventually consistent.                                                          | Commonly associated with highly distributed systems; not "NoSQL only". |
| **ACID vs BASE** | ACID emphasizes transactional correctness; BASE often favors availability/scalability with eventual consistency. | Modern NoSQL databases can also provide ACID transactions.             |

---

# 4. CAP / PACELC

| Topic                         | Interview answer                                                                            | Key point                                                                                 |
| ----------------------------- | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| **CAP — Consistency**         | Reads observe the appropriate/latest value according to the system's consistency guarantee. | Strong consistency is a specific guarantee.                                               |
| **CAP — Availability**        | Every request receives a response.                                                          | Response may not reflect the latest write if consistency is relaxed.                      |
| **CAP — Partition tolerance** | System continues operating despite network communication failures between nodes.            | Distributed systems must account for partitions.                                          |
| **CAP theorem**               | During a partition, you have to trade off strong consistency and availability.              | Don't say "choose any two" without mentioning the partition scenario.                     |
| **PACELC**                    | During partition: **A vs C**. Otherwise: **L vs C**.                                        | Even without failure, stronger consistency may require coordination and increase latency. |

---

# 5. Transactions & Concurrency

| Topic                      | Interview answer                                                                                       | Key point                                                     |
| -------------------------- | ------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------- |
| **Transaction**            | A logical set of DB operations treated as one unit of work.                                            | Commit or rollback.                                           |
| **Read Uncommitted**       | Can see uncommitted changes.                                                                           | Dirty reads possible.                                         |
| **Read Committed**         | Only sees committed data.                                                                              | Prevents dirty reads; non-repeatable reads can occur.         |
| **Repeatable Read**        | Repeated reads of the same row are protected from non-repeatable changes.                              | Phantom-read behavior can be DB-dependent.                    |
| **Serializable**           | Concurrent transactions behave as if executed sequentially.                                            | Strongest standard isolation; less concurrency/more overhead. |
| **Dirty read**             | Read data written by another transaction that hasn't committed.                                        | Can disappear after rollback.                                 |
| **Non-repeatable read**    | Same row returns different values within one transaction because another transaction updated it.       | Read Committed allows this.                                   |
| **Phantom read**           | Same query returns a different set of rows because another transaction inserted/deleted matching rows. | Repeatable Read behavior depends on DB implementation.        |
| **Pessimistic locking**    | Lock the resource before modifying it.                                                                 | Good when conflicts are expected.                             |
| **Optimistic concurrency** | Don't lock initially; detect conflicts during update using version/timestamp etc.                      | Good when conflicts are relatively rare.                      |
| **MVCC**                   | Maintain multiple versions/snapshots so transactions can read appropriate versions while writes occur. | Reduces reader/writer blocking in many systems.               |

---

# 6. Distributed Transactions

| Topic                                | Interview answer                                                                                                                     | Key point                                                                       |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------- |
| **Distributed transaction**          | Transaction involving multiple independent DBs/services.                                                                             | A normal local DB rollback cannot undo another service's committed transaction. |
| **2PC**                              | Coordinator asks participants to prepare, then tells them to commit/rollback.                                                        | Strong atomicity but coordination, latency and blocking costs.                  |
| **Saga**                             | Break distributed workflow into local transactions and use compensating actions when something fails.                                | Eventual consistency; no global rollback.                                       |
| **Saga choreography**                | Services communicate through events without a central coordinator.                                                                   | Can become difficult to understand/manage as workflow grows.                    |
| **Saga orchestration**               | Central orchestrator controls the workflow.                                                                                          | Easier to reason about complex workflows.                                       |
| **Payment succeeds + booking fails** | Keep explicit state such as `PAYMENT_SUCCESS + BOOKING_PENDING`, retry booking idempotently, and refund if booking ultimately fails. | Classic failure scenario.                                                       |
| **Idempotency**                      | Repeating the same request produces the same business result rather than duplicate side effects.                                     | Essential for retries/payment/booking/event processing.                         |

---

# 7. Sharding

| Topic                       | Interview answer                                                                          | Key point                                                          |
| --------------------------- | ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| **Sharding**                | Distribute different subsets of data across multiple nodes.                               | Horizontal scaling.                                                |
| **Shard key**               | Determines which shard stores a record.                                                   | Need good distribution + alignment with access patterns.           |
| **Good shard key**          | High cardinality, even distribution and avoids hotspots; ideally supports common queries. | Bad key → hot shard.                                               |
| **Hot shard**               | One shard receives disproportionately high traffic/data.                                  | Can become system bottleneck.                                      |
| **Cross-shard query**       | Query multiple shards and merge results.                                                  | Scatter-gather → higher latency/complexity.                        |
| **Cross-shard transaction** | Transaction spans multiple shards.                                                        | Becomes a distributed transaction problem.                         |
| **Sharding + replication**  | Each shard can itself have replicas.                                                      | Sharding gives scale; replication gives availability/read scaling. |

---

# 8. Consistent Hashing

| Topic                            | Interview answer                                                                      | Key point                                                            |
| -------------------------------- | ------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| **Problem with `hash(key) % N`** | Changing N changes the mapping for a large proportion of keys.                        | Causes significant data movement/remapping.                          |
| **Consistent hashing**           | Map both nodes and keys onto a hash ring; assign each key to the next node clockwise. | Adding/removing nodes affects only a relatively small range of keys. |
| **Hash ring**                    | Circular keyspace where nodes and keys are positioned using hashes.                   | Enables incremental remapping.                                       |
| **Virtual nodes**                | Multiple logical positions on the ring belong to one physical node.                   | Better distribution and easier rebalancing.                          |
| **Main benefit**                 | Minimize key movement when nodes are added/removed.                                   | Common in distributed caches/databases.                              |

### One-liner to memorize:

> **"Consistent hashing distributes keys across nodes while minimizing remapping when nodes are added or removed."**

---

# 9. Database Federation

| Topic                      | Interview answer                                                                          | Key point                                                |
| -------------------------- | ----------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| **Federation**             | Split data into independent databases based on business domains/functionality.            | Domain isolation + independent scaling.                  |
| **Example**                | User DB, Order DB, Payment DB.                                                            | Common in microservice architectures.                    |
| **Benefits**               | Independent scaling, fault isolation, ownership and reduced resource contention.          | Each domain can evolve independently.                    |
| **Problems**               | Cross-database joins and transactions become difficult.                                   | More network calls and distributed consistency problems. |
| **Federation vs Sharding** | Federation = **different domains**; Sharding = **different subsets of the same dataset**. | Very important distinction.                              |

---

# ⭐ Most Important "Show Your Knowledge" Statements

These are the ones I'd actively inject into your system-design answers when relevant.

| Situation                   | Say this                                                                                                                                              |
| --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Choosing DB**             | "I'm choosing this database based on the access patterns and consistency/transaction requirements, not simply because it's faster."                   |
| **Read replicas**           | "Read replicas improve read scalability, but asynchronous replication introduces replication lag and potentially stale reads."                        |
| **Index**                   | "This index improves our common read path, but it adds storage and write-maintenance overhead."                                                       |
| **Denormalization**         | "I'm denormalizing because this is a predictable, read-heavy access pattern; the tradeoff is duplicated data and more complex updates."               |
| **Concurrency**             | "There is a race condition here, so I need concurrency control to ensure two requests can't incorrectly modify the same resource."                    |
| **Sharding**                | "I'll choose the shard key based on even distribution and our access patterns, while avoiding hot partitions and unnecessary scatter-gather queries." |
| **Cross-shard**             | "I'll try to keep related operations on the same shard because cross-shard queries and transactions are more expensive."                              |
| **Distributed transaction** | "Since these operations span multiple services, a normal database transaction can't provide atomicity across them."                                   |
| **Saga**                    | "I'd use Saga with retries, idempotency and compensating actions rather than relying on a distributed database rollback."                             |
| **Failure scenario**        | "I'm also considering the case where one operation succeeds and the next one fails, rather than designing only for the happy path."                   |
| **Idempotency**             | "Since this operation can be retried, it needs to be idempotent so retries don't create duplicate side effects."                                      |
| **Consistency**             | "I'm accepting eventual consistency here because the business requirement doesn't require every read to immediately see the latest write."            |
| **Replication failure**     | "If the primary fails, we need failover, but we should also consider whether the promoted replica has received the latest writes."                    |
| **Consistent hashing**      | "I'd use consistent hashing to minimize key movement when nodes are added or removed."                                                                |

---

# 🔥 The 20 Things I'd Memorize

If you're doing a quick revision **30 minutes before the interview**, focus on these:

1. **SQL vs NoSQL is a workload decision, not a scale decision.**
2. **Data model + access patterns drive database choice.**
3. **Replication = copies; sharding = partitioning.**
4. **Read replicas improve read scalability but can be stale.**
5. **Synchronous replication = stronger consistency, higher latency.**
6. **Asynchronous replication = lower latency, replication lag.**
7. **Indexes speed reads but increase write/storage overhead.**
8. **Normalization reduces duplication; denormalization optimizes reads.**
9. **ACID = Atomicity, Consistency, Isolation, Durability.**
10. **Read Committed prevents dirty reads.**
11. **Serializable makes concurrent execution behave as if sequential.**
12. **MVCC uses multiple versions/snapshots for concurrency.**
13. **CAP's tradeoff applies when there's a network partition.**
14. **PACELC adds latency vs consistency even without a partition.**
15. **2PC gives distributed atomicity but has coordination/blocking costs.**
16. **Saga uses local transactions + compensating actions.**
17. **Retries require idempotency.**
18. **Good shard keys distribute both data and traffic and fit access patterns.**
19. **Consistent hashing minimizes data movement when nodes change.**
20. **Federation = separate databases by business domain.**

### Your ultimate mental model

```text
DATABASE
   │
   ├── SQL / NoSQL
   │      └── Choose based on workload
   │
   ├── Performance
   │      ├── Indexes
   │      ├── Denormalization
   │      ├── Replication
   │      └── Sharding
   │
   ├── Correctness
   │      ├── ACID
   │      ├── Transactions
   │      ├── Isolation
   │      └── Concurrency
   │
   ├── Distributed Systems
   │      ├── CAP
   │      ├── PACELC
   │      ├── Replication
   │      ├── Sharding
   │      └── Consistent Hashing
   │
   └── Microservices
          ├── Federation
          ├── 2PC
          ├── Saga
          └── Idempotency
```

**This is the version I'd keep beside you while doing system-design practice.**
