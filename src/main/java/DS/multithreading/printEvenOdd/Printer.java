package DS.multithreading.printEvenOdd;

/**
 * Both threads share the same Printer object. Only one thread can enter a synchronized method at a time.
 * The odd thread waits whenever it's not its turn, and the even thread waits whenever it's not its turn.
 * After printing, the thread flips the boolean flag and calls notifyAll() to wake the other waiting thread.
 * The while loop ensures correctness even if a thread wakes up unexpectedly (spurious wakeup).
 */
class Printer {

    private boolean isOdd = false;

    public synchronized void printOdd(int number) {

        while (isOdd) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println(number);

        isOdd = true;
        notifyAll();
    }

    public synchronized void printEven(int number) {

        while (!isOdd) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println(number);

        isOdd = false;
        notifyAll();
    }
}