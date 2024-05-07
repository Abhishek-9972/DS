package ds.multithreading.printEvenOdd;

public class Main {
    public static void main(String[] args) {
        Printer printer = new Printer();
        Thread t1 = new Thread(new TakeEvenOdd(printer, 10, false), "Odd");
        Thread t2 = new Thread(new TakeEvenOdd(printer, 10, true), "Even");
        t1.start();
        t2.start();
    }
}
