package DS.multithreading.producerConsumerProblem;

import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private final BlockingQueue blockingQueue;

    public Consumer(BlockingQueue blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        for (int i = 0; i <= 10; i++) {
            try {
                System.out.println("Consumed : " + blockingQueue.take());
            } catch (Exception e) {

            }
        }
    }
}
