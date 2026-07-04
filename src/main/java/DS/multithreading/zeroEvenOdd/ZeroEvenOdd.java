package DS.multithreading.zeroEvenOdd;

import java.util.function.IntConsumer;

/**
 * https://leetcode.com/problems/print-zero-even-odd/
 */
class ZeroEvenOdd {

    private int n;

    private int current = 1;

    // 0 = zero's turn
    // 1 = odd's turn
    // 2 = even's turn
    private int turn = 0;

    public ZeroEvenOdd(int n) {
        this.n = n;
    }

    public synchronized void zero(IntConsumer printNumber)
            throws InterruptedException {

        while (current <= n) {

            while (turn != 0) {
                wait();
            }

            printNumber.accept(0);

            if (current % 2 == 1) {
                turn = 1;
            } else {
                turn = 2;
            }

            notifyAll();
        }
    }

    public synchronized void even(IntConsumer printNumber)
            throws InterruptedException {

        while (current <= n) {

            while (turn != 2) {
                wait();
            }

            printNumber.accept(current);

            current++;

            turn = 0;

            notifyAll();
        }
    }

    public synchronized void odd(IntConsumer printNumber)
            throws InterruptedException {

        while (current <= n) {

            while (turn != 1) {
                wait();
            }

            printNumber.accept(current);

            current++;

            turn = 0;

            notifyAll();
        }
    }
}