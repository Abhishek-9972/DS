package DS.multithreading.printEvenOdd;

class OddEvenPrinter {

    private int number = 1;
    private final int limit;

    OddEvenPrinter(int limit) {
        this.limit = limit;
    }

    public synchronized void printOdd() throws InterruptedException {

        while (number <= limit) {

            // If number is even, wait
            while (number % 2 == 0 && number <= limit) {
                wait();
            }

            if (number <= limit) {
                System.out.println(Thread.currentThread().getName() + " : " + number);
                number++;

                notifyAll();
            }
        }
    }

    public synchronized void printEven() throws InterruptedException {

        while (number <= limit) {

            // If number is odd, wait
            while (number % 2 != 0 && number <= limit) {
                wait();
            }

            if (number <= limit) {
                System.out.println(Thread.currentThread().getName() + " : " + number);
                number++;

                notifyAll();
            }
        }
    }
}