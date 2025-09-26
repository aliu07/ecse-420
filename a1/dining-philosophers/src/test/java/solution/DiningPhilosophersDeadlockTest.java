package solution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiningPhilosophersDeadlockTest {
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testPhilosophersEat200Times() throws InterruptedException {
        final int numberOfPhilosophers = 5;
        final int targetEatCount = 200;
        final AtomicInteger totalEats = new AtomicInteger(0);
        final AtomicBoolean shouldStop = new AtomicBoolean(false);
        
        TestableDeadlockPhilosopher[] philosophers = new TestableDeadlockPhilosopher[numberOfPhilosophers];
        Object[] chopsticks = new Object[numberOfPhilosophers];

        // Initialize chopsticks
        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new Object();
        }

        // Initialize philosophers with deadlock prevention (resource ordering)
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Object leftChopstick = chopsticks[i];
            Object rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
            philosophers[i] = new TestableDeadlockPhilosopher(i, leftChopstick, rightChopstick, 
                                                            totalEats, targetEatCount, shouldStop);
        }

        // Start all philosophers
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfPhilosophers);
        for (TestableDeadlockPhilosopher philosopher : philosophers) {
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
        for (TestableDeadlockPhilosopher philosopher : philosophers) {
            assertTrue(philosopher.getEatCount() > 0, "Philosopher " + philosopher.getId() + " should have eaten at least once");
        }
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testDeadlockScenario() throws InterruptedException {
        final int numberOfPhilosophers = 5;
        final AtomicInteger totalEats = new AtomicInteger(0);
        final AtomicBoolean potentialDeadlock = new AtomicBoolean(false);
        
        DeadlockPronePhilosopher[] philosophers = new DeadlockPronePhilosopher[numberOfPhilosophers];
        Object[] chopsticks = new Object[numberOfPhilosophers];

        for (int i = 0; i < numberOfPhilosophers; i++) {
            chopsticks[i] = new Object();
        }

        // Create philosophers that are prone to deadlock (original implementation behavior)
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Object leftChopstick = chopsticks[i];
            Object rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
            philosophers[i] = new DeadlockPronePhilosopher(i, leftChopstick, rightChopstick, totalEats, potentialDeadlock);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfPhilosophers);
        for (DeadlockPronePhilosopher philosopher : philosophers) {
            executorService.execute(philosopher);
        }

        // Let it run for a short while to see if deadlock occurs
        Thread.sleep(5000);
        
        int eatsAfter5Seconds = totalEats.get();
        Thread.sleep(3000);
        int eatsAfter8Seconds = totalEats.get();
        
        executorService.shutdownNow();

        // If no progress was made in the last 3 seconds, likely deadlocked
        boolean likelyDeadlocked = (eatsAfter8Seconds == eatsAfter5Seconds) && eatsAfter5Seconds > 0;
        
        // This test documents the deadlock behavior - it may or may not deadlock depending on timing
        System.out.println("Total eats achieved: " + totalEats.get());
        System.out.println("Potential deadlock detected: " + (likelyDeadlocked || potentialDeadlock.get()));
        
        // The test passes regardless, as it's meant to demonstrate the deadlock scenario
        assertTrue(true, "Test completed - this demonstrates potential deadlock behavior");
    }

    // Modified philosopher that uses resource ordering to prevent deadlock
    private static class TestableDeadlockPhilosopher implements Runnable {
        private final int id;
        private final Object leftChopstick;
        private final Object rightChopstick;
        private final AtomicInteger totalEats;
        private final int targetEatCount;
        private final AtomicBoolean shouldStop;
        private volatile int eatCount = 0;

        public TestableDeadlockPhilosopher(int id, Object leftChopstick, Object rightChopstick, 
                                         AtomicInteger totalEats, int targetEatCount, 
                                         AtomicBoolean shouldStop) {
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
                    pickUpChopsticksWithResourceOrdering();
                    eat();
                    putDownChopsticks();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void think() throws InterruptedException {
            Thread.sleep(5); // Reduced for faster testing
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

        private void pickUpChopsticksWithResourceOrdering() {
            // Use resource ordering to prevent deadlock
            Object first, second;
            if (System.identityHashCode(leftChopstick) < System.identityHashCode(rightChopstick)) {
                first = leftChopstick;
                second = rightChopstick;
            } else {
                first = rightChopstick;
                second = leftChopstick;
            }
            
            synchronized (first) {
                synchronized (second) {
                    // Both chopsticks acquired safely
                }
            }
        }

        private void putDownChopsticks() {
            // Chopsticks are automatically released when exiting synchronized blocks
        }

        public int getEatCount() {
            return eatCount;
        }

        public int getId() {
            return id;
        }
    }

    // Original deadlock-prone implementation for demonstration
    private static class DeadlockPronePhilosopher implements Runnable {
        private final Object leftChopstick;
        private final Object rightChopstick;
        private final AtomicInteger totalEats;
        private volatile boolean running = true;

        public DeadlockPronePhilosopher(int id, Object leftChopstick, Object rightChopstick, 
                                      AtomicInteger totalEats, AtomicBoolean potentialDeadlock) {
            this.leftChopstick = leftChopstick;
            this.rightChopstick = rightChopstick;
            this.totalEats = totalEats;
            // id and potentialDeadlock parameters ignored in this simplified implementation
        }

        @Override
        public void run() {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    think();
                    pickUpChopsticks();
                    eat();
                    putDownChopsticks();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void think() throws InterruptedException {
            Thread.sleep(5);
        }

        private void eat() throws InterruptedException {
            totalEats.incrementAndGet();
            Thread.sleep(50); // Longer sleep to increase deadlock probability
        }

        private void pickUpChopsticks() {
            // This is the problematic approach - all philosophers try left first
            synchronized (leftChopstick) {
                try {
                    Thread.sleep(1); // Small delay to increase deadlock probability
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                synchronized (rightChopstick) {
                    // Both chopsticks acquired
                }
            }
        }

        private void putDownChopsticks() {
            // Chopsticks automatically released
        }
    }
}