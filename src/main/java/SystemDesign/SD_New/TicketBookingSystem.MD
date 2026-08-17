# Ticket Booking System — Senior/Staff-Level System Design

Below is the design I would recommend for a **high-scale bus/movie/event ticket-booking platform** such as a redBus-style system. The design focuses on the difficult parts: **inventory correctness, temporary holds, payments, idempotency, concurrency, external inventory, failure recovery, and scalability**.

---

# 1. Requirements

## Functional

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

For operators:

* Create/update trips.
* Configure seat layouts.
* Manage inventory.
* Receive booking/cancellation updates.

---

# 2. Non-functional requirements

The most important requirements are:

### Correctness

**Never double-book a seat.**

### Availability

Search should remain available even if the booking/payment system is degraded.

### Scalability

Support millions of users and large traffic spikes.

### Low latency

Target roughly:

* Search: `< 300 ms`
* Seat availability: `< 200 ms`
* Hold: `< 200 ms`
* Booking confirmation: typically `< 2–5 sec`, depending on payment gateway.

### Durability

Once payment and booking are confirmed, the booking must not disappear.

### Idempotency

Retries must never:

* double-book a seat
* double-charge a customer
* create duplicate tickets.

---

# 3. Back-of-the-envelope estimation

Let's choose a reasonable large-scale target rather than designing for an arbitrary number.

Assume:

* **20M registered users**
* **5M bookings/day**
* Average **2.5 seats/booking**
* **12 hours/day** of significant traffic
* Peak traffic = **10× average**
* 10% of daily bookings occur during the peak hour
* Each booking involves ~10–15 internal API operations/events.

### Booking QPS

5M bookings/day:

```text
5,000,000 / 86,400
≈ 58 bookings/sec average
```

Peak:

```text
58 × 10
≈ 580 bookings/sec
```

Design for approximately:

> **1,000 booking attempts/sec**

with headroom.

### Seat operations

2.5 seats/booking:

```text
1,000 × 2.5
≈ 2,500 seat operations/sec
```

But search and seat availability traffic is much larger.

Assume:

```text
20 searches per booking
```

Then:

```text
1,000 × 20
≈ 20K searches/sec peak
```

So I'd design roughly for:

| Operation         | Peak target |
| ----------------- | ----------: |
| Search            |    ~20K QPS |
| Seat availability |  ~5–10K QPS |
| Hold              |   ~2–3K QPS |
| Booking/payment   |     ~1K QPS |
| Notifications     |       Async |

These are **design assumptions**, not actual redBus traffic.

---

# 4. High-level architecture

```text
                         ┌──────────────┐
                         │    Client    │
                         │ Web / Mobile │
                         └──────┬───────┘
                                │
                                ↓
                         ┌──────────────┐
                         │ API Gateway  │
                         └──────┬───────┘
                                │
             ┌──────────────────┼──────────────────┐
             │                  │                  │
             ↓                  ↓                  ↓
       Search Service      Inventory Service   Booking Service
             │                  │                  │
             ↓                  ↓                  ↓
       Search Index        Redis + Inventory     Booking DB
       / Search DB             Store
                                │
                                ↓
                         ┌──────────────┐
                         │    Kafka     │
                         └──────┬───────┘
                                │
                 ┌──────────────┼──────────────┐
                 ↓              ↓              ↓
           Payment Service Ticket Service Notification
                 │
                 ↓
          Payment Gateway

                         External Operator
                                │
                                ↓
                       Operator Inventory API
```

---

# 5. Important separation of responsibilities

I would separate the system into these logical domains:

### Search Service

Responsible for:

* location/date search
* filtering
* sorting
* bus/trip discovery.

It should **not** be responsible for authoritative seat locking.

---

### Inventory Service

Responsible for:

* seat availability
* temporary holds
* seat state transitions
* preventing double booking.

This is the **most consistency-sensitive service**.

---

### Booking Service

Responsible for:

* booking lifecycle
* confirmed reservations
* cancellation
* booking history.

---

### Payment Service

Responsible for:

* payment initiation
* payment status
* payment gateway integration
* payment idempotency.

---

### Notification Service

Responsible for:

* email
* SMS
* push notification.

Completely asynchronous.

---

# 6. Data model

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

## Seat inventory

Conceptually:

```text
SeatInventory
-------------
tripId
seatId
status
reservationId
```

