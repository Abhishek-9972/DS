Absolutely. Here's the **same interview-focused cheat sheet** for the networking/infrastructure section. I've emphasized **what you should actually say during system design**, rather than textbook definitions.

# System Design Cheat Sheet — Networking & Infrastructure

## 1. IP

| Topic          | What to say in interview                                                                 | Key point                                          |
| -------------- | ---------------------------------------------------------------------------------------- | -------------------------------------------------- |
| **IP Address** | An IP address identifies a device/interface on a network so packets can be routed to it. | IPv4 = 32-bit, IPv6 = 128-bit.                     |
| **Public IP**  | Routable over the public internet.                                                       | Usually assigned by ISP/cloud provider.            |
| **Private IP** | Used within a private network and not directly routable over the internet.               | Common inside VPC/VNet.                            |
| **IPv4**       | 32-bit address, e.g. `192.168.1.10`.                                                     | Limited address space.                             |
| **IPv6**       | 128-bit address.                                                                         | Huge address space.                                |
| **NAT**        | Translates private IPs to a public IP for internet communication.                        | Allows many private machines to share a public IP. |

### Interview one-liner

> **"An IP address identifies a network interface and allows packets to be routed between networks."**

---

# 2. OSI Model

The seven layers:

```text
7  Application
6  Presentation
5  Session
4  Transport
3  Network
2  Data Link
1  Physical
```

For system design, the most important are usually:

```text
Application → HTTP
Transport   → TCP / UDP
Network     → IP
```

| Layer            | Responsibility                    | Examples                    |
| ---------------- | --------------------------------- | --------------------------- |
| **Application**  | Application-level communication   | HTTP, DNS, SMTP             |
| **Presentation** | Encoding, encryption, compression | TLS, JSON, serialization    |
| **Session**      | Manage communication sessions     | Session management concepts |
| **Transport**    | End-to-end delivery               | TCP, UDP                    |
| **Network**      | Routing packets                   | IP                          |
| **Data Link**    | Local network delivery            | Ethernet, MAC               |
| **Physical**     | Physical transmission             | Cables, radio               |

### Easy interview explanation

> **"At a high level, HTTP operates at the application layer, TCP/UDP at the transport layer, and IP at the network layer. The lower layers handle local delivery and physical transmission."**

Don't waste interview time explaining all seven layers unless asked.

---

# 3. TCP vs UDP

|                        | TCP                            | UDP                                       |
| ---------------------- | ------------------------------ | ----------------------------------------- |
| **Connection**         | Connection-oriented            | Connectionless                            |
| **Reliability**        | Reliable delivery              | No delivery guarantee                     |
| **Ordering**           | Guarantees ordered byte stream | No ordering guarantee                     |
| **Retransmission**     | Yes                            | No                                        |
| **Flow control**       | Yes                            | No                                        |
| **Congestion control** | Yes                            | No                                        |
| **Overhead**           | Higher                         | Lower                                     |
| **Typical use**        | HTTP, databases, APIs          | DNS, gaming, streaming, real-time traffic |

### TCP

TCP provides:

* Reliable delivery
* Ordering
* Retransmission
* Flow control
* Congestion control

### UDP

UDP gives you a lightweight datagram protocol without TCP's reliability guarantees.

### Interview answer

> **"I'd use TCP when reliability and ordered delivery are important. I'd consider UDP when low latency is more important and the application can tolerate packet loss or implement its own reliability."**

### Important

Don't say:

> ❌ UDP is faster than TCP.

Better:

> ✅ **"UDP has lower protocol overhead and doesn't incur TCP's connection and retransmission mechanisms, but whether it's actually faster depends on the workload and network."**

---

# 4. DNS

DNS:

> **Domain Name System — translates domain names into IP addresses.**

Example:

```text
www.example.com
       ↓
      DNS
       ↓
142.250.x.x
```

### Typical flow

```text
Browser
   ↓
DNS Resolver
   ↓
Root
   ↓
TLD (.com)
   ↓
Authoritative DNS
   ↓
IP address
```

In practice, caching means you don't necessarily traverse all these steps every time.

---

## DNS caching

DNS responses have a **TTL**.

```text
example.com
     ↓
IP = 10.20.30.40
TTL = 300 seconds
```

The resolver can cache the answer for 300 seconds.

### Benefits

* Lower latency
* Reduced DNS traffic
* Reduced load on authoritative servers

### Tradeoff

If the IP changes, cached records may continue pointing to the old IP until TTL expires.

---

# 5. Load Balancing

