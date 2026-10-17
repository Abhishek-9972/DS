### Caching
````
1. CacheAside Caching, 
2. CacheEvictionPolicies - LRU, LFU, FIFO, TTL
3. Caching Problems
    3.1 Cache Stampede : Request Coalescing, Cache Warming
    3.2 Cache Consistency : Cache Invalidation on writes, Short TTLs, Eventual Consistency
    3.3 Hot Keys : Replicate Hot Keys, Application level local cache, Rate Limiter
````

### Redis
````
1. InMemoryDataStore, stores data in RAM
2. Single Threaded
3. Rate Limiter, Distributed Locks, Aggregate Ranking, GeoSpatial Search, session storage
4. Lua Script
````

### ApiGateway
````
Request routing, authentication, rate limiting, request validation, SSL Termination
````

### Cassandra
````
1. Wide Column Database, Predictable Access Patterns, Consistent Hashing, Gossip Protocols, Tunable Consistency(One, Quoram, All), High Throughput
2. Bloom Filters
3. MemTable, SSTable
````

### ElasticSearch
````
1. Full Text Search, Fuzzy Search; supports filtering, ranking and aggregations; Auto Complete
2. Inverted Index: Java > Doc1, Doc2
3. Document, Index, Shard, Replica
4. NOT the source of truth: rebuild via reindex (alias swap) from Kafka / CDC
````

### LoadBalancer
````
- Types - (Network Layer(L4), Application Layer(L7)) - Software Hardware -
- Routing Algorithms : a) RoundRobin b) Weighted Round Robin c) Least Connections d) Least Response Time e) Least Bandwidth f) Hashing
- Features - Autoscaling, Sticky Sessions, HealthChecks, Encryption, Caching, Logging, Request Redirects, Fixed Response
- Eg : Nginx, Amazon Elastic Load Balancing
````

### Kafka
````
Saga, CQRS, Outbox Pattern(Debezium CDC), Idempotency, Kafka Delivery Semantics, Leader Election, 
Data Partitioning Strategies and Hot Partitions, Dead Letter Queue (DLQ) and Poison Messages, 
Exactly-Once Processing Semantics and Deduplication, Schema Evolution and Backward Compatibility, 
Asynchronous Processing
````


### Major Items
````
- CAP
- SAGA
- Partitioning
- Versioning
````