Possible states:

```text
AVAILABLE
HELD
BOOKED
```

However, I would **not necessarily store temporary holds in the durable DB** if Redis is being used as the authoritative temporary lock.

---

# 7. Reservation

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

Statuses:

```text
PENDING
PAYMENT_IN_PROGRESS
CONFIRMED
EXPIRED
CANCELLED
FAILED
```

---

# 8. Booking

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

The booking record is **durable**.

---

# 9. The most important part — seat reservation

This is where most interview candidates make mistakes.

We don't do:

```text
READ seat
if available:
    UPDATE seat
```

because two users can race.

Instead:

> **The acquisition of a seat must be atomic.**

---

# 10. Recommended temporary hold design

Use Redis for the temporary reservation.

Example:

```text
seat:{tripId}:{seatId}
        ↓
reservationId
        ↓
TTL = 5 minutes
```

For example:

```text
SET seat:T123:A1 R123 NX EX 300
```

`NX` means:

> Only create if the key doesn't exist.

`EX 300`:

> Automatically expire after 300 seconds.

---

# 11. Double booking

### Scenario

Alice and Bob simultaneously request A1.

```text
Alice → SET seat:T123:A1 R123 NX EX 300
Bob   → SET seat:T123:A1 R456 NX EX 300
```

Redis processes each atomic command.

Only one succeeds:

```text
Alice → SUCCESS
Bob   → FAILURE
```

Therefore:

```text
A1 → HELD by R123
```

Bob gets:

```text
SEAT_UNAVAILABLE
```

### Why this works

There is no:

```text
READ → CHECK → WRITE
```

race.

The check and acquisition are atomic.

---

# 12. Hold expiry

Suppose Alice gets:

```text
seat:T123:A1 → R123
TTL = 300 seconds
```

Alice doesn't pay.

Redis automatically expires the key.

```text
HELD
  ↓
TTL expires
  ↓
No Redis key
  ↓
Seat can be held by someone else
```

### Do we need a cleanup worker?

**No, not for the temporary Redis hold.**

This is an important distinction.

The Redis TTL itself releases the temporary hold.

A reconciliation worker can still exist for **recovery and correctness auditing**, but the booking system should not depend on it to release every seat.

---

# 13. Payment failure

Alice holds:

```text
A1 → R123
```

She attempts payment.

Payment fails.

We mark:

```text
Reservation R123
→ PAYMENT_FAILED
```

We do **not need a separate seat-release operation** if the hold is represented by Redis TTL.

The TTL continues running.

Eventually:

```text
Redis key expires
```

and A1 becomes available.

We can optionally delete the key immediately after a definitive payment failure:

```text
DEL seat:T123:A1
```

but the deletion must verify ownership:

```text
seat belongs to R123
```

We must never accidentally delete another user's newer hold.

---

# 14. Payment success

Alice's payment succeeds.

We now need to convert:

```text
Temporary hold
        ↓
Permanent booking
```

Flow:

```text
Redis hold exists
        ↓
Payment SUCCESS
        ↓
Create CONFIRMED booking
        ↓
DB COMMIT
        ↓
Delete Redis hold
```

The important ordering is:

> **Persist the confirmed booking before deleting the temporary hold.**

Otherwise:

```text
DELETE Redis
   ↓
DB write fails
```

and the seat becomes available even though the booking wasn't persisted.

---

# 15. Payment success + booking service crashes

This is one of the most important failure scenarios.

Suppose:

```text
1. Alice holds A1
2. Alice pays
3. Payment gateway says SUCCESS
4. Booking service crashes
```

We now have:

```text
Payment = SUCCESS
Redis = HELD
Booking = not yet CONFIRMED
```

### Solution

Payment success should generate a **durable payment event**.

```text
Payment Service
      ↓
Payment SUCCESS
      ↓
Kafka
      ↓
Booking Service
      ↓
CONFIRM R123
```

Kafka provides durable messaging.

If Booking Service crashes:

```text
PaymentConfirmed event
```

remains available for retry.

When Booking Service comes back:

```text
consume event
     ↓
confirm booking
```

---

# 16. What if the booking service processes the event twice?

This is normal in distributed systems.

```text
PaymentConfirmed(R123)
PaymentConfirmed(R123)
```

Therefore:

> **Booking confirmation must be idempotent.**

Use:

```text
reservationId = R123
```

