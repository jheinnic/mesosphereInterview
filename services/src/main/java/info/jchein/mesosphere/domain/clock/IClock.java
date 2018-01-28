package info.jchein.mesosphere.domain.clock;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action0;

public interface IClock {
   long getMillisecondsPerTick();

	long now();

	void scheduleOnce(Action0 action, long interval, TimeUnit intervalUnit);
	
	void scheduleOnce(long interval, TimeUnit intervalUnit, int priority, IInterruptHandler handler);

	void scheduleInterrupt(long cycleInterval, TimeUnit intervalUnit, int priority, IInterruptHandler handler);
	
	void scheduleVariable(long firstInterval, TimeUnit intervalUnit, int priority, IRepeatIntervalFunction handler);

	void begin();
}
