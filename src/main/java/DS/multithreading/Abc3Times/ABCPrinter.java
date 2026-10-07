package DS.multithreading.Abc3Times;

class ABCPrinter {

    private int turn = 0;

    public synchronized void printA(int times) throws InterruptedException {
        for (int i = 0; i < times; i++) {

            while (turn != 0) {
                wait();
            }

            System.out.print("A");

            turn = 1;
            notifyAll();
        }
    }

    public synchronized void printB(int times) throws InterruptedException {
        for (int i = 0; i < times; i++) {

            while (turn != 1) {
                wait();
            }

            System.out.print("B");

            turn = 2;
            notifyAll();
        }
    }

    public synchronized void printC(int times) throws InterruptedException {
        for (int i = 0; i < times; i++) {

            while (turn != 2) {
                wait();
            }

            System.out.print("C");

            turn = 0;
            notifyAll();
        }
    }
}
