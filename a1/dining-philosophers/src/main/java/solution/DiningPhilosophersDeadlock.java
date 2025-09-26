package solution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophersDeadlock {
	
	public static void main(String[] args) {

		int numberOfPhilosophers = 5;
		Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
		Object[] chopsticks = new Object[numberOfPhilosophers];

		for (int i = 0; i < numberOfPhilosophers; i++) {
			chopsticks[i] = new Object();
		}

		for (int i = 0; i < numberOfPhilosophers; i++) {
			Object leftChopstick = chopsticks[i];
			Object rightChopstick = chopsticks[(i + 1) % numberOfPhilosophers];
			philosophers[i] = new Philosopher(i, leftChopstick, rightChopstick);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(numberOfPhilosophers);
		for (Philosopher philosopher : philosophers) {
			executorService.execute(philosopher);
		}
	}

	public static class Philosopher implements Runnable {

		private final int id;
		private final Object leftChopstick;
		private final Object rightChopstick;
		
		public Philosopher(int id, Object leftChopstick, Object rightChopstick) {
			this.id = id;
			this.leftChopstick = leftChopstick;
			this.rightChopstick = rightChopstick;
		}

		@Override
		public void run() {
			try {
				while (true) {
					think();
					pickUpChopsticks();
					eat();
					putDownChopsticks();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Philosopher " + id + " was interrupted");
			}	
		}
		private void think() throws InterruptedException {
			System.out.println("Philosopher " + id + " is thinking");
			Thread.sleep(50);
		}
		private void eat() throws InterruptedException {
			System.out.println("Philosopher " + id + " is eating");
			Thread.sleep(1000);
		}
		private void pickUpChopsticks() {
			// all philosophers try to pick up left chopstick first, which would eventually create deadlock
			System.out.println("Philosopher " + id + " is trying to pick up left chopstick");
			synchronized (leftChopstick) {
				System.out.println("Philosopher " + id + " picked up left chopstick");
				
				System.out.println("Philosopher " + id + " is trying to pick up right chopstick");
				synchronized (rightChopstick) {
					System.out.println("Philosopher " + id + " picked up right chopstick");
				}
			}
		}
		private void putDownChopsticks() {
			System.out.println("Philosopher " + id + " put down both chopsticks");
		}
	}
}
