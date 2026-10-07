package DS.multithreading.Abc3Times;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        ABCPrinter printer = new ABCPrinter();

        Thread threadA = new Thread(() -> {
            try {
                printer.printA(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread threadB = new Thread(() -> {
            try {
                printer.printB(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread threadC = new Thread(() -> {
            try {
                printer.printC(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        threadA.start();
        threadB.start();
        threadC.start();

        threadA.join();
        threadB.join();
        threadC.join();
    }
}
