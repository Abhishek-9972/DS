package DS.DesignPattern.behavioral.observer.Bkp;

public class MessageSubscriberThree implements Observer {
    @Override
    public void update(Message m) {
        System.out.println("MessageSubscriberThree :: " + m.getMessageContent());
    }
}