A load balancer distributes incoming traffic across multiple backend servers.

```text
                 Load Balancer
                /      |      \
               ↓       ↓       ↓
             App 1   App 2   App 3
```

### Why use it?

* Distribute traffic
* High availability
* Failover
* Horizontal scaling
* Health checking

---

## Common algorithms

| Algorithm                | Idea                                          |
| ------------------------ | --------------------------------------------- |
| **Round Robin**          | Rotate requests across servers                |
| **Weighted Round Robin** | Give more traffic to stronger servers         |
| **Least Connections**    | Send to server with fewest active connections |
| **IP Hash**              | Map client IP to server                       |
| **Consistent Hashing**   | Minimize remapping when nodes change          |

---

## Health checks

Load balancer periodically checks:

```text
App 1 → healthy
App 2 → healthy
App 3 → unhealthy
```

It stops sending traffic to App 3.

### Interview statement

> **"The load balancer should perform health checks and stop routing traffic to unhealthy instances."**

---

## L4 vs L7 Load Balancer

### L4

Operates at transport layer.

```text
TCP / UDP
```

Routes based on things like:

* IP
* Port
* TCP/UDP connection

### L7

Operates at application layer.

```text
HTTP
```

Can route based on:

* URL
* Headers
* Cookies
* Hostname

Example:

```text
/api/users → User Service

/api/orders → Order Service
```

### Interview statement

> **"I'd use an L7 load balancer when I need HTTP-aware routing, while L4 is useful when I only need transport-level routing."**

---

# 6. Clustering

A cluster is a group of machines working together as a logical system.

```text
             Cluster
        ┌──────┼──────┐
        ↓      ↓      ↓
       Node   Node   Node
```

### Why clustering?

* High availability
* Fault tolerance
* Horizontal scaling
* Workload distribution

---

## Clustering vs Load Balancing

This is a common interview question.

### Clustering

> **Multiple machines work together as a system.**

### Load balancing

> **Distributes incoming requests across machines.**

They often work together:

```text
              Load Balancer
             /      |      \
            ↓       ↓       ↓
         Node 1   Node 2   Node 3
              \     |     /
                 Cluster
```

### Important distinction

A load balancer **doesn't make an application a cluster**.

It is a mechanism for distributing traffic.

---

# 7. Caching

Caching stores frequently accessed data in a faster layer.

```text
Request
   ↓
Cache
   ↓
Cache Hit → Return
   ↓
Cache Miss
   ↓
Database
```

Example:

```text
Redis
```

---

## Why caching?

* Lower latency
* Reduce database load
* Higher throughput
* Better scalability

---

## Cache hit

Data exists:

```text
Request
 ↓
Cache → Data ✅
```

## Cache miss

Data doesn't exist:

```text
Request
 ↓
Cache ❌
 ↓
DB
 ↓
Cache
 ↓
Response
```

---

# 8. Cache Eviction Policies

| Policy   | Meaning                      |
| -------- | ---------------------------- |
| **LRU**  | Remove least recently used   |
| **LFU**  | Remove least frequently used |
| **FIFO** | Remove oldest inserted item  |
| **TTL**  | Remove after expiration time |

---

# 9. Cache Strategies

## Cache-aside

Application manages cache.

```text
App
 ↓
Cache
 ↓ miss
DB
 ↓
Cache
```

Most common strategy.

### Write

```text
DB update
 ↓
Invalidate/update cache
```

---

## Write-through

Application writes to cache and cache synchronously writes to DB.

```text
App
 ↓
Cache
 ↓
DB
```

### Benefit

Cache and DB remain more synchronized.

### Cost

Higher write latency.

---

## Write-behind / Write-back

Write to cache first and persist to DB asynchronously.

```text
App
 ↓
Cache
 ↓
Later
 ↓
DB
```

### Benefit

Very fast writes.

### Risk

Data can be lost if the cache fails before persistence.

---

# 10. Cache Problems

### Cache invalidation

> **How do I ensure cached data doesn't remain stale?**

One of the hardest caching problems.

### Cache stampede

Many requests simultaneously miss the cache:

```text
Cache expires
     ↓
1000 requests
     ↓
1000 DB queries 🔥
```

Solutions include:

* Request coalescing
* Locks
* Early refresh
* Randomized TTL/jitter

### Hot key

One key receives huge traffic:

```text
celebrity_profile
      ↓
1M requests/sec 🔥
```

Can overload one cache node.

---

# 11. CDN

CDN:

> **Content Delivery Network — geographically distributed servers that cache content closer to users.**

