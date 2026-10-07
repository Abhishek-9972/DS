package DS.multithreading.printEvenOdd;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        OddEvenPrinter printer = new OddEvenPrinter(10);

        Thread oddThread = new Thread(
                () -> {
                    try {
                        printer.printOdd();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                "Odd"
        );

        Thread evenThread = new Thread(
                () -> {
                    try {
                        printer.printEven();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                "Even"
        );

        oddThread.start();
        evenThread.start();

        oddThread.join();
        evenThread.join();
    }
}