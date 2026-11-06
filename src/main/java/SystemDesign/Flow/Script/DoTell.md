## CDN

I would use a CDN to distribute static and cacheable content geographically closer to users. Instead of every user request reaching our origin servers, requests are served from nearby edge locations.
CDN follows a caching mechanism. On a cache hit, it directly serves the content. On a cache miss, it fetches the content from the origin, caches it based on TTL and caching policies, and serves it to the user.
This reduces latency, origin server load, and bandwidth consumption while improving scalability and user experience. For content freshness, I would use TTL, cache invalidation, or versioned URLs.
PushCDN, PullCDN
Eg . CloudFront

### LoadBalancer

A Load Balancer distributes incoming traffic across multiple healthy backend instances. It improves availability, scalability and fault tolerance.
Types - (Network Layer(L4), Application Layer(L7)) - Software Hardware -
Routing Algorithms : a) RoundRobin b) Weighted Round Robin c) Least Connections d) Least Response Time e) Least Bandwidth f) Hashing
Features - Autoscaling, Sticky Sessions, HealthChecks, Encryption, Caching, Logging, Request Redirects, Fixed Response
Eg : Nginx, Amazon Elastic Load Balancing

### ApiGateway

- Acts as a single entry point for all client API requests.
- Routing, RateLimiting, Authentication and Authorization, Request Validation, API Versioning
- Handles cross-cutting concerns instead of implementing them in every microservice.

### Kafka

- Decouples producer and consumer so a slow or failed consumer never blocks the user-facing path
- Partition Key
- Durability - The producer uses acks=all with idempotence enabled. Replication factor is 3 with min.insync.replicas=2, so we survive one broker failure with no data loss.
- Outbox
- Consumers - “That makes delivery at-least-once, so every consumer is idempotent, deduping on event ID. Consumers run in groups and commit offsets only after processing. 
Failures go to retry topics with exponential backoff, then a DLQ, so a poison message never blocks the partition. And I watch consumer lag as the main health signal, alerting if it keeps growing.”

