package DS.DesignPattern.structural.facade;


// Subsystem 1
class PaymentService {
    public void makePayment() {
        System.out.println("Payment completed");
    }
}

// Subsystem 2
class InventoryService {
    public void updateInventory() {
        System.out.println("Inventory updated");
    }
}

// Subsystem 3
class NotificationService {
    public void sendNotification() {
        System.out.println("Notification sent");
    }
}

// Facade
class OrderFacade {

    private PaymentService paymentService;
    private InventoryService inventoryService;
    private NotificationService notificationService;

    public OrderFacade() {
        paymentService = new PaymentService();
        inventoryService = new InventoryService();
        notificationService = new NotificationService();
    }

    public void placeOrder() {
        paymentService.makePayment();
        inventoryService.updateInventory();
        notificationService.sendNotification();
    }
}

// Client
public class Facade {

    public static void main(String[] args) {

        OrderFacade orderFacade = new OrderFacade();
        orderFacade.placeOrder();
    }
}
