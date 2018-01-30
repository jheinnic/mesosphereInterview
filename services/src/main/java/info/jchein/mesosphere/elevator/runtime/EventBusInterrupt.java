package info.jchein.mesosphere.elevator.runtime;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.common.eventbus.EventBus;

public class EventBusInterrupt implements IIntervalHandler {
	public class EventBusClockEvent {
		EventBusClockEvent() { }
	}

	public final EventBusClockEvent CLOCK_EVENT = new EventBusClockEvent();

	private final IRuntimeEventBus eventBus;
	private final IRuntimeScheduler systemClock;
   private final SystemRuntimeProperties runtimeProps;
	
	public EventBusInterrupt(SystemRuntimeProperties runtimeProps, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus) {
		this.runtimeProps = runtimeProps;
      this.eventBus = eventBus;
		this.systemClock = scheduler;
	}
	
	@PostConstruct
	public void init() {
		this.systemClock.scheduleInterrupt(this.runtimeProps.getTickDurationMillis(), TimeUnit.MILLISECONDS, IRuntimeScheduler.HI_PRIORITY, this);
	}

	@Override
	public void call(Long arg0) {
		this.eventBus.post(CLOCK_EVENT);
	}
}