```text
User India
    ↓
CDN India
    ↓
Origin

User US
    ↓
CDN US
    ↓
Origin
```

### Good for

* Images
* Videos
* JavaScript
* CSS
* Static files
* Cacheable API responses

### Benefits

* Lower latency
* Reduced origin load
* Higher scalability
* Better user experience

---

## CDN vs Cache

| CDN                                | Cache                          |
| ---------------------------------- | ------------------------------ |
| Distributed geographically         | Can be centralized/distributed |
| Usually closer to end users        | Usually closer to application  |
| Great for static/cacheable content | General application data       |
| Edge caching                       | Application/data caching       |

Think:

```text
Browser
   ↓
CDN
   ↓
Load Balancer
   ↓
Application
   ↓
Redis
   ↓
Database
```

---

# 12. Proxy

A proxy is an intermediary between a client and another server.

```text
Client
   ↓
Proxy
   ↓
Server
```

---

## Forward Proxy

Proxy acts on behalf of the **client**.

```text
Client → Forward Proxy → Internet
```

Example:

Corporate proxy.

The destination server sees the proxy rather than directly seeing the client.

---

## Reverse Proxy

Proxy acts on behalf of the **server**.

```text
Client → Reverse Proxy → Application
```

Examples/use cases:

* Nginx
* TLS termination
* Load balancing
* Routing
* Authentication
* Rate limiting
* Caching

### Interview statement

> **"A reverse proxy sits in front of backend services and can provide routing, TLS termination, caching, authentication and sometimes load balancing."**

---

# 13. Load Balancer vs Reverse Proxy

They overlap.

A load balancer's primary job:

> **Distribute traffic across backend instances.**

A reverse proxy can do much more:

```text
TLS termination
Routing
Authentication
Caching
Compression
Rate limiting
```

Modern infrastructure components can perform both roles.

---

# 14. Availability

Availability means:

> **The system is operational and able to serve requests when needed.**

Usually expressed as a percentage.

| Availability | Approx downtime/year |
| ------------ | -------------------: |
| 99%          |           ~3.65 days |
| 99.9%        |          ~8.76 hours |
| 99.99%       |        ~52.6 minutes |
| 99.999%      |        ~5.26 minutes |

---

## How to improve availability?

### Redundancy

Don't depend on one machine.

```text
❌ App 1

✅ App 1
   App 2
   App 3
```

### Replication

Multiple copies of data.

### Load balancing

Remove unhealthy instances from traffic.

### Multi-AZ deployment

Deploy across multiple availability zones.

```text
Region
 ├── AZ1 → App
 ├── AZ2 → App
 └── AZ3 → App
```

If AZ1 fails, traffic can go to AZ2/AZ3.

---

# 15. Availability vs Reliability

These are related but different.

### Availability

> Is the system available when requested?

### Reliability

> Does the system consistently perform correctly over time?

A system can technically be available but return incorrect responses.

---

# 16. Scalability

Scalability means:

> **The ability to handle increasing workload by adding resources.**

---

## Vertical scaling

Increase resources on one machine.

```text
4 CPU
 ↓
16 CPU
```

Pros:

* Simple
* No distributed-system complexity

Cons:

* Hardware limits
* Expensive
* Single-machine failure

---

## Horizontal scaling

Add more machines.

```text
1 server
 ↓
3 servers
 ↓
10 servers
```

Pros:

* Large scale
* Better fault tolerance
* Can distribute workload

Cons:

* Distributed-system complexity
* Coordination
* Networking
* Data partitioning

### Interview statement

> **"I'd generally prefer horizontal scaling for stateless application services because it provides better scalability and fault tolerance."**

---

# 17. Stateless vs Stateful

### Stateless service

Any instance can handle any request.

```text
Request
 ↓
LB
 ├── App 1
 ├── App 2
 └── App 3
```

No user session stored locally.

Easy to horizontally scale.

### Stateful service

Instance maintains state.

```text
User → App 1
         ↓
      Session
```

Harder to scale/failover.

Usually move state into shared storage:

```text
App instances
     ↓
 Redis / DB
```

---

# 18. Storage

Storage is where persistent data is stored.

Common categories:

| Storage                     | Best suited for                 |
| --------------------------- | ------------------------------- |
| **Block storage**           | VM disks, databases             |
| **Object storage**          | Images, videos, backups, files  |
| **File storage**            | Shared filesystem               |
| **Local/ephemeral storage** | Temporary high-performance data |

---

# 19. Object Storage

Examples:

```text
S3
```

Good for:

