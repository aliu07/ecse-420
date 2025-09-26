package solution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
	
	public static void main(String[] args) {

		int numberOfPhilosophers = 5;
		Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
		ReentrantLock[] chopsticks = new ReentrantLock[numberOfPhilosophers];

		for (int i = 0; i < numberOfPhilosophers; i++) {
			// chopsticks[i] = new ReentrantLock(); // without fairness, starvation possible
			chopsticks[i] = new ReentrantLock(true); // adding fairness
		}

		for (int i = 0; i < numberOfPhilosophers; i++) {
			ReentrantLock leftChopstick = chopsticks[i];
			ReentrantLock rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
			philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(numberOfPhilosophers);
		for (Philosopher philosopher : philosophers) {
			executorService.execute(philosopher);
		}
	}

	public static class Philosopher implements Runnable {

		private final int id;
		private final ReentrantLock leftChopstick;
		private final ReentrantLock rightChopstick;
		private final long timeoutMs = 100; // timeout for trying to acquire chopsticks
		private int eatCount = 0;


		public Philosopher(int id, ReentrantLock leftChopstick, ReentrantLock rightChopstick) {
			this.id = id;
			this.leftChopstick = leftChopstick;
			this.rightChopstick = rightChopstick;
		}

		@Override
		public void run() {
			try {
				while (true) {
					think();
					if (pickUpChopsticks()) {
						eat();
						putDownChopsticks();
					} else {
						// failed to get both chopsticks, try again after a brief pause
						System.out.println("Philosopher " + id + " couldn't get both chopsticks, retrying...");
						Thread.sleep(50);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Philosopher " + id + " was interrupted");
			}
		}

		private void think() {
			System.out.println("Philosopher " + id + " is thinking.");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		private void eat() {
			System.out.println("Philosopher " + id + " is eating.");
			eatCount++;
			System.out.println("Philosopher " + id + " has eaten " + eatCount + " times.");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		private boolean pickUpChopsticks() throws InterruptedException {
			boolean leftAcquired = false;
			boolean rightAcquired = false;
			try {
				leftAcquired = leftChopstick.tryLock(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
				if (leftAcquired) {
					System.out.println("Philosopher " + id + " picked up left chopstick.");
					rightAcquired = rightChopstick.tryLock(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
					if (rightAcquired) {
						System.out.println("Philosopher " + id + " picked up right chopstick.");
						return true;
					} else {
						System.out.println("Philosopher " + id + " failed to pick up right chopstick.");
						return false;
					}
				} else {
					// Failed to acquire left chopstick
					return false;
				}
			} finally {
				if (!rightAcquired && leftAcquired) {
					leftChopstick.unlock();
					System.out.println("Philosopher " + id + " put down left chopstick.");
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
			System.out.println("Philosopher " + id + " put down chopsticks.");
		}
	}

}
	