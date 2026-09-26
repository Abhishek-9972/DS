Absolutely. In a system design interview, **Non-Functional Requirements (NFRs)** are basically answering:

> **“How well does the system need to work?”**

Whereas functional requirements answer:

> **“What does the system do?”**

### 1. Functional vs Non-functional

For a **food-delivery system**:

**Functional**

* User can search restaurants.
* User can place an order.
* Restaurant can accept/reject order.
* User can track delivery.

**Non-functional**

* Search should respond within 200 ms.
* System should support 100K requests/sec.
* System should be highly available.
* Orders should not be lost.
* System should scale during lunch/dinner peaks.

---

# What should YOU say in an interview?

Don't just say:

> "The system should be scalable, reliable and highly available."

That's too generic.

Instead, establish **measurable NFRs**.

A good framework is:

### **L A S C S**

**Latency → Availability → Scale → Consistency → Security**

And depending on the system, add:

**Durability / Reliability / Fault tolerance / Observability / Cost**

---

## 1. Latency

Ask:

> "What response time do we expect?"

For example:

```text
Read APIs:
p95 < 200 ms
p99 < 500 ms

Write APIs:
p95 < 300 ms
```

Don't obsess over exact numbers. You're establishing an engineering target.

You can say:

> "I'll assume p95 latency of 200 ms for normal read APIs unless the business requires something stricter."

### Why p95/p99?

Average latency can hide bad experiences.

Suppose:

```text
99 users → 100 ms
1 user  → 10 seconds
```

Average might still look reasonable, while one user has a terrible experience.

So system-design interviews commonly discuss:

```text
p50 → median
p95 → 95% requests are faster than this
p99 → 99% requests are faster than this
```

---

# 2. Availability

Ask:

> "How available does the system need to be?"

For example:

```text
99.9% availability
```

means approximately:

```text
~8.76 hours downtime/year
```

While:

```text
99.99%
```

means approximately:

```text
~52.6 minutes/year
```

And:

```text
99.999%
```

means approximately:

```text
~5.26 minutes/year
```

So don't casually say **"five nines"** unless the business actually needs it.

You should connect availability to business impact.

For example:

> "For a payment system, availability is critical because failed transactions directly impact revenue. I'll target 99.99% availability."

---

# 3. Scalability

This is where you estimate traffic.

Don't simply say:

> "The system should be scalable."

Instead:

> "Let's estimate the expected traffic."

For example:

```text
100M users
10M DAU
5 requests/user/day

= 50M requests/day

50M / 86,400
≈ 579 requests/sec average
```

Then account for peak traffic:

```text
Peak ≈ 5 × average

≈ 3K RPS
```

Now your architecture has an actual target.

You can say:

> "I'll design the system for around 3K peak RPS, with the ability to horizontally scale beyond that."

---

# 4. Consistency

This is **very important**.

Ask:

> "Does the system require strong consistency, or is eventual consistency acceptable?"

Different operations can have different requirements.

Example: **ticket booking**

```text
Seat availability → strong consistency
Payment status    → strong consistency
Recommendations   → eventual consistency
Analytics         → eventual consistency
```

You should avoid saying:

> "We need strong consistency everywhere."

That can unnecessarily hurt scalability and availability.

Instead:

> "I'll use strong consistency for operations where stale data can cause incorrect business outcomes, such as booking a seat. Eventual consistency is acceptable for non-critical data such as recommendations."

---

# 5. Durability / Reliability

Ask:

> "Can we ever lose data?"

For something like banking:

```text
Data loss = unacceptable
```

You might require:

```text
durable persistence
replication
backups
disaster recovery
```

For example:

> "Payment records must not be lost even if a node or availability zone fails."

This requirement influences your architecture.

You may then choose:

```text
DB replication
+
durable storage
+
backup
+
cross-AZ replication
```

---

# 6. Fault Tolerance

Ask:

> "What happens when a component fails?"

For example:

```text
Application server dies
        ↓
Load balancer
        ↓
Another server
```

Or:

```text
Kafka broker fails
        ↓
Replica takes over
```

Or:

```text
DB primary fails
        ↓
Replica/failover
```

In an interview, you can say:

> "The system should tolerate failure of a single application instance and ideally an availability zone without causing a complete service outage."

---

# 7. Security

Depending on the system:

```text
Authentication
Authorization
Encryption
Data privacy
Rate limiting
Audit logging
```

For example:

> "Users should only be able to access resources they are authorized to access, and sensitive data should be encrypted both in transit and at rest."

For a banking/payment system, this becomes much more important.

---

# 8. Observability

Often forgotten by candidates.

You can mention:

```text
Metrics
Logs
Tracing
Alerts
```

For example:

> "We should be able to monitor latency, error rate, throughput, consumer lag and dependency failures."

For your **Kafka** example:

```text
Kafka consumer lag
      ↓
monitoring
      ↓
alert if lag crosses threshold
```

That's a very good practical NFR.

---

# 9. Cost

Sometimes cost is an NFR/trade-off rather than a hard requirement.

For example:

> "We should support the expected traffic without unnecessarily over-provisioning infrastructure."

This becomes important when choosing:

```text
Redis vs DB
Kafka vs direct communication
S3 vs database
more replicas vs fewer replicas
```

---

# The interview answer I want you to memorize

When the interviewer says:

> **"What are your non-functional requirements?"**

You can say:

> **"Before designing, I'd like to establish the key NFRs: expected scale and peak traffic, latency targets, availability, consistency requirements, durability, fault tolerance, security, and observability. I'll quantify the important ones where possible."**

Then go through:

```text
1. Scale
   → DAU / requests per second / peak traffic

2. Latency
   → p95 / p99 target

3. Availability
   → 99.9 / 99.99 etc.

4. Consistency
   → strong vs eventual

5. Durability
   → can data loss be tolerated?

6. Fault tolerance
   → node / AZ / dependency failure

7. Security
   → authentication / authorization / encryption

8. Observability
   → metrics / logs / tracing / alerts
```

### One important interview trick

**Don't spend 10 minutes discussing NFRs.**

For a 45-minute system-design interview, something like:

```text
Requirements:        5 min
Capacity estimation: 5 min
High-level design:  10 min
Deep dive:          15 min
Trade-offs:          5 min
```

is much better.

And **NFRs should drive your architecture**.

For example:

```text
High traffic
   ↓
Partitioning / sharding / horizontal scaling

Low latency
   ↓
Caching / indexes / async processing

High availability
   ↓
Replication / multiple AZs / failover

Strong consistency
   ↓
Transactions / consensus / appropriate DB

High durability
   ↓
Replication / durable storage / backups
```

That last connection—**NFR → architectural decision**—is what separates a memorized system-design answer from a senior-level answer.
