package examples;

public class Deadlock {

    private static class DeadlockExampleThread1 extends Thread {

        public void run() {
            synchronized (lock1) {
                System.out.println("Thread 1 holding lock 1...");

                try {
                    Thread.sleep(1000); // sleep to increase chances of deadlock
                } catch (InterruptedException e) {
                    // ignore exception
                }

                System.out.println("Thread 1 waiting for lock 2...");

                synchronized (lock2) {
                    System.out.println("Thread 1 holding locks 1 and 2...");
                }
            }
        }
    }

    private static class DeadlockExampleThread2 extends Thread {

        public void run() {
            synchronized (lock2) {
                System.out.println("Thread 2 holding lock 2...");

                try {
                    Thread.sleep(1000); // sleep to increase chances of deadlock
                } catch (InterruptedException e) {
                    // ignore exception
                }

                System.out.println("Thread 2 waiting for lock 1...");

                synchronized (lock1) {
                    System.out.println("Thread 2 holding locks 1 and 2...");
                }
            }
        }
    }

    public static Object lock1 = new Object();
    public static Object lock2 = new Object();

    public static void main(String[] args) {
        DeadlockExampleThread1 t1 = new DeadlockExampleThread1();
        DeadlockExampleThread2 t2 = new DeadlockExampleThread2();

        t1.start();
        t2.start();
    }
}
