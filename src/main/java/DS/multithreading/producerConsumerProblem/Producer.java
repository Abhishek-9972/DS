package DS.multithreading.producerConsumerProblem;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {
    private final BlockingQueue blockingQueue;

    public Producer(BlockingQueue blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        for (int i = 0; i <= 10; i++) {
            try {
                System.out.println("Produced : " + i);
                blockingQueue.put(i);
            } catch (Exception e) {

            }
        }
    }
}
