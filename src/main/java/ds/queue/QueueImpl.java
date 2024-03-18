package ds.queue;

/**
 * QUEUE is First In First Out (FIFO)
 */
public class QueueImpl {
    private int capacity;
    private int currentSize;
    private int[] queueArr;
    private int front = 0;
    private int rear = -1;

    private QueueImpl(int queueSize) {
        this.capacity = queueSize;
        this.queueArr = new int[queueSize];
    }

    private void enqueue(int item) throws Exception {
        if (isQueueFull()) {
            throw new Exception("Queue is already full");
        } else {
            rear++;
            if (rear == capacity - 1) {
                rear = 0;
            }
            queueArr[rear] = item;
            currentSize++;
        }

    }


    private int dequeue() throws Exception {
        if (isQueueEmpty()) {
            throw new Exception("Queue is Empty");
        } else {
            front++;
            if (front == capacity - 1) {
                front = 0;
            }
            return queueArr[front - 1];
        }
    }

    private boolean isQueueEmpty() {
        return currentSize == 0;
    }

    private boolean isQueueFull() {
        return currentSize == capacity;
    }

    public static void main(String[] args) throws Exception {
        QueueImpl queue = new QueueImpl(10);
        queue.enqueue(5);
        queue.enqueue(4);
        queue.enqueue(3);
        queue.enqueue(1);
        queue.enqueue(5);
        queue.enqueue(13);
        System.out.println(queue.dequeue());

    }
}