as a business idempotency key.

The DB can enforce uniqueness:

```text
UNIQUE(reservationId)
```

So:

```text
First event  → creates booking
Second event → finds existing booking
```

No duplicate booking.

---

# 17. Duplicate payment

This is another critical problem.

Client:

```text
POST /payment
```

Payment succeeds.

But response is lost.

Client retries:

```text
POST /payment
```

We must not charge twice.

The client generates:

```text
idempotencyKey = P123
```

Payment service stores:

```text
P123 → SUCCESS → paymentId PAY456
```

Second request:

```text
P123
```

returns the existing result.

```text
SUCCESS
paymentId = PAY456
```

No second charge.

---

# 18. Expiry race

This is subtle.

Suppose:

```text
Hold expires = 2:00:00
```

At exactly that time:

```text
Alice → Confirm
Bob   → Hold
```

You must have a well-defined atomic state transition.

The reservation must have an authoritative state and deadline.

Conceptually:

```text
if reservation.status == HELD
AND reservation.expiresAt > now
AND reservation.id == R123

    confirm
else
    reject
```

Only one state transition can win.

You should **never** have:

```text
Alice sees HELD
Bob sees AVAILABLE
```

and allow both to proceed.

---

# 19. Better handling of payment near expiry

I would introduce:

```text
HELD
   ↓
PAYMENT_IN_PROGRESS
   ↓
CONFIRMED
```

When payment starts near expiry, extend the temporary reservation for a small payment grace period.

For example:

```text
Initial hold: 5 minutes

Payment started at 4:58
       ↓
Extend hold by another 2 minutes
```

This prevents a payment transaction from racing against the original hold expiry.

But the extension itself must be **conditional on ownership**:

```text
reservationId = R123
```

so Alice can't extend someone else's hold.

---

# 20. Multiple seats

Suppose Alice wants:

```text
A1
A2
A3
```

and:

```text
A1 → available
A2 → available
A3 → unavailable
```

I recommend:

> **All-or-nothing reservation.**

Don't give Alice A1/A2 and fail A3 unless the business explicitly supports partial booking.

Otherwise you create a bad user experience.

### Atomicity problem

With Redis, we need to acquire all three seats safely.

Conceptually:

```text
A1 → R123
A2 → R123
A3 → R123
```

We need to ensure another user doesn't acquire one of them halfway through.

A Redis Lua script or another atomic reservation mechanism can perform the multi-key operation:

```text
Check A1
Check A2
Check A3

If ALL available:
    acquire ALL

Else:
    acquire NONE
```

This gives us an atomic multi-seat hold.

---

# 21. External inventory — the redBus problem

This is a very important architectural distinction.

Suppose:

```text
redBus
   ↓
Bus Operator
   ↓
Actual Seat Inventory
```

If the bus operator owns the actual inventory, then:

> **redBus cannot be the ultimate authority for the seat.**

Otherwise:

```text
redBus → A1 available
Operator → A1 available

redBus holds A1

Another channel → Operator → books A1
```

Now you have double booking.

Therefore, the authoritative lock must ultimately exist at the **inventory owner/operator system**, or there must be a strong reservation protocol between the platform and the operator.

---

# 22. External inventory architecture

```text
                 Booking Platform
                       │
                Inventory Service
                       │
                       ↓
              Operator Adapter
                       │
             ┌─────────┴─────────┐
             ↓                   ↓
       Operator API          Operator DB
```

The platform can maintain a **local cache/view** of availability for fast searching.

But when actually booking:

```text
User
 ↓
Platform
 ↓
Operator Inventory
 ↓
AUTHORITATIVE HOLD
```

The operator's system confirms:

```text
A1 → HELD
```

Then payment/booking continues.

This is particularly important because the same inventory might be sold through:

* redBus
* operator website
* physical ticket counter
* another OTA

There must be **one authoritative inventory owner**.

---

# 23. Search vs booking consistency

This leads to an important design principle.

Search results can be **eventually consistent**.

For example:

```text
Search says:
A1 available
```

But by the time the user clicks it:

```text
A1 → already booked
```

That's acceptable.

The **booking operation is the source of truth**.

Therefore:

> Search availability is advisory; reservation is authoritative.

This allows us to cache search aggressively without compromising booking correctness.

---

# 24. Database choice

For confirmed bookings, I'd prefer a durable relational database such as:

```text
PostgreSQL / MySQL
```

because booking data has strong transactional requirements.

Example:

```text
Booking
Payment
Reservation
Cancellation
```

need strong integrity guarantees.

MongoDB can also work if the access patterns and consistency requirements justify it, but I wouldn't choose MongoDB merely because the system needs scale.

---

# 25. Redis

Redis is useful for:

* Temporary seat holds
* TTL
* Rate limiting
* Caching
* Hot availability data
* Distributed coordination where appropriate.

But:

> **I would not use Redis as the permanent booking database.**

If Redis disappears, confirmed bookings must still exist in durable storage.

---

# 26. Kafka

Kafka can be used for asynchronous workflows:

```text
PaymentConfirmed
BookingConfirmed
BookingCancelled
TicketIssued
SeatReleased
```

For example:

```text
Payment
   ↓
Kafka
   ↓
Booking
   ↓
Kafka
   ↓
Notification
```

The booking path doesn't need to synchronously call every downstream system.

---

# 27. Kafka partitioning

Partition booking events by:

```text
tripId
```

or potentially:

```text
inventoryId
```

depending on the workflow.

This helps maintain ordering for events affecting the same inventory.

However, don't blindly partition by `userId` because seat/inventory consistency is usually more closely related to the **trip/inventory**.

---

# 28. Idempotency everywhere

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

Database uniqueness constraints should enforce the invariant wherever possible.

---

# 29. Failure handling

## Redis failure

If Redis becomes unavailable:

> **Stop accepting new seat holds rather than risking double booking.**

Search can continue from cached/search data.

Booking should degrade closed.

This is an important availability vs correctness decision.

---

## Kafka failure

Don't lose payment events.

Use:

* durable Kafka
* producer acknowledgements
* retries
* consumer retries
* DLQ
* reconciliation.

---

## Payment gateway failure

Don't immediately assume payment failed.

There are three states:

```text
SUCCESS
FAILED
UNKNOWN
```

For `UNKNOWN`, query the payment gateway using the idempotency/payment ID before retrying.

---

# 30. Reconciliation

Even with all of the above, financial systems need reconciliation.

Have a periodic job compare:

```text
Payment System
       ↕
Booking System
       ↕
Inventory System
```

Find cases like:

```text
Payment = SUCCESS
Booking = missing
```

or:

```text
Booking = CONFIRMED
Payment = missing
```

These become recoverable workflows.

Reconciliation is a **safety net**, not the primary mechanism for seat expiry or concurrency.

---

# 31. Cancellation

Example:

```text
CONFIRMED
    ↓
CANCEL_REQUESTED
    ↓
PAYMENT_REFUND
    ↓
CANCELLED
    ↓
INVENTORY RELEASED
```

Again, don't assume every step succeeds synchronously.

Use an idempotent workflow.

---

# 32. API design

### Search

```text
GET /trips?from=BLR&to=HYD&date=2026-08-20
```

### Get seats

```text
GET /trips/{tripId}/seats
```

### Hold seats

```text
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

### Payment

```text
POST /payments
```

### Confirm

```text
POST /reservations/{reservationId}/confirm
```

---

# 33. Overall booking flow

The final flow I'd present in an interview is:

```text
                         User
                           │
                           ↓
                    Search Service
                           │
                           ↓
                   Seat Availability
                           │
                           ↓
                  Reservation Service
                           │
                           ↓
                  ┌─────────────────┐
                  │      Redis      │
                  │ Atomic SET NX   │
                  │ TTL = 5 minutes │
                  └────────┬────────┘
                           │
                        SUCCESS
                           │
                           ↓
                       Payment
                           │
                 ┌─────────┴─────────┐
                 │                   │
              SUCCESS              FAILED
                 │                   │
                 ↓                   ↓
              Kafka              Release/
                 │               TTL expiry
                 ↓
          Booking Service
                 │
                 ↓
             Booking DB
                 │
              COMMIT
                 │
                 ↓
          Delete Redis hold
                 │
                 ↓
          Ticket Generation
                 │
                 ↓
           Notification