* Images
* Videos
* Documents
* Backups
* Logs
* Large files

Application:

```text
Client
 ↓
Application
 ↓
Object Storage
```

Often better to keep large files out of the relational DB.

---

# 20. Block Storage

Think of it as a raw disk attached to a machine.

Good for:

* Databases
* VM disks
* Applications requiring filesystem/block-level access

Example:

```text
EC2/VM
  ↓
Block Storage
```

---

# 21. File Storage

Provides a filesystem-like interface that multiple machines can potentially mount.

Useful for:

* Shared files
* Legacy applications
* Shared filesystem workloads

---

# ⭐ High-Value Interview Statements

| When                  | Say this                                                                                                                                  |
| --------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **Load balancer**     | "I'll use health checks so unhealthy instances are removed from the traffic pool."                                                        |
| **Scaling**           | "I'll keep application instances stateless so they can scale horizontally behind a load balancer."                                        |
| **Cache**             | "The cache is an optimization; the database remains the source of truth."                                                                 |
| **Cache consistency** | "I need to define how stale the cache is allowed to be and how invalidation will work."                                                   |
| **Cache stampede**    | "We need protection against many requests simultaneously hitting the DB after a cache expiry."                                            |
| **CDN**               | "Static/cacheable content can be served from the CDN to reduce latency and origin load."                                                  |
| **Reverse proxy**     | "A reverse proxy can provide routing, TLS termination, authentication, caching and rate limiting."                                        |
| **Availability**      | "I'll avoid a single point of failure by using redundancy and deploying across multiple failure domains."                                 |
| **Scaling**           | "Stateless services are easier to horizontally scale because any instance can handle a request."                                          |
| **Storage**           | "I'd use object storage for large files rather than putting them directly into the database."                                             |
| **DNS**               | "DNS caching reduces lookup latency and load, but TTL means IP changes aren't necessarily reflected immediately."                         |
| **TCP**               | "TCP gives reliable ordered delivery; I'd use UDP when the application prioritizes low overhead/latency and can tolerate or handle loss." |
| **Load balancing**    | "L7 is useful when routing decisions depend on HTTP information such as URL, hostname or headers."                                        |
| **Clustering**        | "Clustering provides multiple nodes working together; a load balancer can distribute traffic across those nodes."                         |

---

# 🔥 20 Things to Memorize

If you're revising this section immediately before an interview:

1. **IP identifies a network interface and enables packet routing.**
2. **OSI: HTTP → Application, TCP/UDP → Transport, IP → Network.**
3. **TCP = reliable, ordered delivery.**
4. **UDP = lightweight datagrams without TCP's reliability guarantees.**
5. **DNS translates domain names to IP addresses.**
6. **DNS caching improves latency but TTL creates propagation delay.**
7. **Load balancer distributes traffic and provides health-based failover.**
8. **L4 = TCP/UDP; L7 = HTTP-aware routing.**
9. **Cluster = multiple nodes working as a system.**
10. **Load balancer = distributes traffic; clustering = group of nodes.**
11. **Cache reduces latency and database load.**
12. **Cache invalidation and stale data are major tradeoffs.**
13. **Cache stampede happens when many requests simultaneously miss/expire.**
14. **CDN moves cacheable content closer to users.**
15. **Reverse proxy sits in front of backend services.**
16. **Availability comes from redundancy, replication and eliminating single points of failure.**
17. **Horizontal scaling = add machines; vertical scaling = bigger machine.**
18. **Stateless applications are easier to horizontally scale.**
19. **Object storage is ideal for large files such as images/videos/backups.**
20. **For every infrastructure component, think: bottleneck → failure → scaling → tradeoff.**

---

# 🧠 The System Design Mental Model

When you're designing a system, you can mentally walk through it like this:

```text
                         USERS
                           │
                           ↓
                         DNS
                           │
                           ↓
                          CDN
                    (if applicable)
                           │
                           ↓
                    Load Balancer
                           │
              ┌────────────┼────────────┐
              ↓            ↓            ↓
           App 1         App 2        App 3
              └────────────┼────────────┘
                           ↓
                    Cache / Redis
                           │
                           ↓
                    Database Layer
                     /          \
                Primary         Replicas
                   │
                Sharding
                   │
                   ↓
                 Storage
```

And at **every layer**, ask yourself:

> **How does it scale?**

> **What happens when it fails?**

> **Where is the bottleneck?**

> **What consistency/latency tradeoff am I making?**

> **Is there a single point of failure?**

Those five questions are often more valuable in a system-design interview than memorizing individual definitions.
