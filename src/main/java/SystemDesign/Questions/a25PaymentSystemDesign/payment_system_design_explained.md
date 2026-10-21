# Payment System — System Design Explained

> **Pipeline in one line:** Idempotency gate → async saga worker → immutable double-entry ledger → compensation → reconciliation

**Core guarantee:** *Exactly-once **effect*** = safe retries + immutable ledger + compensation + reconciliation.

---

## Table of Contents

1. [The Problem and the Key Insight](#1-the-problem-and-the-key-insight)
2. [High-Level Design (Panel 1)](#2-high-level-design-panel-1)
3. [How the Design Was Built Up: 4 Fixes](#3-how-the-design-was-built-up-4-fixes)
4. [Step-by-Step Payment Flow (Panel 2)](#4-step-by-step-payment-flow-panel-2)
5. [The Double-Entry Ledger (Panel 3)](#5-the-double-entry-ledger-panel-3)
6. [In-Doubt: A Timeout Is Not a Failure (Panel 4)](#6-in-doubt-a-timeout-is-not-a-failure-panel-4)
7. [Sizing, Data Model and Variants (Panel 5)](#7-sizing-data-model-and-variants-panel-5)
8. [Failure Scenario Matrix](#8-failure-scenario-matrix)
9. [Monitoring and Alerting](#9-monitoring-and-alerting)
10. [Common Questions and Pitfalls](#10-common-questions-and-pitfalls)
11. [Summary Cheat Sheet](#11-summary-cheat-sheet)

---

## 1. The Problem and the Key Insight

A payment system moves money between a **payer** and a **payee**, using an **external provider** (a card network or bank) that you do not control. The hard part is not throughput; it is **correctness under failure**:

- The client may retry a request (double-click, network blip).
- Your server may crash mid-payment.
- The provider may charge the card but the response never reaches you.
- Your database and the provider's system are **two separate systems**. No single transaction can span both.

### Why "no shared transaction"?

The provider cannot join your DB transaction. You cannot do:

```
BEGIN;
  charge_card_at_provider();   -- impossible to roll back with your DB
  UPDATE balance ...;
COMMIT;
```

So **exactly-once is engineered**, not provided by any one component. It is the combination of:

| Mechanism | Problem it solves |
|---|---|
| Idempotency | Duplicate requests / retries causing double charges |
| Immutable double-entry ledger | Lost updates, missing audit trail, silent corruption |
| Saga with compensation | Failures you can *see* (explicit errors) |
| Reconciliation | Failures you *cannot see* (timeouts, lost responses) |

The result is not literal exactly-once delivery; it is **exactly-once effect**: no matter how many times something is retried, the money moves once.

---

## 2. High-Level Design (Panel 1)

### Architecture diagram (text form)

```
                     gate                        call + key
 ┌────────┐     ┌────────────┐    ┌───────────────────┐    ┌────────────┐     ┌───────────────────┐
 │ Client │────▶│ API Server │───▶│ Transactional     │◀──▶│ Saga Worker│────▶│ External Provider │
 └────────┘     │ (idem gate)│    │ Store (sharded by │    │ (async SM) │◀ ─ ─│ (card network/bank)│
                └────────────┘    │ account)          │    └────────────┘ or  └─────────┬─────────┘
                                  │  • IdempotencyMap │      claim · txn     timeout    │ daily file
                ┌────────────┐    │  • Payment        │                                 ▼
                │ Invariant  │◀───│  • LedgerEntry    │    ┌────────────────┐   ┌───────────────────┐
                │ Checker    │scan│    (immutable)    │◀──▶│ Reconciliation │◀──│ Settlement Report │
                └────────────┘    └────────┬──────────┘    │ Job            │truth└──────────────────┘
                                           │ alerts        └────────────────┘
                                           ▼                   compare
                                  ┌────────────────────┐
                                  │ Monitoring + Alerts│
                                  └────────────────────┘
```

### Component-by-component

#### Client
- Calls `POST /v1/payments`, `GET` status, and `POST` refund.
- Must send an **idempotency key** with each create request so retries are safe.

#### API Server (Idempotency Gate)
- First line of defence against duplicates.
- Rule: **seen key → return the same result; new key → process.**
- It does **not** call the provider itself. It records intent (a `pending` payment) and returns quickly.

#### Transactional Store (sharded by account)
The authoritative database (ACID). Sharded by **account** so that all entries touching one account live together and per-account writes stay serialized. It holds three logical tables:

1. **IdempotencyMapping** — `UNIQUE(idempotency_key) → payment_id`. The UNIQUE constraint is what makes the gate race-safe: two concurrent requests with the same key cannot both insert.
2. **Payment** — the payment record plus `saga_state`:
   `authorizing → authorized → capturing → captured · compensating · in_doubt`
3. **LedgerEntry (immutable)** — debit + credit per payment. Balance = `SUM(entries)`. **Append-only, never updated.**

Key rule: **capture commits as ONE transaction: debit + credit + status flip.** (The provider cannot join it, but everything on *your* side is atomic.)

#### Saga Worker
- An **asynchronous state machine** that drives each payment: `authorize → capture`.
- Claims pending work from the store (`claim · txn`), calls the provider with the **same idempotency key** (`call + key`), and writes results back.
- Because state is persisted after every step, a crashed worker is replaced by another that **resumes from the persisted state**.

#### External Provider
- Card network or bank. **Not inside your DB transaction.**
- May respond, error out, or **time out** (the dangerous case).
- Produces a **daily settlement file**.

#### Settlement Report
- The provider's **ground truth** of what actually happened, delivered daily.

#### Reconciliation Job
- Compares **ledger vs settlement report**.
- Resolves every `in_doubt` payment, finalizes the ledger, alerts on mismatches.

#### Invariant Checker
- Continuously scans the store and checks: **debits + credits = 0 per payment, per currency.**
- An unbalanced payment means the write path is broken.

#### Monitoring + Alerts
- Watches `in_doubt` count and age, plus ledger mismatches. Receives alerts from the store/checker/reconciler.

---

## 3. How the Design Was Built Up: 4 Fixes

The design is explained as an evolution from a naive solution; each fix addresses a specific failure.

| # | Starting point / fix | What it fixes |
|---|---|---|
| 0 | **Naive:** "charge the card, update a balance" | Breaks on retries, crashes, concurrency, lost responses |
| 1 | **Idempotency gate** | Duplicate requests → duplicate charges |
| 2 | **Async state machine (saga worker)** | Crashes mid-flow; long/slow provider calls blocking the API |
| 3 | **Immutable double-entry ledger** | Lost updates, no history, silent drift in balances |
| 4 | **Saga with compensation** (void/refund) | Visible failures leaving money in a bad state |
| + | **Reconciliation** (backstop) | Invisible failures (timeouts, lost responses) |

The final sentence of the diagram ties it together: **exactly-once effect = safe retries + immutable ledger + compensation + reconciliation.**

---

## 4. Step-by-Step Payment Flow (Panel 2)

### Step 1 — Initiate
- Client calls `POST /v1/payments` with an `idempotency_key`.
- **Key already seen →** return the same `payment_id` (and current/previous result).
- **New key →** in one DB transaction insert the key (UNIQUE) and the payment as `pending`.
- Reply **"pending" at once** — the client does not wait for the provider.

### Step 2 — Hand off to the worker
- The async saga worker picks up the `pending` payment.
- It asks the provider to **authorize** (hold funds), passing the **same idempotency key downstream** so provider-side retries are also safe.

### Step 3 — Capture
- Capture **claims the held funds**.
- **Every step is idempotent and resumable**, so a crashed worker is replaced and continues from where it stopped (state is in the store, not in worker memory).

### Step 4 — Record in the ledger (one transaction)
- In a single DB transaction: write the **debit LedgerEntry**, the **credit LedgerEntry**, and flip status to **captured**. They commit together or not at all.
- **The balance is never stored as a source of truth:** it is the `SUM` of an account's entries.

### Step 5 — Visible failure → compensate
- Explicit error **after authorize** → **void the hold** (cheap, nothing moved yet).
- Failure on a payment that was already **captured** → **refund** (`POST /payments/{id}/refund`, with its own idempotency key). Undoing a capture requires a refund, which is a new, forward transaction, not a deletion.

### Step 6 — Timeout → `in_doubt`
- A **lost response is not a failure**: the money may have moved.
- Mark the payment `in_doubt`, then either:
  - **retry with the same key** (the provider dedups), or
  - **query the provider by your payment reference** if it has no idempotency support.

### Step 7 — Reconcile
- A batch job compares the **ledger** with the provider's **settlement report (truth)**.
- It resolves every `in_doubt` payment, finalizes the ledger, and **alerts on mismatch**.

### State machine

```
            ┌──────────┐
            │ pending  │  (created by API, with idempotency key)
            └────┬─────┘
                 ▼
          ┌─────────────┐   error      ┌───────────────┐
          │ authorizing │─────────────▶│ compensating  │──▶ voided / refunded
          └────┬────────┘              └───────────────┘
               ▼                              ▲
          ┌────────────┐                      │ error after authorize/capture
          │ authorized │──────────────────────┤
          └────┬───────┘                      │
               ▼                              │
          ┌────────────┐   timeout     ┌──────┴──────┐
          │ capturing  │──────────────▶│  in_doubt   │──▶ resolved by retry / query / reconciliation
          └────┬───────┘               └─────────────┘
               ▼
          ┌────────────┐
          │ captured   │  (ledger debit + credit written atomically)
          └────────────┘
```

---

## 5. The Double-Entry Ledger (Panel 3)

### What it is

Every payment writes **two** immutable entries that offset each other:

| payment | account | side | amount |
|---|---|---|---|
| pay_123 | payer | DEBIT | − 50.00 USD |
| pay_123 | payee | CREDIT | + 50.00 USD |

- **Σ per currency = 0** → the payment is **balanced**.
- **balance of an account = SUM of that account's entries.**

### Why not a mutable balance column?

- **Concurrent updates can be lost.** Two writers read 100, both write back their own result; one update vanishes.
- **No history.** A single number tells you nothing about how it got there.
- **Immutable entries fix both:** balance is derived by summation; history *is* the audit trail; nothing is ever overwritten. Corrections are made by appending new (reversing) entries.

### Keeping it correct under concurrency

1. **Overdraft check:** read the *derived* balance **in the same transaction** as the debit, using **SERIALIZABLE isolation or a per-account lock**. Otherwise two concurrent debits can both see enough funds.
2. **Hot accounts:** an account with huge write volume (e.g. a marketplace's main account) becomes a bottleneck. **Split into N sub-ledgers by `hash(payment_id)`;** the account total = sum of the N sub-ledgers. This spreads write contention.
3. **Materialized balance = cache only.** If you keep a running balance for speed, update it **in the same transaction as its entries**; it is a cache, never the truth.
4. **Invariant:** each payment's legs sum to zero per currency, **checked continuously** by the Invariant Checker. An unbalanced payment **proves the write path is broken** — treat it as a critical alert.

### Example of a refund in the ledger (illustrative)

The original entries are never edited. A refund appends new ones:

| payment | account | side | amount |
|---|---|---|---|
| pay_123 | payer | DEBIT | −50.00 USD |
| pay_123 | payee | CREDIT | +50.00 USD |
| refund_123 | payee | DEBIT | −50.00 USD |
| refund_123 | payer | CREDIT | +50.00 USD |

Net effect on both accounts is zero, and the full history remains.

---

## 6. In-Doubt: A Timeout Is Not a Failure (Panel 4)

### The timeline

The diagram shows three lanes over time (t0 … t5):

| Time | Worker | Provider | saga_state |
|---|---|---|---|
| t0 | **capture sent** | | → `capturing` |
| t1 | | **money moves** (charge succeeds at provider) | `capturing` |
| t2 | **timeout ✖** (response lost) | | → `in_doubt (unresolved)` |
| t4 | | **settlement file** produced | `in_doubt` |
| t5 | **ledger written** (after resolution) | | → `captured` |

The crucial moment is **t1–t2**: the provider *did* move the money, but the worker never heard back. If you treated the timeout as a failure and, say, retried naively or voided, you could double charge or lose track of real money.

### Why `in_doubt`, not `failed`

- A timeout means **the response was lost**, not that the charge failed.
- `in_doubt` means **"unresolved"**; the **settlement report decides the real outcome**.

### Retry-safety at two layers

1. **Same idempotency key at your gate and at the provider** (valid within the provider's key retention window). Retrying is then harmless: the provider returns the original result rather than charging again.
2. **If the provider lacks idempotency support:** query it by **your own payment reference** to learn the real state, then reconcile.

### Saga vs reconciliation

| | **Saga** | **Reconciliation** |
|---|---|---|
| Handles | Failures you **SEE** (explicit errors) | Failures you **CAN'T see** (lost responses, silent divergence) |
| Mechanism | Compensating actions (void / refund) | Compare ledger to provider's ground truth |
| Timing | Online, immediate | Batch, daily |

Also note the asymmetry of compensation:
- **Authorize = hold** → voiding it is **cheap**.
- **Capture = claim** → undoing it needs a **refund** (a real, separate money movement).

**Alarm on:** count and age of `in_doubt` payments, and **any ledger vs settlement mismatch.**

---

## 7. Sizing, Data Model and Variants (Panel 5)

### Sizing (page assumptions)

| Metric | Value | Notes |
|---|---|---|
| Peak payments | **10K / s** | Assumed peak load |
| Ledger entries per payment | **2** (debit + credit) | Double-entry |
| Ledger writes | **10K × 2 = 20K / s** | Dominant write load |
| Ledger bandwidth | **~200 B × 20K ≈ 4 MB / s** | Tiny for modern storage |
| Daily reconciliation | **≈ 500M rows**, a routine batch | Easily handled by a standard batch job |

**Takeaway:** the volume is modest. **The design is won on correctness under failure, not raw throughput.** Interview time should go to idempotency, ledger, saga, and reconciliation — not exotic scaling.

### Data model (illustrative)

```sql
-- Idempotency gate: UNIQUE is what makes it race-safe
CREATE TABLE idempotency_mapping (
  idempotency_key  TEXT PRIMARY KEY,      -- UNIQUE
  payment_id       TEXT NOT NULL
);

CREATE TABLE payment (
  payment_id  TEXT PRIMARY KEY,
  payer       TEXT NOT NULL,
  payee       TEXT NOT NULL,
  amount      NUMERIC(18,2) NOT NULL,
  currency    CHAR(3) NOT NULL,
  saga_state  TEXT NOT NULL    -- authorizing | authorized | capturing |
                               -- captured | compensating | in_doubt
);

-- Immutable: INSERT only. No UPDATE / DELETE permitted.
CREATE TABLE ledger_entry (
  entry_id    TEXT PRIMARY KEY,
  payment_id  TEXT NOT NULL REFERENCES payment(payment_id),
  account     TEXT NOT NULL,
  side        TEXT NOT NULL CHECK (side IN ('DEBIT','CREDIT')),
  amount      NUMERIC(18,2) NOT NULL    -- signed or paired with side
);

-- Balance is derived, never stored as truth:
-- SELECT SUM(amount) FROM ledger_entry WHERE account = :acct;
```

The capture transaction:

```sql
BEGIN;
  INSERT INTO ledger_entry (...) VALUES (... 'payer', 'DEBIT',  -50.00);
  INSERT INTO ledger_entry (...) VALUES (... 'payee', 'CREDIT', +50.00);
  UPDATE payment SET saga_state = 'captured' WHERE payment_id = 'pay_123';
COMMIT;
```

### Variants

| Variant | How the design changes |
|---|---|
| **10× scale** | **Shard the ledger by account**, so per-account writes stay serialized and cross-shard coordination is avoided. |
| **Internal wallet transfer** | Both legs in **ONE transaction**. No provider → **no `in_doubt` state and no reconciliation** needed. |
| **Multi-currency** | Add an **FX step** (its own rate + provider call) to the saga; **record both legs in their native currencies** (per-currency balancing still holds). |

### Out of scope
Fraud detection, provider internals, pricing / fees.

---

## 8. Failure Scenario Matrix

| Failure | What happens | Mechanism that protects you |
|---|---|---|
| Client retries the same request | Same key hits the UNIQUE constraint → same `payment_id` returned | **Idempotency gate** |
| Two simultaneous identical requests | One insert wins; the other sees the existing key | **UNIQUE(idempotency_key)** |
| API server crashes after inserting `pending` | Worker still sees the pending payment and processes it | **Durable state + async worker** |
| Worker crashes mid-flow | Another worker claims the payment and resumes from `saga_state` | **Resumable idempotent steps** |
| Provider returns explicit decline/error after authorize | Void the hold | **Saga compensation** |
| Provider error after capture | Refund (own idempotency key) | **Saga compensation** |
| Timeout during capture | Mark `in_doubt`; retry with same key or query provider | **Idempotency + `in_doubt`** |
| Provider charged but we never learned | Settlement file shows the charge; ledger finalized | **Reconciliation** |
| Provider didn't charge but we're unsure | Settlement file shows nothing; payment resolved as failed | **Reconciliation** |
| Concurrent debits on one account | Serializable txn / per-account lock enforces overdraft check | **Concurrency control** |
| Bug writes only one ledger leg | Σ ≠ 0 detected, alert fires | **Invariant Checker** |
| Hot account contention | Split into N sub-ledgers by `hash(payment_id)` | **Sub-ledger sharding** |

---

## 9. Monitoring and Alerting

| Signal | Why it matters |
|---|---|
| **`in_doubt` count** | Rising count → provider trouble or timeout misconfiguration |
| **`in_doubt` age** | Old unresolved payments mean reconciliation is lagging or broken |
| **Ledger vs settlement mismatch** | Direct evidence money and records disagree; highest severity |
| **Invariant violations** (Σ ≠ 0) | The write path is broken; stop and investigate |
| Payments stuck in `authorizing` / `capturing` | Worker health, provider latency |

The diagram routes **alerts** from the store/checker/reconciler into **Monitoring + Alerts** (dashed arrows).

---

## 10. Common Questions and Pitfalls

**Q: Why return "pending" instead of waiting for the result?**
Provider calls are slow and can fail or hang. Making the API return fast and delegating to an async worker keeps the API responsive and makes the flow crash-safe (state lives in the DB).

**Q: Why not use a distributed transaction (2PC) with the provider?**
External providers don't participate in your transactions. So you use idempotency + saga + reconciliation to *emulate* exactly-once effect.

**Q: Isn't retrying dangerous?**
Only without idempotency. With the same key at both your gate and the provider, a retry cannot create a second charge.

**Q: What if the provider has no idempotency key support?**
Query the provider by your payment reference to find out the real state before acting, and let reconciliation be the final arbiter.

**Q: Why is the ledger immutable?**
Auditability, no lost updates, and the ability to reconstruct any balance at any point in time. Mistakes are corrected with new compensating entries, never edits.

**Q: Why shard by account?**
Overdraft checks and balance sums are per account. Co-locating an account's entries keeps those operations single-shard and serializable.

**Q: Is a stored balance ever OK?**
Yes, **as a cache** updated in the **same transaction** as the entries; the ledger remains the source of truth.

**Common pitfalls**
- Treating a timeout as a failure and issuing a second charge.
- Updating a balance column in place.
- Forgetting to pass the idempotency key downstream to the provider.
- Writing the two ledger legs in separate transactions.
- Having no reconciliation, so silent mismatches go undetected.
- Sending refunds without their own idempotency key.

---

## 11. Summary Cheat Sheet

1. **Idempotency gate** (`UNIQUE(idempotency_key) → payment_id`): retries are safe; same key → same result.
2. **Async saga worker** drives `authorize → capture` with a persisted, resumable state machine.
3. **Same idempotency key downstream** to the provider (within its retention window).
4. **Immutable double-entry ledger:** every payment = debit + credit, Σ = 0 per currency; balance = SUM of entries; append-only.
5. **Capture commits atomically:** debit + credit + status flip in ONE DB transaction.
6. **Compensate visible failures:** void a hold (cheap); refund a capture (own key).
7. **Timeout ⇒ `in_doubt`, never `failed`**; resolve by retry with same key, provider query, or settlement report.
8. **Reconciliation** (ledger vs provider settlement file) catches what the saga cannot see.
9. **Invariant checker + alerts** on `in_doubt` count/age and ledger mismatches.
10. **Scale is modest** (10K/s → 20K ledger writes/s ≈ 4 MB/s); the challenge is **correctness under failure**.

> **Guarantee: exactly-once EFFECT = safe retries + immutable ledger + compensation + reconciliation.**

---

*Based on the diagram "Payment System — System Design" (redrawn from the text of systemdesignschool.io/problems/payment-system/solution).*
