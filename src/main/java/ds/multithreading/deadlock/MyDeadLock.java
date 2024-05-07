package ds.multithreading.deadlock;

public class MyDeadLock {
    String str1 = "Java";
    String str2 = "UNIX";

    Thread trd1 = new Thread("My Thread 1") {
        public void run() {
            while (true) {
                synchronized (str1) {
                    synchronized (str2) {
                        System.out.println(str1 + str2);
                    }
                }
            }
        }
    };

    Thread trd2 = new Thread("My Thread 2") {
        public void run() {
            while (true) {
                synchronized (str2) {
                    synchronized (str1) {
                        System.out.println(str1 + str2);
                    }
                }
            }

        }
    };

    public static void main(String[] args) {
        MyDeadLock myDeadLock = new MyDeadLock();
        myDeadLock.trd1.start();
        myDeadLock.trd2.start();
    }

}
