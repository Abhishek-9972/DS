### Payment Flow – High Level

1\. Customer initiates payment

- Customer calls `PaymentService.pay(orderId, amount, paymentType)`.

2\. Create Payment

- `PaymentService` creates a `Payment` object with status `INITIATED`.

3\. Fraud Detection

- `PaymentService` calls `FraudDetector` to validate the payment.
- If fraudulent, reject the payment.

4\. Select Payment Method

- Based on `PaymentType`, select `CardPayment`, `UpiPayment`, `WalletPayment`, etc.

5\. Process Payment

- `PaymentService` calls `PaymentGateway.charge(payment)`.

6\. Create Transaction

- Concrete gateway (`RazorpayGateway` / `StripeGateway`) communicates with the external payment provider.
- Gateway returns a `Transaction` object with transaction ID and status.

7\. Update Payment Status

- `PaymentService` updates payment status to `SUCCESS` or `FAILED` based on the transaction result.

8\. Notify Customer

- `PaymentService` notifies observers (`EmailNotifier`, `SmsNotifier`) about payment status.

9\. Refund (if requested)

- Customer requests refund.
- `PaymentService` calls `PaymentGateway.refund()`.
- Gateway returns refund result.
- Refund and payment statuses are updated.

10\. Check Payment Status

- Customer calls `PaymentService.getStatus(paymentId)` to retrieve the latest payment status.