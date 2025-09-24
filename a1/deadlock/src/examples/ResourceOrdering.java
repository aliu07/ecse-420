package examples;

public class ResourceOrdering {

    private static class ResourceOrderingExampleThread extends Thread {

        private Integer threadNum;

        public ResourceOrderingExampleThread(Integer threadNum) {
            this.threadNum = threadNum;
        }

        public void run() {
            synchronized (lock1) {
                System.out.println(
                        String.format("Thread %d holding lock 1...", threadNum));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore exception
                }

                System.out.println(
                        String.format("Thread %d waiting for lock 2...", threadNum));

                synchronized (lock2) {
                    System.out.println(
                            String.format(
                                    "Thread %d holding locks 1 and 2...",
                                    threadNum));
                }
            }
        }
    }

    public static Object lock1 = new Object();
    public static Object lock2 = new Object();

    public static void main(String[] args) {
        ResourceOrderingExampleThread t1 = new ResourceOrderingExampleThread(1);
        ResourceOrderingExampleThread t2 = new ResourceOrderingExampleThread(2);

        t1.start();
        t2.start();
    }
}
