# Ticket Booking System — Senior/Staff-Level System Design

## 1. Requirements

### Functional

Users should be able to:

1. Search buses/events/trips.
2. View available seats.
3. Select one or more seats.
4. Temporarily hold seats for 5 minutes.
5. Make payment.
6. Confirm the booking.
7. Receive a ticket.
8. Cancel a booking where applicable.
9. View booking history.

Operators should be able to:

- Create/update trips.
- Configure seat layouts.
- Manage inventory.
- Receive booking/cancellation updates.

### Non-functional

- **Correctness:** Never double-book a seat.
- **Availability:** Search remains available even if booking/payment is degraded.
- **Scalability:** Support millions of users and traffic spikes.
- **Low latency:** Search <300 ms, availability <200 ms, hold <200 ms; confirmation depends partly on payment gateway.
- **Durability:** Confirmed bookings must survive infrastructure/cache failures.
- **Idempotency:** Retries must never double-book, double-charge, or create duplicate tickets.

---

# 2. Back-of-the-envelope capacity estimation

Assume:

- 20M registered users
- 5M bookings/day
- Average 2.5 seats/booking
- 12 hours/day of significant traffic
- Peak traffic = 10x average
- 10% of daily bookings occur during the peak hour
- Design target: approximately 1,000 booking attempts/sec

### Booking QPS

```text
5,000,000 / 86,400
≈ 58 bookings/sec average

58 × 10
≈ 580 bookings/sec peak
```

Design for approximately **1,000 booking attempts/sec** with headroom.

### Seat operations

```text
1,000 × 2.5
≈ 2,500 seat operations/sec
```

### Search

Assume 20 searches per booking:

```text
1,000 × 20
≈ 20K search QPS at peak
```

### Approximate design targets

| Operation | Peak target |
|---|---:|
| Search | ~20K QPS |
| Seat availability | ~5–10K QPS |
| Hold | ~2–3K QPS |
| Booking/payment | ~1K QPS |
| Notifications | Async |

These are design assumptions, not actual traffic figures for any specific company.

---

# 3. High-level architecture

```text
                         +----------------+
                         |     Client     |
                         | Web / Mobile   |
                         +-------+--------+
                                 |
                                 v
                         +---------------+
                         |  API Gateway  |
                         +-------+-------+
                                 |
             +-------------------+-------------------+
             |                   |                   |
             v                   v                   v
      +-------------+     +-------------+     +-------------+
      |   Search    |     |  Inventory  |     |   Booking   |
      |   Service   |     |   Service   |     |   Service   |
      +------+------+     +------+------+     +------+------+
             |                   |                   |
             v                   v                   v
       Search Index        Redis / Inventory     Booking DB
                              Store
                                 |
                                 v
                          +-------------+
                          |    Kafka    |
                          +------+------+
                                 |
                 +---------------+---------------+
                 |               |               |
                 v               v               v
          Payment Service   Ticket Service   Notification
                 |
                 v
          Payment Gateway

                         External Operator
                                |
                                v
                       Operator Inventory API
```

---

# 4. Service responsibilities

## Search Service

Responsible for:

- Location/date search
- Filtering
- Sorting
- Trip discovery

Search availability is **advisory**, not authoritative.

## Inventory Service

Responsible for:

- Seat availability
- Temporary holds
- Seat state transitions
- Preventing double booking

This is the most consistency-sensitive part of the system.

## Booking Service

Responsible for:

- Booking lifecycle
- Confirmed reservations
- Cancellation
- Booking history

## Payment Service

Responsible for:

- Payment initiation
- Payment status
- Payment gateway integration
- Payment idempotency

## Notification Service

Responsible for:

- Email
- SMS
- Push notification

All notification delivery should be asynchronous.

---

# 5. Data model

## Trip

```text
Trip
----
tripId
operatorId
routeId
departureTime
arrivalTime
seatLayoutId
status
```

## Reservation

```text
Reservation
-----------
reservationId
userId
tripId
seatIds[]
status
expiresAt
createdAt
```

Possible states:

```text
PENDING
PAYMENT_IN_PROGRESS
CONFIRMED
EXPIRED
CANCELLED
FAILED
```

## Booking

