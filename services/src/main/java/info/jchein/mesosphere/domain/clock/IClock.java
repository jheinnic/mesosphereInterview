package info.jchein.mesosphere.domain.clock;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action0;

public interface IClock {
	/**
	 * Interrupts may only be scheduled during bootstrap, but non-recurring actions may be scheduled for subsequent 
	 * execution at any time.
	 * 
	 * @param action
	 * @param interval
	 * @param timeUnit
	 * @return
	 */
	Subscription scheduleOnce(Action0 action, long interval, TimeUnit timeUnit);
	
	long now();

	void scheduleInterrupt(long cycleInterval, TimeUnit intervalUnit, int priority, IInterruptHandler handler);

	void begin();
}
