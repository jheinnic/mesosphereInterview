package info.jchein.mesosphere.domain.clock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscription;
import rx.functions.Action0;

public class SystemClock implements IClock {
	private static final Logger LOG = LoggerFactory.getLogger(SystemClock.class);

	private final long tickMillis;
	private final Worker worker;
	private final FibonacciHeap<RegisteredInterrupt> interruptHeap;
	private final ArrayList<FibonacciHeapNode<RegisteredInterrupt>> allInterrupts;

	private boolean acceptingInterrupts;
	private Subscription interruptsSubscription;

	class RegisteredInterrupt implements Comparable<RegisteredInterrupt> {
		final IInterruptHandler handler;
		final int priority;
		final long targetInterval;

		// Each iteration we mutate this interval field to compensate for clock skew
		// based on whether the clock tick where the interrupt actually fires happens
		// earlier or later than its ideal target time that would have been a perfect
		// multiple of the tick interval.
		long nextExpectedInterval;
		long previousActualInterval;

		RegisteredInterrupt(IInterruptHandler handler, long interval, int priority) {
			this.handler = handler;
			this.priority = priority;
			this.targetInterval = interval;
			this.nextExpectedInterval = interval;
			this.previousActualInterval = 0;
		}

		public int compareTo(RegisteredInterrupt o) {
			return this.priority - o.priority;
		}
	}

	class ProcessRecurringInterrupts implements Action0 {
		private final long timeZero;
		private final long timeNext;
		private final long deltaNext;

		ProcessRecurringInterrupts() {
			this.timeZero = SystemClock.this.worker.now();
			this.timeNext = this.timeZero + SystemClock.this.tickMillis;
			this.deltaNext = SystemClock.this.tickMillis;
		}

		ProcessRecurringInterrupts(long timeZero, long timeNext, long deltaNext) {
			this.timeZero = timeZero;
			this.timeNext = timeNext;
			this.deltaNext = deltaNext;
		}

		@Override
		public void call() {
			final long timeNow = SystemClock.this.worker.now();
			final long deltaNow = timeNow - this.timeZero;
			final long nextTimeNext = this.timeNext + SystemClock.this.tickMillis;
			final long deltaNext = nextTimeNext - timeNow;
			final long iterationError = deltaNext - SystemClock.this.tickMillis;

			// TODO: Proofcheck these alternate paths yield the same calculated values?
			// final long nextTimeNext = this.timeNext + SystemClock.this.tickMillis;
			// final long iterationError = this.timeNext - timeNow;
			// final long deltaNext = iterationError + SystemClock.this.tickMillis;

			for (final FibonacciHeapNode<RegisteredInterrupt> nextNode : SystemClock.this.allInterrupts) {
				SystemClock.this.interruptHeap.decreaseKey(nextNode, nextNode.getKey() - deltaNow);
			}

			ArrayList<RegisteredInterrupt> callTargets = new ArrayList<RegisteredInterrupt>();
			FibonacciHeapNode<RegisteredInterrupt> closestEvent = SystemClock.this.interruptHeap.min();
			while (closestEvent.getKey() < 0) {
				// Build a collection of registrations whose interrupts have just fired. Adjust
				// the previous expected
				// interval based on the adjustment just calculated, and also use it to set an
				// expected value for the
				// next iteration.
				final RegisteredInterrupt closestData = closestEvent.getData();
				callTargets.add(closestData);

				// Compensate for the difference between when this latest clock tick was
				// expected to fire and the time it
				// actually did fire. It is sufficient to examine skew for present clock tick
				// only since we deducted
				// actual time spent from a calculated expected value in all each tick since the
				// last scheduling occurred.
				//
				// ** deltaNext > tickMillis implies we are in this iteration sooner than
				// expected and therefore wish to
				// deduct that shortfall from the previous expectation and added it onto the
				// next expected interval..
				// ** deltaNext < tickMillis implies we are late in this iteration and therefore
				// wish to deduct that lag
				// time from the next interval's expected duration and transfer it by addition
				// to previous expected value.
				closestData.previousActualInterval = closestData.nextExpectedInterval - iterationError;
				closestData.nextExpectedInterval = closestData.targetInterval + iterationError;

				// Remove the interruptHeap entry for the registration just accounted for and
				// then re-insert it with a refreshed
				// timer key.
				SystemClock.this.interruptHeap.removeMin();
				SystemClock.this.interruptHeap.insert(closestEvent, closestData.nextExpectedInterval);
				closestEvent = SystemClock.this.interruptHeap.min();
			}

			// Call the interrupt handler method for each RegisteredInterrupt that exhausted
			// its time span. Sort the list
			// by priority order first.
			Collections.sort(callTargets);
			for (final RegisteredInterrupt firedInterrupt : callTargets) {
				try {
					firedInterrupt.handler.call(firedInterrupt.previousActualInterval);
				} catch (Exception exp) {
					LOG.error("Unexpected exception thrown by registered interrupt handler discarded: ", exp);
				}
			}

			// Reschedule this handler for interrupt handling's next clock tick.
			SystemClock.this.interruptsSubscription.unsubscribe();
			SystemClock.this.interruptsSubscription =
				SystemClock.this.worker.schedule(
					new ProcessRecurringInterrupts(timeNow, nextTimeNext, deltaNext),
					deltaNext, TimeUnit.MILLISECONDS);
		}
	}

	public SystemClock(@Min(5) long tickMillis, @NotNull Scheduler scheduler) {
		this.tickMillis = tickMillis;
		this.worker = scheduler.createWorker();
		this.acceptingInterrupts = true;
		this.interruptHeap = new FibonacciHeap<RegisteredInterrupt>();
		this.allInterrupts = new ArrayList<FibonacciHeapNode<RegisteredInterrupt>>(10);
	}

	public void scheduleInterrupt(
		@Min(1) long cycleInterval, @NotNull TimeUnit intervalUnit, @Min(0) int priority, @NotNull IInterruptHandler handler)
	{
		Preconditions.checkState(this.acceptingInterrupts,
			"Interrupts may only be scheduled before calling SystemClock.begin()");

		long cycleMillis = intervalUnit.toMillis(cycleInterval);
		Preconditions.checkArgument(cycleMillis >= this.tickMillis,
			"Cannot repeat on an interval shorter than tick duration");

		if ((cycleMillis % this.tickMillis) > 0) {
			LOG.warn("Interrupt cycle of %s is not an even multiple of the tick duration, %s", cycleMillis,
					this.tickMillis);
		}

		final RegisteredInterrupt registration = new RegisteredInterrupt(handler, cycleMillis, priority);
		final FibonacciHeapNode<RegisteredInterrupt> node = new FibonacciHeapNode<RegisteredInterrupt>(registration);

		this.interruptHeap.insert(node, registration.nextExpectedInterval);
		this.allInterrupts.add(node);
	}

	@Override
	public void begin() {
		Preconditions.checkState(this.acceptingInterrupts,
			"SystemClock.begin() may only be called once, and must only be called after all interrupts are registered");
		this.acceptingInterrupts = false;
		final ProcessRecurringInterrupts interruptAction = new ProcessRecurringInterrupts();
		this.interruptsSubscription = 
			this.worker.schedule(interruptAction, interruptAction.deltaNext, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public Subscription scheduleOnce(Action0 action, long interval, TimeUnit timeUnit) {
		return this.worker.schedule(action, interval, timeUnit);
	}

	@Override
	public long now() {
		return this.worker.now();
	}
}