```text
Booking
-------
bookingId
reservationId
userId
tripId
seatIds[]
amount
paymentId
status
createdAt
```

The booking record is durable.

---

# 6. Core design principle

Separate the consistency requirements:

```text
SEARCH
  |
  +--> Eventually consistent is acceptable

TEMPORARY HOLD
  |
  +--> Strong atomicity required

CONFIRMED BOOKING / PAYMENT
  |
  +--> Durable + strongly consistent + idempotent
```

Core invariants:

```text
One seat -> at most one active reservation

One payment idempotency key -> at most one charge

One reservation ID -> at most one confirmed booking

Confirmed booking must survive cache/infrastructure failures

External inventory owner ultimately determines whether a seat can actually be sold
```

---

# 7. Temporary seat hold

The recommended design uses Redis for the temporary hold.

Example:

```text
seat:{tripId}:{seatId}
    -> reservationId
    -> TTL = 5 minutes
```

Acquire the hold atomically:

```text
SET seat:T123:A1 R123 NX EX 300
```

Where:

- `NX` means create only if the key doesn't exist.
- `EX 300` means expire after 300 seconds.

If successful:

```text
seat:T123:A1 -> R123
TTL = 300 seconds
```

Only one concurrent request can acquire the seat.

The durable database stores the confirmed booking, not every temporary hold.

---

# 8. Complete booking flow

```text
                         User
                           |
                           v
                    Search Service
                           |
                           v
                   Seat Availability
                           |
                           v
                  Reservation Service
                           |
                           v
                  +------------------+
                  |      Redis       |
                  | Atomic SET NX    |
                  | TTL = 5 minutes  |
                  +--------+---------+
                           |
                        SUCCESS
                           |
                           v
                        Payment
                           |
                +----------+----------+
                |                     |
             SUCCESS                FAILED
                |                     |
                v                     v
             Kafka                Release/
                |                TTL expiry
                v
          Booking Service
                |
                v
            Booking DB
                |
              COMMIT
                |
                v
        Delete Redis hold
                |
                v
        Ticket Generation
                |
                v
          Notification
```

---

# 9. Double booking

## Problem

Alice and Bob simultaneously try to reserve A1.

A bad implementation:

```text
READ A1
if AVAILABLE:
    UPDATE A1
```

Both can read `AVAILABLE` before either updates it.

## Recommended solution

Use an atomic conditional acquisition:

```text
Alice -> SET seat:T123:A1 R123 NX EX 300
Bob   -> SET seat:T123:A1 R456 NX EX 300
```

Result:

```text
Alice -> SUCCESS
Bob   -> FAILURE
```

The check and acquisition happen atomically.

The losing request receives `SEAT_UNAVAILABLE`.

---

# 10. Hold expiry

Suppose:

```text
seat:T123:A1 -> R123
TTL = 300 seconds
```

If the user does not pay:

```text
HELD
  |
  | TTL expires
  v
Redis key disappears
  |
  v
Seat can be held by another user
```

## Do we need a cleanup worker?

No, not for the basic temporary hold.

The Redis TTL automatically releases the temporary hold.

A reconciliation worker can still exist for safety and auditing, but the booking system should not depend on it to release every temporary seat hold.

---

# 11. Payment failure

Flow:

```text
User has R123
     |
     v
Payment attempt
     |
     v
PAYMENT_FAILED
```

The temporary Redis hold can be released immediately, provided the delete verifies that the hold still belongs to `R123`.

Otherwise, the TTL eventually releases it.

Important: never blindly execute:

```text
DEL seat:T123:A1
```

because the seat could have been reassigned to another reservation.

The release should verify ownership, for example:

```text
if seat:T123:A1 == R123:
    delete
```

This should be atomic.

---

# 12. Payment success

The conversion is:

```text
Temporary hold
      |
      v
Payment SUCCESS
      |
      v
Create CONFIRMED booking
      |
      v
DB COMMIT
      |
      v
Delete Redis hold
```

The important ordering is:

> Persist the confirmed booking before deleting the temporary Redis hold.

Do not do:

```text
DELETE Redis
   |
   v
DB write
```

because if the DB write fails after Redis is deleted, the seat could become available without a durable booking.

---

# 13. Payment succeeds but Booking Service crashes

