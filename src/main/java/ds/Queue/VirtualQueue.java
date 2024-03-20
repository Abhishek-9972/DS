package ds.Queue;

import java.util.Stack;

public class VirtualQueue {

    private Stack<Integer> stackWithOldOnTop = new Stack<>();
    private Stack<Integer> stackWithNewOnTop = new Stack<>();

    private void enqueue(int item) {
        stackWithNewOnTop.push(item);
    }

    private int dequeue() {
        shiftStacks();
        return stackWithOldOnTop.pop();
    }

    private void shiftStacks() {
        if (stackWithOldOnTop.isEmpty()) {
            while (!stackWithNewOnTop.isEmpty()) {
                stackWithOldOnTop.push(stackWithNewOnTop.pop());
            }
        }
    }

    public static void main(String[] args) {
        VirtualQueue virtualQueue = new VirtualQueue();
        virtualQueue.enqueue(1);
        virtualQueue.enqueue(2);
        virtualQueue.enqueue(3);
        System.out.println(virtualQueue.dequeue());
    }
}
