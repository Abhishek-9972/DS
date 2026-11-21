# System Design NFR Cheat Sheet

*Top 7 concepts and practical techniques to achieve each non-functional
requirement.*

> **How to use this in an interview:** Finish the HLD, revisit the
> stated NFRs, and explain only the techniques that are relevant to your
> architecture. Tie each technique to a component or design decision; do
> not recite the entire list.

## Low Latency

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Caching**           Redis / Memcached for
                                                     frequently accessed
                                                     data

                             2 **CDN**               Cache static content
                                                     closer to users

                             3 **Database Indexing** B-Tree / Hash indexes
                                                     for selective lookups

                             4 **Database            Avoid full scans;
                               Optimization**        optimize queries and
                                                     execution plans

                             5 **Denormalization**   Reduce expensive
                                                     JOINs; use
                                                     materialized views
                                                     where appropriate

                             6 **Connection          Reuse database
                               Pooling**             connections and avoid
                                                     connection setup
                                                     overhead

                             7 **Async Processing**  Kafka / message
                                                     queues for
                                                     non-critical work
                                                     outside the request
                                                     path
  ------------------------------------------------------------------------

## High Availability

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Multi-AZ            Run service and data
                               Deployment**          replicas across
                                                     availability zones

                             2 **Redundancy**        Maintain multiple
                                                     instances of critical
                                                     components

                             3 **Load Balancing**    Route requests across
                                                     healthy instances

                             4 **Health Checks**     Use readiness /
                                                     liveness checks to
                                                     detect unhealthy
                                                     instances

                             5 **Automatic           Promote or route to
                               Failover**            healthy replicas when
                                                     a node or zone fails

                             6 **Replication**       Maintain copies of
                                                     data across nodes or
                                                     zones

                             7 **Rolling /           Deploy changes
                               Blue-Green            without taking the
                               Deployments**         service offline
  ------------------------------------------------------------------------

## Strong Consistency

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **ACID Transactions** Use atomic,
                                                     consistent, isolated
                                                     and durable database
                                                     transactions

                             2 **Synchronous         Acknowledge writes
                               Replication**         after required
                                                     replicas confirm them

                             3 **Quorum Reads /      Require a read or
                               Writes**              write quorum for
                                                     replicated data

                             4 **Single-Leader       Serialize writes
                               Writes**              through one leader
                                                     per partition

                             5 **Distributed Locks** Coordinate access to
                                                     shared resources when
                                                     required

                             6 **Serializable        Prevent conflicting
                               Isolation**           transactions from
                                                     observing invalid
                                                     interleavings

                             7 **Consensus           Use Raft / Paxos for
                               Protocols**           consistent replicated
                                                     state and leader
                                                     election
  ------------------------------------------------------------------------

## Eventual Consistency

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Asynchronous        Propagate updates to
                               Replication**         replicas without
                                                     blocking the write

                             2 **Event-Driven        Publish domain events
                               Architecture**        through Kafka or
                                                     another event stream

                             3 **Outbox Pattern**    Atomically persist
                                                     business changes and
                                                     events before
                                                     publishing

                             4 **Conflict            Define deterministic
                               Resolution**          rules for concurrent
                                                     updates

                             5 **CRDTs**             Use conflict-free
                                                     data types for
                                                     supported distributed
                                                     updates

                             6 **Read Repair /       Detect and reconcile
                               Anti-Entropy**        stale replicas

                             7 **Saga Pattern**      Coordinate
                                                     multi-service
                                                     workflows with
                                                     compensating actions
  ------------------------------------------------------------------------

## High Scalability

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Horizontal          Add application
                               Scaling**             instances as traffic
                                                     grows

                             2 **Database Sharding** Partition data across
                                                     multiple database
                                                     nodes

                             3 **Stateless           Keep session state
                               Services**            outside application
                                                     instances

                             4 **Load Balancing**    Distribute incoming
                                                     requests across
                                                     instances

                             5 **Caching**           Reduce repeated
                                                     database and
                                                     downstream-service
                                                     work

                             6 **Message Queues**    Decouple producers
                                                     and consumers; absorb
                                                     traffic spikes

                             7 **Auto Scaling**      Add or remove
                                                     capacity based on
                                                     demand and resource
                                                     metrics
  ------------------------------------------------------------------------

## Fault Tolerance

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Circuit Breaker**   Stop repeatedly
                                                     calling an unhealthy
                                                     dependency

                             2 **Retry with Backoff  Retry transient
                               and Jitter**          failures without
                                                     synchronized retry
                                                     spikes

                             3 **Bulkhead Pattern**  Isolate resource
                                                     pools so one failure
                                                     does not exhaust the
                                                     whole service

                             4 **Redundancy**        Keep alternate
                                                     instances or
                                                     components available

                             5 **Automatic           Switch traffic or
                               Failover**            leadership to a
                                                     healthy replica

                             6 **Timeouts**          Bound how long a
                                                     request waits on a
                                                     dependency

                             7 **Graceful            Preserve core
                               Degradation**         functionality when
                                                     optional components
                                                     fail
  ------------------------------------------------------------------------

## High Durability

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Replication**       Keep multiple copies
                                                     of committed data

                             2 **Backups**           Take regular backups
                                                     and test restoration

                             3 **Point-in-Time       Use transaction logs
                               Recovery**            to restore to a
                                                     selected time

                             4 **Write-Ahead         Persist log records
                               Logging**             before applying
                                                     data-page changes

                             5 **Erasure Coding**    Protect data with
                                                     fragments and parity
                                                     across failure
                                                     domains

                             6 **Cross-Region        Maintain copies in
                               Replication**         another region for
                                                     regional disasters

                             7 **Checksums and       Detect corruption and
                               Integrity Checks**    verify stored data
  ------------------------------------------------------------------------

