package info.jchein.mesosphere.elevator.runtime.virtual;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import info.jchein.mesosphere.elevator.runtime.IIntervalHandler;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;

public class EventBusInterrupt implements IIntervalHandler {
	public class EventBusClockEvent {
		EventBusClockEvent() { }
	}

	public final EventBusClockEvent CLOCK_EVENT = new EventBusClockEvent();

	private final IRuntimeEventBus eventBus;
	private final IRuntimeScheduler systemClock;
   private final VirtualRuntimeProperties runtimeProps;
	
	public EventBusInterrupt(VirtualRuntimeProperties runtimeProps, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus) {
		this.runtimeProps = runtimeProps;
      this.eventBus = eventBus;
		this.systemClock = scheduler;
	}
	
	@PostConstruct
	public void init() {
		this.systemClock.scheduleInterrupt(this.runtimeProps.getTickDurationMillis(), TimeUnit.MILLISECONDS, IRuntimeScheduler.HI_PRIORITY, this);
	}

	@Override
	public void call(long arg0) {
		this.eventBus.post(CLOCK_EVENT);
	}
}