```

---

# 34. The 8 questions — recommended answers

## 1. Double booking

**Question:**

> Two users click the same seat at exactly the same time. Walk me through how you guarantee only one gets it.

**Recommended answer:**

> "I use an atomic conditional reservation operation. For temporary holds, Redis `SET NX EX` ensures only one request can acquire the seat. The operation combines the existence check and acquisition atomically. The successful reservation gets a unique reservation ID; all other requests fail. We don't use a separate read-then-write operation because that creates a race condition."

---

# 35. Hold expiry

**Question:**

> How do you automatically release a seat after 5 minutes?

**Recommended answer:**

> "The temporary hold is stored in Redis with a 5-minute TTL. Redis automatically removes the key after expiry. I don't depend on a cleanup worker to release the seat. A reconciliation worker can exist as a safety mechanism, but expiry correctness comes from the TTL."

### Cleanup service down?

> "Nothing critical happens. Redis TTL continues to expire independently. The cleanup worker is only for reconciliation and detecting inconsistencies."

---

# 36. Payment failure

**Question:**

> Payment fails. How does the seat become available again?

**Recommended answer:**

> "The reservation remains temporary. On a definitive payment failure, we can release the Redis hold immediately using a reservation-ID ownership check. Even if that release doesn't happen, the TTL eventually releases the seat. We never release another user's hold accidentally because the delete operation verifies the reservation ID."

---

# 37. Payment succeeds + booking service crashes

**Question:**

> Payment succeeds but the booking service crashes. What happens?

**Recommended answer:**

> "Payment success produces a durable event containing the reservation ID and payment ID. Kafka retains the event. If Booking Service crashes, it consumes the event after recovery and idempotently creates the confirmed booking. The reservation ID has a uniqueness constraint so duplicate event delivery can't create duplicate bookings. We also run reconciliation between payment and booking systems as a safety net."

This is the answer I'd particularly expect from a strong candidate.

---

# 38. Duplicate payment

**Question:**

> Client retries because payment response was lost. How do you prevent double charging?

**Recommended answer:**

> "Every payment request has an idempotency key. The payment service persists the key and the resulting payment status. If the same request is retried, we return the original payment result rather than invoking the payment gateway again. The gateway integration should also use an idempotency key where supported."

---

# 39. Expiry race

**Question:**

> Hold expires at exactly 2:00 PM while the user clicks Confirm at 2:00 PM. Who wins?

**Recommended answer:**

> "The reservation transition must be atomic. Confirmation is allowed only if the reservation is still owned by that reservation ID, is in the expected state, and has not expired. The competing transition—expiry/release versus confirmation—must be serialized so only one wins. If confirmation wins before expiry, the booking becomes confirmed; otherwise confirmation fails and the seat can be acquired by another user."

---

# 40. Multiple seats

**Question:**

> User wants A1, A2 and A3. A1 and A2 are available but A3 is taken. Do you hold A1/A2 or reject the entire request?

**Recommended answer:**

> "For normal ticket booking I'd use all-or-nothing semantics. The user requested a specific set, so I either atomically hold all three or none. With Redis, I'd use an atomic Lua script or equivalent mechanism to check and acquire all seats together. If any seat is unavailable, no seat is held."

---

# 41. External inventory

**Question:**

> Suppose redBus doesn't own the bus inventory. The actual operator owns the seats. Where does the authoritative seat lock live?

**Recommended answer:**

> "The operator's inventory system is the ultimate source of truth because the same seat can be sold through multiple channels. Our platform can cache availability for search, but during the actual reservation we must obtain an authoritative hold from the operator's inventory API or use an agreed inventory protocol. We cannot independently lock the seat only in our Redis because another sales channel could sell the same seat."

---

# 42. The architecture principle I'd emphasize

For a **12-year-experience candidate**, I would expect them to distinguish these three things:

```text
SEARCH
   ↓
Eventually consistent is acceptable

TEMPORARY HOLD
   ↓
Strong atomicity required

CONFIRMED BOOKING / PAYMENT
   ↓
Durable + strongly consistent + idempotent
```

That distinction is much more important than whether they choose **MongoDB vs PostgreSQL**, **Redis vs another cache**, or **microservices vs monolith**.

The core invariants are:

> **One seat → at most one active reservation.**

> **One payment idempotency key → at most one charge.**

> **One reservation ID → at most one confirmed booking.**

> **Confirmed booking must survive cache/infrastructure failures.**

> **External inventory owner ultimately determines whether a seat can actually be sold.**

If the candidate can design around those invariants and explain the failure cases above, that's a **strong senior/IC3-level ticket-booking design**.