Failure scenario:

```text
1. Alice gets seat hold
2. Alice pays
3. Payment gateway says SUCCESS
4. Booking Service crashes
```

State:

```text
Payment = SUCCESS
Redis   = HELD
Booking = not yet CONFIRMED
```

## Recommended solution

Payment success produces a durable event:

```text
Payment Service
      |
      v
Payment SUCCESS
      |
      v
Kafka
      |
      v
Booking Service
      |
      v
CONFIRM R123
```

If Booking Service crashes, the event remains in Kafka and can be retried after recovery.

Booking confirmation must be idempotent.

Use:

```text
reservationId = R123
```

as the business idempotency key and enforce:

```text
UNIQUE(reservationId)
```

Therefore duplicate event delivery is harmless.

---

# 14. Duplicate payment

Failure scenario:

```text
Client -> Payment
Payment succeeds
Response is lost
Client retries
```

Without idempotency:

```text
Charge #1 -> SUCCESS
Charge #2 -> SUCCESS
```

This is unacceptable.

## Recommended solution

Generate an idempotency key:

```text
paymentIdempotencyKey = P123
```

Persist the result:

```text
P123 -> SUCCESS -> PAY456
```

If the same request is retried:

```text
P123 -> already processed
```

Return the original result instead of charging again.

The external payment gateway should also receive an idempotency key where supported.

---

# 15. Expiry race

Scenario:

```text
Hold expires at 2:00:00 PM

Alice -> Confirm at 2:00 PM
Bob   -> Reserve at 2:00 PM
```

The system must define a deterministic winner.

The reservation transition must be atomic.

Conceptually:

```text
if reservation.status == HELD
AND reservation.id == R123
AND reservation.expiresAt > now:
    CONFIRM
else:
    REJECT
```

The competing expiry/release and confirmation operations must be serialized so only one can win.

Possible result:

```text
Confirmation wins
    -> CONFIRMED
    -> Bob cannot acquire

OR

Expiry wins
    -> hold becomes invalid
    -> Alice confirmation fails
    -> Bob may acquire
```

The important property is:

> Both users can never successfully acquire the same seat.

---

# 16. Payment near expiry

A payment can start just before the original hold expires.

Example:

```text
4:59:59 -> Payment starts
5:00:00 -> Original hold expires
5:00:02 -> Payment succeeds
```

Recommended approach:

```text
HELD
  |
  v
PAYMENT_IN_PROGRESS
  |
  v
CONFIRMED
```

When payment processing starts near expiry, extend the temporary hold for a short grace period.

The extension must be conditional on the reservation ID so another reservation cannot extend the seat.

If payment still cannot be confirmed, the system transitions to a failure/reconciliation path.

---

# 17. Multiple seats

User requests:

```text
A1
A2
A3
```

Current state:

```text
A1 -> AVAILABLE
A2 -> AVAILABLE
A3 -> HELD
```

Recommended semantics:

> All-or-nothing.

Do not hold A1/A2 if A3 cannot be acquired, unless the business explicitly supports partial booking.

## Atomic multi-seat acquisition

Use a Redis Lua script or equivalent atomic mechanism:

```text
Check A1
Check A2
Check A3

If ALL available:
    acquire ALL

Else:
    acquire NONE
```

This avoids partially held reservations.

---

# 18. External inventory

This is critical for an OTA/redBus-style platform.

Suppose the platform does not own the bus inventory.

```text
Booking Platform
       |
       v
Operator
       |
       v
Actual Seat Inventory
```

The same seat may be sold through:

- Platform A
- Platform B
- Operator website
- Physical ticket counter

Therefore the platform's Redis cannot be the ultimate authority.

## Recommended architecture

```text
                 Booking Platform
                       |
                Inventory Service
                       |
                       v
              Operator Adapter
                       |
             +---------+---------+
             |                   |
             v                   v
       Operator API          Operator DB
```

The platform can cache availability for search.

During actual booking:

```text
User
  |
  v
Platform
  |
  v
Operator Inventory
  |
  v
AUTHORITATIVE HOLD
```

The operator confirms whether the seat is actually available.

---

# 19. Search consistency

Search can be eventually consistent.

Example:

```text
Search result:
A1 -> AVAILABLE
```