## High Throughput

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Batching**          Process multiple
                                                     records or requests
                                                     in one operation

                             2 **Partitioning**      Distribute work
                                                     across partitions and
                                                     consumers

                             3 **Queues / Streams**  Buffer and distribute
                                                     high-volume workloads

                             4 **Parallel            Execute independent
                               Processing**          tasks concurrently

                             5 **Compression**       Reduce network and
                                                     storage I/O where CPU
                                                     trade-offs make sense

                             6 **Bulk APIs**         Reduce per-request
                                                     overhead by accepting
                                                     multiple operations

                             7 **Horizontal          Add workers or
                               Scaling**             service instances to
                                                     increase processing
                                                     capacity
  ------------------------------------------------------------------------

## Reliability

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Redundancy**        Avoid dependence on a
                                                     single instance or
                                                     component

                             2 **Monitoring**        Track service health,
                                                     errors, saturation
                                                     and latency

                             3 **Automated Testing** Use unit,
                                                     integration, load and
                                                     failure-injection
                                                     tests

                             4 **Retries**           Recover from
                                                     transient errors with
                                                     bounded retry
                                                     policies

                             5 **Safe Deployments**  Use canaries, rolling
                                                     releases and quick
                                                     rollback

                             6 **Disaster Recovery** Define and test
                                                     backup, restore, RPO
                                                     and RTO procedures

                             7 **Capacity Planning** Maintain headroom for
                                                     expected peaks and
                                                     growth
  ------------------------------------------------------------------------

## Elasticity

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Auto Scaling**      Scale capacity up and
                                                     down based on demand

                             2 **Serverless**        Use managed execution
                                                     that scales with
                                                     incoming events or
                                                     requests

                             3 **Container           Use Kubernetes or
                               Orchestration**       managed orchestration
                                                     to schedule and scale
                                                     workloads

                             4 **Queue-Based         Scale consumers using
                               Scaling**             queue depth and
                                                     processing lag

                             5 **Right-Sizing**      Adjust CPU, memory
                                                     and instance sizes to
                                                     workload needs

                             6 **Scale-to-Zero**     Remove idle compute
                                                     where startup latency
                                                     is acceptable

                             7 **Resource Quotas**   Set limits to prevent
                                                     one workload from
                                                     consuming all
                                                     capacity
  ------------------------------------------------------------------------

## Security

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Authentication**    Verify user and
                                                     service identity
                                                     using OAuth 2.0 /
                                                     OIDC or mTLS

                             2 **Authorization**     Enforce RBAC / ABAC
                                                     and least-privilege
                                                     access

                             3 **Encryption in       Use TLS for network
                               Transit**             communication

                             4 **Encryption at       Encrypt databases,
                               Rest**                disks, backups and
                                                     object storage

                             5 **Rate Limiting**     Protect APIs from
                                                     abuse and excessive
                                                     requests

                             6 **WAF / Input         Filter malicious
                               Validation**          traffic and validate
                                                     untrusted input

                             7 **Secrets             Store and rotate
                               Management**          credentials using a
                                                     secrets manager
  ------------------------------------------------------------------------

## Observability

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Metrics**           Track request rate,
                                                     error rate, latency
                                                     and resource
                                                     saturation

                             2 **Centralized         Aggregate structured
                               Logging**             logs in ELK / Splunk
                                                     or a managed platform

                             3 **Distributed         Follow requests
                               Tracing**             across services using
                                                     OpenTelemetry /
                                                     Jaeger

                             4 **Alerting**          Notify responders
                                                     when actionable
                                                     thresholds or
                                                     conditions are met

                             5 **SLI / SLO           Measure service
                               Monitoring**          indicators against
                                                     reliability
                                                     objectives

                             6 **Health Checks**     Expose readiness and
                                                     liveness signals for
                                                     orchestration and
                                                     routing

                             7 **Audit Logging**     Record
                                                     security-sensitive
                                                     and business-critical
                                                     actions
  ------------------------------------------------------------------------

## Partition Tolerance (CAP)

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Replication**       Keep data copies on
                                                     multiple nodes so
                                                     service can continue
                                                     through node loss

                             2 **Quorum Strategies** Choose read/write
                                                     quorum behavior based
                                                     on consistency and
                                                     availability needs

                             3 **Partition-Aware     Define behavior when
                               Design**              nodes cannot
                                                     communicate

                             4 **Conflict            Reconcile concurrent
                               Resolution**          writes after
                                                     connectivity returns

                             5 **Leader Election**   Establish an
                                                     authoritative writer
                                                     where the design
                                                     requires one

                             6 **Timeouts and        Detect unreachable
                               Failure Detection**   peers without waiting
                                                     indefinitely

                             7 **Degraded            Choose which reads or
                               Operation**           writes remain
                                                     available during a
                                                     partition
  ------------------------------------------------------------------------

## Maintainability

  ------------------------------------------------------------------------
                            \# Concept               How to achieve it
  ---------------------------- --------------------- ---------------------
                             1 **Modular Services**  Separate
                                                     responsibilities
                                                     behind clear
                                                     interfaces

                             2 **CI / CD**           Automate build, test
                                                     and deployment
                                                     pipelines

                             3 **Infrastructure as   Version and reproduce
                               Code**                infrastructure
                                                     configuration

                             4 **Versioned APIs**    Evolve contracts
                                                     while supporting
                                                     existing clients

                             5 **Automated Testing** Catch regressions
                                                     with repeatable test
                                                     suites

                             6 **Documentation**     Maintain architecture
                                                     decisions, runbooks
                                                     and operational
                                                     guidance

                             7 **Code Quality        Use reviews,
                               Practices**           standards, static
                                                     analysis and
                                                     refactoring
  ------------------------------------------------------------------------
