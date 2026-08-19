## How do you choose between SQL and NoSQL?

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