User clicks A1.

Another user may have booked it milliseconds earlier:

```text
A1 -> BOOKED
```

The booking operation then fails.

This is acceptable.

Therefore:

> Search availability is advisory; reservation is authoritative.

This allows aggressive caching and high search throughput without compromising booking correctness.

---

# 20. Database

For confirmed booking data, a relational database such as:

```text
PostgreSQL / MySQL
```

is a strong default because of:

- ACID transactions
- unique constraints
- referential integrity
- transactional state changes
- mature operational tooling.

MongoDB can also work if its document model and access patterns fit the workload, but it should not be chosen merely because the system needs scale.

---

# 21. Redis

Redis is useful for:

- Temporary seat holds
- TTL
- Fast availability checks
- Caching
- Rate limiting
- Distributed coordination where appropriate.

Do not use Redis as the permanent store for confirmed bookings.

If Redis disappears, confirmed bookings must remain available in durable storage.

---

# 22. Kafka

Use Kafka for asynchronous events such as:

```text
PaymentConfirmed
BookingConfirmed
BookingCancelled
TicketIssued
SeatReleased
```

Example:

```text
Payment
   |
   v
Kafka
   |
   v
Booking
   |
   v
Kafka
   |
   +--> Notification
   |
   +--> Ticket
```

This prevents the booking request from synchronously depending on every downstream system.

---

# 23. Kafka partitioning

For booking/inventory events, partitioning by:

```text
tripId
```

or an appropriate inventory identifier can help preserve ordering for events affecting the same inventory.

Avoid choosing a partition key solely because it is convenient. The key should match the ordering/consistency requirements.

---

# 24. Idempotency

Every externally retried operation should have an idempotency key.

Examples:

```text
Hold:
reservationId

Payment:
paymentId / idempotencyKey

Booking:
reservationId

Cancellation:
cancellationId
```

Use database uniqueness constraints wherever possible.

---

# 25. Failure handling

## Redis failure

Recommended behavior:

> Stop accepting new seat holds rather than risking double booking.

Search can continue from cached/search data.

Booking should fail closed when the system cannot guarantee inventory correctness.

## Kafka failure

Use:

- Durable Kafka
- Producer acknowledgements
- Retries
- Consumer retries
- Dead-letter queues
- Reconciliation

## Payment gateway failure

Do not automatically assume failure.

Payment should have states such as:

```text
SUCCESS
FAILED
UNKNOWN
```

For `UNKNOWN`, query the payment gateway using the payment/idempotency ID before retrying.

---

# 26. Reconciliation

Even with all the mechanisms above, financial systems should have reconciliation.

Compare:

```text
Payment System
      |
      v
Booking System
      |
      v
Inventory System
```

Find cases such as:

```text
Payment = SUCCESS
Booking = missing
```

or:

```text
Booking = CONFIRMED
Payment = missing
```

Reconciliation is a **safety net**, not the primary mechanism for seat expiry or concurrency control.

---

# 27. Cancellation

Example:

```text
CONFIRMED
    |
    v
CANCEL_REQUESTED
    |
    v
PAYMENT_REFUND
    |
    v
CANCELLED
    |
    v
INVENTORY_RELEASED
```

The cancellation workflow must also be idempotent.

---

# 28. API design

## Search

```http
GET /trips?from=BLR&to=HYD&date=2026-08-20
```

## Seat availability

```http
GET /trips/{tripId}/seats
```

## Hold seats

```http
POST /reservations
```

Request:

```json
{
  "tripId": "T123",
  "seatIds": ["A1", "A2"],
  "idempotencyKey": "R123"
}
```

Response:

```json
{
  "reservationId": "R123",
  "expiresAt": "2026-08-20T15:05:00Z",
  "amount": 1500
}
```

## Payment

```http
POST /payments
```

## Confirm

```http
POST /reservations/{reservationId}/confirm
```

---

# 29. Complete state machine

