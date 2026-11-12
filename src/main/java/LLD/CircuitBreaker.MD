### Circuit Breaker Flow – High Level

1\. Client initiates request

- `ServiceClient` receives a request to call an external/remote service.

2\. Get Circuit Breaker

- `ServiceClient` obtains the `CircuitBreaker` instance from `CircuitBreakerRegistry`.
- Registry creates or returns an existing breaker for the configured service.

3\. Execute Request

- `ServiceClient` calls `CircuitBreaker.execute()` with the remote service call.

4\. Check Circuit State

- Circuit breaker checks its current state:
    - CLOSED: Allow the request to proceed.
    - OPEN: Reject the request immediately (Fail Fast).
    - HALF_OPEN: Allow only a limited number of probe requests.

5\. Execute Remote Call

- If allowed, the actual remote service call is executed.

6\. Record Result

- `CircuitBreaker` records success or failure in `SlidingWindow`.
- Sliding window maintains recent request results and calculates the failure rate.

7\. State Transition

- Based on configured thresholds:
    - CLOSED → OPEN: Failure rate reaches the configured threshold.
    - OPEN → HALF_OPEN: Open timeout expires.
    - HALF_OPEN → CLOSED: Probe requests succeed up to the success threshold.
    - HALF_OPEN → OPEN: Any probe request fails.

8\. Fallback Handling

- When the circuit is OPEN or a request fails, `FallbackHandler` is invoked.
- It can return cached data (`CachedFallback`) or a default response (`DefaultFallback`).

9\. Notify Listeners

- Whenever the circuit state changes, `StateChangeListener` implementations are notified.
- Examples: `LogListener` and `AlertListener`.

10\. Return Response

- Return the actual remote service response if successful, otherwise return the fallback response or throw `CircuitOpenException`.

Key design patterns:

| Pattern   | Component                             | Purpose                           |
| --------- | ------------------------------------- | --------------------------------- |
| State     | ClosedState, OpenState, HalfOpenState | Manage circuit states             |
| Singleton | CircuitBreakerRegistry                | Maintain shared breaker instances |
| Strategy  | FallbackHandler                       | Different fallback strategies     |
| Observer  | StateChangeListener                   | Notify state changes              |

Interview takeaway: The `CircuitBreaker` protects downstream services by monitoring failures and controlling whether requests are allowed, rejected, or selectively tested through the three states: CLOSED, OPEN, and HALF_OPEN.