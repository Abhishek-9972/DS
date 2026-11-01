package DS.DesignPattern.behavioral.observer;


import java.util.ArrayList;
import java.util.List;

// Observer
interface Subscriber {
    void notify(String video);
}

// Concrete Observer
class User implements Subscriber {

    private String name;

    public User(String name) {
        this.name = name;
    }

    public void notify(String video) {
        System.out.println(name + " received notification: " + video);
    }
}

// Subject
class YouTubeChannel {

    private List<Subscriber> subscribers = new ArrayList<>();

    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void uploadVideo(String video) {
        System.out.println("New video uploaded: " + video);

        for (Subscriber subscriber : subscribers) {
            subscriber.notify(video);
        }
    }
}

// Client
public class ObserverPattern {

    public static void main(String[] args) {

        YouTubeChannel channel = new YouTubeChannel();

        Subscriber user1 = new User("Alice");
        Subscriber user2 = new User("Bob");

        channel.subscribe(user1);
        channel.subscribe(user2);

        channel.uploadVideo("Java Design Patterns");

        channel.unsubscribe(user1);

        channel.uploadVideo("System Design");
    }
}