```text
                         +-----------+
                         | AVAILABLE |
                         +-----+-----+
                               |
                         Atomic Hold
                               |
                               v
                         +-----------+
                         |   HELD    |
                         +-----+-----+
                               |
                    +----------+----------+
                    |                     |
              Payment starts          Timeout
                    |                     |
                    v                     v
           +------------------+      +---------+
           | PAYMENT_PENDING  |      | EXPIRED |
           +--------+---------+      +---------+
                    |
              Payment SUCCESS
                    |
                    v
              +-----------+
              | CONFIRMED |
              +-----------+
                    |
                 Cancel
                    |
                    v
              +-----------+
              | CANCELLED |
              +-----------+
```

---

# 30. Final booking sequence

```text
1. User searches trips.
2. Search Service returns cached/indexed results.
3. User requests seat map.
4. User selects seats.
5. Reservation Service atomically acquires temporary holds.
6. Redis stores the holds with a 5-minute TTL.
7. User initiates payment.
8. Reservation enters PAYMENT_IN_PROGRESS.
9. If needed, hold receives a short payment grace extension.
10. Payment Service calls payment gateway with idempotency key.
11. Payment succeeds.
12. Payment Service emits durable PaymentConfirmed event.
13. Booking Service consumes the event.
14. Booking Service verifies reservation ownership/state.
15. Booking is persisted transactionally.
16. Booking uniqueness constraint prevents duplicates.
17. Redis hold is deleted after durable booking commit.
18. Ticket generation happens asynchronously.
19. Notification happens asynchronously.
20. Reconciliation continuously checks payment, booking, and inventory consistency.
```

---

# 31. Interview answers — concise version

## 1. Double booking

> "I use an atomic conditional reservation operation. For temporary holds, Redis `SET NX EX` ensures only one request can acquire the seat. The operation combines the existence check and acquisition atomically. The successful reservation gets a unique reservation ID; competing requests fail."

## 2. Hold expiry

> "The temporary hold is stored in Redis with a 5-minute TTL. Redis automatically removes the key after expiry. I don't depend on a cleanup worker for seat release. A reconciliation worker can exist as a safety mechanism."

### Cleanup service down?

> "Nothing critical happens. Redis TTL continues independently. The cleanup/reconciliation worker is not the correctness mechanism for expiry."

## 3. Payment failure

> "On definitive payment failure, we can release the Redis hold using an atomic ownership check. If that release fails, the TTL eventually releases the seat."

## 4. Payment succeeds + booking service crashes

> "Payment success produces a durable event containing reservation ID and payment ID. Kafka retains it. Booking Service consumes it after recovery and idempotently creates the confirmed booking. A uniqueness constraint on reservation ID prevents duplicate bookings. Reconciliation is the safety net."

## 5. Duplicate payment

> "Every payment has an idempotency key. The payment service persists the key and result. A retry returns the existing result instead of charging again. The gateway should also receive the idempotency key where supported."

## 6. Expiry race

> "Confirmation and expiry must be serialized through an atomic state transition. Confirmation succeeds only if the reservation still belongs to that reservation ID and has not expired. Either confirmation or expiry wins, never both."

## 7. Multiple seats

> "I use all-or-nothing semantics. If the user requests A1, A2 and A3, I atomically acquire all three or none. A Redis Lua script or equivalent atomic mechanism can implement this."

## 8. External inventory

> "If the operator owns the inventory, the operator's inventory system is the ultimate source of truth. Our platform can cache availability for search, but the actual hold must be authorized by the operator. Otherwise another sales channel could sell the same seat."

---

# 32. What I would expect from a 12-year candidate

The important thing is not whether the candidate says:

- Redis
- Kafka
- MongoDB
- PostgreSQL
- Microservices

The candidate should be able to reason about the **invariants and failure modes**:

```text
                CORRECTNESS
                    |
       +------------+------------+
       |            |            |
       v            v            v
  Double-book   Double-charge  Duplicate
     seat                         booking
       |            |            |
       v            v            v
 Atomic hold    Idempotency    Unique key
       |
       v
  Temporary TTL
       |
       v
 Durable confirmation
       |
       v
 Event-driven recovery
       |
       v
 Reconciliation
```

A strong senior engineer should also recognize the critical distinction:

> **Redis is responsible for fast, temporary inventory holds; the durable database is responsible for confirmed bookings; the external inventory owner is authoritative when inventory is shared across multiple sales channels.**

That distinction, combined with **atomicity + idempotency + durable events + reconciliation**, is the core of a robust ticket-booking design.
