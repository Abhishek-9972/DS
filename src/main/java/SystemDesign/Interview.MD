## 1. How do you choose between SQL and NoSQL?

I wouldn't choose between SQL and NoSQL simply based on scale or whether the data is structured. I'd first look at the **data model and access patterns**, then the **consistency, transaction and data-integrity requirements**, followed by **throughput, latency, availability and scalability** requirements.

If the system has **complex relationships, joins, strong transactional requirements and needs strong data integrity**, I'd generally lean toward SQL. If the workload has a **flexible data model, predictable access patterns, very high throughput, massive horizontal scalability or high availability requirements**, I'd consider an appropriate NoSQL database.

Ultimately, I'd evaluate the **specific database's consistency, transaction and scalability guarantees** rather than making assumptions based simply on whether it's SQL or NoSQL.

### Database selection framework

```text
                    Database choice
                          |
             ┌────────────┴────────────┐
             ↓                         ↓
       What is my data?          How do I use it?
             |                         |
        Data model              Access patterns
             |                         |
             └────────────┬────────────┘
                          ↓
                  Correctness needs
                          |
             ┌────────────┼────────────┐
             ↓            ↓            ↓
        Consistency   Transactions  Integrity
                          |
                          ↓
                    Performance
                          |
             ┌────────────┼────────────┐
             ↓            ↓            ↓
         Throughput    Latency     Availability
                          |
                          ↓
                     Scalability
```

### Key factors

**Data model** → How naturally does the data fit a relational, document, key-value, wide-column or graph model?

**Access patterns** → How will the application query and update the data?

**Consistency** → How fresh and synchronized does the data need to be?

**Transactions** → Do multiple operations need to succeed or fail atomically?

**Data integrity** → How do we enforce correctness through constraints and validation?

**Throughput** → How many operations per second does the system need to handle?

**Latency** → How quickly does an individual operation need to complete?

**Availability** → How resilient does the system need to be to failures?

**Scalability** → How well can the system handle increasing traffic and data?

### Typical database choices by workload

| Requirement                                | Strong candidates                      | Why                                                                         |
| ------------------------------------------ | -------------------------------------- | --------------------------------------------------------------------------- |
| **Complex joins**                          | SQL — PostgreSQL, MySQL, Oracle        | Relational model + mature query optimizer                                   |
| **Simple key-value lookup**                | Redis                                  | Extremely low-latency key-based access                                      |
| **Document-oriented data**                 | MongoDB                                | Natural document model                                                      |
| **Very high distributed write throughput** | Cassandra                              | Distributed architecture + horizontal scaling + predictable access patterns |
| **Massive distributed scale**              | Cassandra / DynamoDB / distributed SQL | Designed to distribute data and workload                                    |
| **Complex transactions**                   | SQL                                    | Mature ACID and concurrency mechanisms                                      |
| **Flexible/evolving schema**               | MongoDB                                | Document model                                                              |
| **Relationship traversal**                 | Graph databases                        | Relationships are first-class                                               |
| **Caching**                                | Redis                                  | Very low-latency in-memory access                                           |
| **Strong consistency**                     | SQL / distributed SQL / some NoSQL     | Depends on the specific database and configuration                          |

Database performance is workload-dependent. There is no universally fastest database. I'd choose the database that best matches the system's data model, access patterns, consistency, transaction, throughput, latency, availability and scalability requirements.

| Type            | Example   | Typical use                       |
| --------------- | --------- | --------------------------------- |
| **Key-value**   | Redis     | Cache, sessions                   |
| **Document**    | MongoDB   | Profiles, catalogs                |
| **Wide-column** | Cassandra | High-volume distributed workloads |
| **Graph**       | Neo4j     | Relationship-heavy queries        |

#### Cassandra — the four things to remember


* Cassandra is a good fit when I have high write throughput, massive distributed data, predictable access patterns, and high availability requirements.
* Cassandra data modeling is query-driven. I design the partition key and clustering keys based on how the application will access the data.
* Cassandra provides tunable consistency through consistency levels such as ONE and QUORUM.
* Eventual consistency is a consistency model; tunable consistency is the mechanism that allows me to choose different consistency levels.

## 2. SQL - Horizontal Scaling

SQL databases can scale horizontally using replication, partitioning and sharding. However, distributed SQL introduces challenges such as cross-shard joins, distributed transactions, rebalancing and consistency.

## 3. CAP

CAP stands for Consistency, Availability and Partition Tolerance. Consistency means every read gets the most recent successful write. Availability means every request to a non-failing node receives a response, even if that response may not contain the latest data. Partition tolerance means the system continues to operate despite a network partition where nodes cannot communicate with each other."

When a partition occurs, we cannot guarantee both consistency and availability. If we continue serving requests from both sides of the partition, we may return different or stale values, so we sacrifice consistency and choose availability. If we want to preserve consistency, we may have to reject or block requests on one side until the partition is resolved, sacrificing availability.

## 4. Consistent Hashing

"Consistent hashing is a technique used to distribute keys across multiple nodes while minimizing data movement when nodes are added or removed. Instead of using something like hash(key) % N, we map both the keys and nodes onto a hash ring. A key is assigned to the next node clockwise on the ring. When a node is added or removed, only the keys in the affected range need to be remapped, rather than a large proportion of the keys. We can use virtual nodes to improve the distribution of keys and avoid hotspots."

If they ask why not hash(key) % N

"hash(key) % N depends directly on the number of nodes. If N changes, the mapping changes for a large number of keys, causing significant data movement. Consistent hashing minimizes this movement."

If they ask virtual nodes

"Virtual nodes are multiple logical positions on the hash ring that belong to the same physical node. They help distribute the keyspace more evenly and make rebalancing easier when nodes are added or removed."

One line to remember

Consistent hashing = distribute keys across nodes while minimizing remapping when the number of nodes changes.

