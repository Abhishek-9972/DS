# Stock Broker Trading Platform: High-Level Flow in Detail

Step numbers match the numbered arrows in the HLD diagram (`stock_broker_trading_HLD_2560x1440.png`).

---

## Place-Order Flow

### 1. Client → Edge
- The user taps "Buy 10 RELIANCE @ ₹2,900 Limit" in the app. The client generates an **idempotency key** (a UUID) and sends `POST /orders` over HTTPS. Algo clients use REST or FIX instead.
- The request goes through the **CDN/WAF**, which filters DDoS and bot traffic, and then the **API Gateway**.
- The gateway terminates TLS and validates the **JWT** (session) against the Auth service, with a step-up MFA/TOTP check for sensitive actions. It applies **per-user and per-IP rate limits** and checks the idempotency key in Redis. If the key was already seen, it returns the original response, so a retry never creates a second order.
- It then routes the request to the OMS.

### 2. OMS: Validate and Persist
- **Static validation:** the symbol exists and is tradable, the quantity is a multiple of the lot size, the price is within the tick size and the exchange circuit limits (price bands), and the market is open (or the order is AMO/pre-open).
- **State machine:** the OMS creates the order with a unique `orderId` in state `NEW`. The order lifecycle is:
  `NEW → VALIDATED → RISK_APPROVED → SENT → OPEN → PARTIALLY_FILLED → FILLED`,
  with `REJECTED`, `CANCELLED` and `EXPIRED` as terminal states.
- **Durable write before any acknowledgement:** it writes the order row and an outbox/event row in **one transaction** to the Order DB (sharded by `userId`). No order is acknowledged until it is durable.

### 3. Pre-Trade Risk and Margin
- The Risk engine does the checks on the hot path. Where possible it reads from **Redis** (cached limits, margin parameters, current exposure) instead of the database.
  - **Funds or margin:** for delivery buys, `price × qty + charges` must be available. For F&O or intraday, it computes SPAN plus exposure margin.
  - **Limits:** position limits, per-order value limits, a concentration check, and a check that the user is not blocked or in square-off mode.
- **Margin blocking:** the Ledger service reserves the amount with a double-entry hold (available → blocked). This must be atomic and idempotent, keyed by `orderId`.
- On failure the order moves to `REJECTED` with a reason code, and the client gets the error immediately. On success it moves to `RISK_APPROVED`.

### 4. Publish to Kafka
- The `OrderAccepted` event is published to `orders.new`, **partitioned by symbol**, so all orders for a symbol are processed in sequence.
- It goes out via the **transactional outbox**. A relay or CDC process reads the outbox row from step 2 and publishes it, so there is never a state where the order is saved but the event is lost, or the reverse.
- The client gets an **HTTP 202 with the `orderId`** at this point. Execution is asynchronous, and the client then watches for status updates over WebSocket.

### 5. Route to the Exchange
- The **Exchange Gateway / Smart Order Router** consumes the event. Its jobs are:
  - choosing the venue (NSE or BSE) based on liquidity, price and cost;
  - **throttling** to the exchange's orders-per-second limit and queueing the excess by priority;
  - translating the order into the exchange's wire format, typically a FIX `NewOrderSingle` or an exchange-native binary protocol.
- It holds **long-lived FIX sessions** with sequence numbers, heartbeats and resend/gap-fill logic. The gateway runs **active-passive**: if the active node dies, the standby takes over and recovers from the last confirmed sequence number.
- The exchange acknowledges the order, the state becomes `SENT`, then `OPEN`, and the ack goes back through Kafka to the OMS.

### 6. Match and Execute
- The exchange's **matching engine** applies price-time priority. A limit buy rests in the order book until it crosses with a sell.
- Each match produces an **ExecutionReport**: a partial fill (for example 4 of 10 shares) or a full fill, with the fill price, fill quantity and exchange trade ID. The exchange also publishes **rejects** (for example RMS or price-band rejections) and **cancel confirmations** the same way.
- The gateway receives these over FIX, deduplicates them by exec ID, and publishes them to Kafka (`executions` / `fills`).

### 7. Fan-Out of Fills
Several consumers read the fill event independently, each in its own consumer group:
- **OMS** updates the order state (`PARTIALLY_FILLED` or `FILLED`) and the cumulative filled quantity.
- **Portfolio** updates positions, average price and realized/unrealized P&L. This is an eventually consistent view.
- **Ledger** converts the margin block into an actual debit (and releases any difference if the fill price was better than the limit).
- **Notification** sends push, SMS and email ("Order executed").
- **Audit/Data Lake** receives a copy of every event for compliance and analytics.

Every consumer must be **idempotent** (keyed by exec ID), because Kafka delivery is at-least-once.

### 8. Push to the Client and Settle
- The **WebSocket Gateway** pushes order status and position updates to the user's open sessions. Users who are offline get a push notification and see the latest state when the app reconnects.
- **End of day:** reconcile every order and trade against the exchange's trade files. Any mismatch becomes an exception queue item for operations.
- **Clearing and settlement** run on a T+1 cycle through the clearing corporation. Fund and securities movements are posted, and **contract notes** and reports are generated from the data lake.

---

## Market-Data Flow (Runs in Parallel)

1. The exchange publishes tick-by-tick data (market depth, trades) over a multicast or TCP feed.
2. The **Feed Handler** decodes the binary feed, normalizes it to an internal schema and handles sequence gaps.
3. It publishes to the market-data topics in Kafka.
4. The **Market Data Service** keeps the latest snapshot per symbol and writes **LTP and depth to Redis**. It also **conflates** updates: if a client is slow, it sends only the latest price and drops stale ones. Ticks are persisted to the **time-series store** for charts and OHLC candles.
5. The **WebSocket Gateway** keeps client subscriptions (per-symbol topics). On subscribe it sends a snapshot, then deltas. It applies backpressure and bandwidth limits per connection.

---

## Failure Scenarios to Mention in the Interview

| Scenario | Handling |
|---|---|
| Client retries after a timeout | The idempotency key returns the same `orderId`, so no duplicate order. |
| OMS crashes after the DB write but before publishing | The outbox relay still publishes the event after restart. |
| Gateway sends an order but the ack is lost | On reconnect, FIX resend and **order status request** reconcile the state. The order is never blindly resent. |
| Duplicate or out-of-order fill events | Dedupe by exec ID, and apply fills in exchange sequence order. |
| Exchange or feed outage | Circuit breaker stops routing, orders are held or rejected with a clear reason, and clients are told. |
| Runaway algo or bad deployment | Kill switch, per-user order-rate caps, and a pre-trade "fat finger" value check. |
| Market-open spike | Pre-scale services before 9:00, use backpressure, and prioritize cancels and square-offs over new orders. |
