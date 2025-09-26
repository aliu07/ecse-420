package solution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophersTest {
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testPhilosophersEat200Times() throws InterruptedException {
        final int numberOfPhilosophers = 5;
        final int targetEatCount = 200;
        final AtomicInteger totalEats = new AtomicInteger(0);
        final AtomicBoolean shouldStop = new AtomicBoolean(false);
        
        TestablePhilosopher[] philosophers = new TestablePhilosopher[numberOfPhilosophers];
        ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

        // Initialize chopsticks
        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new ReentrantLock(true);
        }

        // Initialize philosophers
        for (int i = 0; i < numberOfPhilosophers; i++) {
            ReentrantLock leftChopstick = chopsticks[i];
            ReentrantLock rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
            philosophers[i] = new TestablePhilosopher(i, leftChopstick, rightChopstick, 
                                                    totalEats, targetEatCount, shouldStop);
        }

        // Start all philosophers
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfPhilosophers);
        for (TestablePhilosopher philosopher : philosophers) {
            executorService.execute(philosopher);
        }

        // Wait for completion
        while (!shouldStop.get() && totalEats.get() < targetEatCount) {
            Thread.sleep(100);
        }
        
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(totalEats.get() >= targetEatCount, "Total eat count should be at least " + targetEatCount);
        assertTrue(totalEats.get() <= targetEatCount + numberOfPhilosophers, 
                  "Total eat count should not exceed " + (targetEatCount + numberOfPhilosophers) + 
                  " but was " + totalEats.get());
        
        // Verify no deadlocks occurred (all philosophers should have eaten at least once)
        for (TestablePhilosopher philosopher : philosophers) {
            assertTrue(philosopher.getEatCount() > 0, "Philosopher " + philosopher.getId() + " should have eaten at least once");
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testNoDeadlockOccurs() throws InterruptedException {
        final int numberOfPhilosophers = 5;
        final int testDurationSeconds = 10;
        final AtomicInteger totalEats = new AtomicInteger(0);
        
        TestablePhilosopher[] philosophers = new TestablePhilosopher[numberOfPhilosophers];
        ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new ReentrantLock(true);
        }

        for (int i = 0; i < numberOfPhilosophers; i++) {
            ReentrantLock leftChopstick = chopsticks[i];
            ReentrantLock rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
            philosophers[i] = new TestablePhilosopher(i, leftChopstick, rightChopstick, totalEats);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfPhilosophers);
        for (TestablePhilosopher philosopher : philosophers) {
            executorService.execute(philosopher);
        }

        // Let it run for a while
        Thread.sleep(testDurationSeconds * 1000);
        executorService.shutdownNow();

        // Verify that eating occurred (no deadlock)
        assertTrue(totalEats.get() > 0, "At least some eating should have occurred (no deadlock)");
        
        // All philosophers should have eaten at least once
        for (TestablePhilosopher philosopher : philosophers) {
            assertTrue(philosopher.getEatCount() > 0, "Philosopher " + philosopher.getId() + " should have eaten at least once");
        }
    }

    // Testable version of Philosopher for unit testing
    private static class TestablePhilosopher implements Runnable {
        private final int id;
        private final ReentrantLock leftChopstick;
        private final ReentrantLock rightChopstick;
        private final long timeoutMs = 100;
        private final AtomicInteger totalEats;
        private final int targetEatCount;
        private final AtomicBoolean shouldStop;
        private volatile int eatCount = 0;

        public TestablePhilosopher(int id, ReentrantLock leftChopstick, ReentrantLock rightChopstick, AtomicInteger totalEats) {
            this(id, leftChopstick, rightChopstick, totalEats, -1, new AtomicBoolean(false));
        }

        public TestablePhilosopher(int id, ReentrantLock leftChopstick, ReentrantLock rightChopstick, 
                                 AtomicInteger totalEats, int targetEatCount, AtomicBoolean shouldStop) {
            this.id = id;
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
            this.totalEats = totalEats;
            this.targetEatCount = targetEatCount;
            this.shouldStop = shouldStop;
        }

        @Override
        public void run() {
            try {
                while (!shouldStop.get() && !Thread.currentThread().isInterrupted()) {
                    think();
                    if (pickUpChopsticks()) {
                        eat();
                        putDownChopsticks();
                    } else {
                        Thread.sleep(10); // Brief pause before retrying
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void think() throws InterruptedException {
            Thread.sleep(5); // Reduced sleep time for faster testing
        }

        private void eat() throws InterruptedException {
            eatCount++;
            int currentTotal = totalEats.incrementAndGet();
            Thread.sleep(10); // Reduced sleep time for faster testing
            
            // Check if we've reached the target after eating
            if (targetEatCount > 0 && currentTotal >= targetEatCount) {
                shouldStop.set(true);
            }
        }

        private boolean pickUpChopsticks() throws InterruptedException {
            boolean leftAcquired = false;
            boolean rightAcquired = false;
            try {
                leftAcquired = leftChopstick.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
                if (leftAcquired) {
                    rightAcquired = rightChopstick.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
                    if (rightAcquired) {
                        return true;
                    }
                }
                return false;
            } finally {
                if (!rightAcquired && leftAcquired) {
                    leftChopstick.unlock();
                }
            }
        }

        private void putDownChopsticks() {
            if (leftChopstick.isHeldByCurrentThread()) {
                leftChopstick.unlock();
            }
            if (rightChopstick.isHeldByCurrentThread()) {
                rightChopstick.unlock();
            }
        }

        public int getEatCount() {
            return eatCount;
        }

        public int getId() {
            return id;
        }
    }
}
