
| Topic | Notes |
|---|---|
| **IP** | An IP address identifies a device/interface on a network so packets can be routed to it.<br><br>**Key Concepts:** Public IP, Private IP, IPv4 (32-bit), IPv6 (128-bit).<br><br>**Example:** `142.250.195.36` → Public IPv4 address of a server |
| **OSI** | **Layers:** Application, Presentation, Session, Transport, Network, Data Link, Physical.<br><br>**Key Concepts:** Application → HTTP<br>Transport → TCP/UDP<br>Network → IP<br><br>**Example:** Browser → **HTTP** → **TCP** → **IP** → Network |
| **TCP vs UDP** | TCP is connection-oriented and reliable, while UDP is connectionless and faster with no delivery guarantee.<br><br>![TcpUdp.png](Global/Chapter1/TcpUdp.png)<br><br>**Key Concepts:** TCP → Reliable, ordered delivery; UDP → Low latency, no delivery guarantee.<br><br>**Examples:** TCP → HTTPS, DB connections; UDP → DNS, video streaming. |
| **DNS** | DNS (Domain Name System) translates human-readable domain names into IP addresses.<br><br>![A01.png](../diagrams/A01.png)<br><br>**Key Concepts:** Domain → IP, hierarchical, distributed, caching.<br><br>**Example:** `google.com` → `142.250.195.36` |
| **Load Balancer** | A load balancer distributes incoming client requests across multiple backend servers.<br><br>![LoadBalancing.png](Global/Chapter1/LoadBalancing.png)<br><br>![L4vsL7.png](Global/Chapter1/L4vsL7.png)<br><br>**Key Concepts:** Traffic distribution, high availability, failover, horizontal scaling, health checking.<br><br>**Important:** The load balancer performs health checks and stops routing traffic to unhealthy instances. |
| **Clustering** | A cluster is a group of machines working together as a logical system.<br><br>**Purpose:** High availability, fault tolerance, and scalability. |
| **Caching** | Caching stores frequently accessed data in a faster storage layer to reduce latency and database load.<br><br>![Caching.png](Global/Chapter1/Caching.png)<br><br>![img_8.png](../KeyTechnologies/images/Caching/img_8.png)<br><br>![CacheProblems.png](Global/Chapter1/CacheProblems.png)<br><br>**Read:** Cache First → DB on Miss → Populate Cache.<br>**Write:** DB First → Invalidate Cache. |
| **CDN** | CDN (Content Delivery Network) consists of geographically distributed servers that cache content closer to users.<br><br>![CDN.png](Global/Chapter1/CDN.png)<br><br>**Key Concepts:** Push CDN, Pull CDN, edge locations, reduced latency. |
| **Proxy** | A proxy is an intermediary between a client and another server.<br><br>![ForwardReverseProxy.png](Global/Chapter1/ForwardReverseProxy.png)<br><br>**Types:** Forward Proxy → Client-side; Reverse Proxy → Server-side.<br><br>**Uses:** Security, routing, caching, load balancing, and hiding internal servers. |
| **Scalability** | Scalability is the ability of a system to handle increasing load by adding resources.<br><br>![Scalability.png](Global/Chapter1/Scalability.png)<br><br>**Types:** Vertical Scaling → Increase resources of one machine; Horizontal Scaling → Add more machines.<br><br>**Key Concept:** Horizontal scaling is commonly preferred for distributed systems. |
| **Stateless vs Stateful** | **Stateless:** Server does not store client session/context. Example → REST APIs, JWT.<br>**Stateful:** Server stores client session/context between requests.<br><br>**Key Concept:** Stateless systems are generally easier to scale horizontally because any server can handle a request. |
| **Block vs File vs Object Storage** | **Block Storage:** Data is divided into blocks; commonly used for disks, VMs, and databases.<br>**File Storage:** Data is organized as files and directories; commonly used for shared file systems.<br>**Object Storage:** Data is stored as objects with metadata and unique identifiers; commonly used for images, videos, backups, and large unstructured data.<br><br>**Examples:** Block → EBS, File → EFS, Object → S3. |

# Database

| Topic | Notes / Image / Example |
|---|---|
| **SQL vs NoSQL** | **Key Concept:** I wouldn't choose between SQL and NoSQL simply based on scale or whether the data is structured. I'd first look at the data model and access patterns, then consistency, transaction and data-integrity requirements, followed by throughput, latency, availability and scalability requirements.<br><br>![DBChoice.png](Global/Database/DbChoice.png)<br><br>![SqlvsNoSql.png](Global/Database/SqlvsNoSql.png)<br><br>![NoSql.png](Global/Database/NoSql.png) |
| **Indexing** | An index is an additional data structure maintained by the database to make queries faster. Instead of scanning every row, the database can use the index to locate relevant rows efficiently.<br><br>B-Tree indexes are commonly used for equality, range, and ordering queries. The trade-off is that indexes consume additional storage and add overhead to inserts, updates, and deletes.<br><br>For composite indexes, column order matters because of the leftmost-prefix behavior, so I design indexes based on the application's actual query patterns. |
| **Replication** | ![Replication.png](Global/Database/Replication.png) |
| **ACID** | ![ACID.png](Global/Database/ACID.png) |
| **PACELC** | **Partition:** Choose Availability or Consistency.<br>**Else:** Choose Latency or Consistency. |
| **Isolation Levels** | ![IsolationLevel.png](Global/Database/IsolationLevel.png) |
| **Normalization** | Organizing data into multiple related tables to reduce data duplication and avoid data anomalies.<br><br>![Normalization.png](Global/Database/Normalization.png) |
| **Denormalization** | Denormalization is a database optimization technique where you intentionally add redundant data to a table to speed up read operations and simplify complex queries. |
| **Optimistic vs Pessimistic Lock** | ![OptimisticLock.png](Global/Database/OptimisticLock.png) |
| **Distributed Transactions** | A transaction involving multiple independent databases/services. |
| **2PC** | ![TwoPhaseCommit.png](Global/Database/TwoPhaseCommit.png)<br><br>Two-phase commit: A system that voted yes and then loses the coordinator is **uncertain**. It can't commit, because another system might have voted no. It can't abort either, because the coordinator might have decided to commit. It has to block until the coordinator recovers. |
| **3PC** | ![ThreePhase.png](Global/Database/ThreePhase.png)<br><br>![3Pc.png](Global/Database/3Pc.png) |
| **SAGA Design Pattern** | ![SAGA.png](Global/Database/SAGA.png) |
| **SAGA Choreography** | ![Choreography.png](Global/Database/Choreography.png) |
| **SAGA Orchestrator** | ![Orchestrator.png](Global/Database/Orchestrator.png) |
| **Orchestrator vs Choreography** | ![ChoreographyVsOrchestrator.png](Global/Database/ChoreographyVsOrchestrator.png) |
| **Sharding** | ![Sharding.png](Global/Database/Sharding.png) |
| **Consistent Hashing** | ![ConsistentHashing.png](Global/Database/ConsistentHashing.png)<br><br>![ConsistentHashing2.png](Global/Database/ConsistentHashing2.png)<br><br>![ConsistentHashing3.png](Global/Database/ConsistentHashing3.png) |
| **Database Federation** | ![DF.png](Global/Database/DF.png) |
