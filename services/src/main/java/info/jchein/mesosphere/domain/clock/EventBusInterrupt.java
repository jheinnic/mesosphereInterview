package info.jchein.mesosphere.domain.clock;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.common.eventbus.EventBus;

public class EventBusInterrupt implements IInterruptHandler {
	public class EventBusClockEvent {
		EventBusClockEvent() { }
	}

	public final EventBusClockEvent CLOCK_EVENT = new EventBusClockEvent();

	private EventBus eventBus;

	private IClock systemClock;

	private long cycle;

	private TimeUnit cycleUnit;

	private int priority;
	
	public EventBusInterrupt(IClock systemClock, EventBus eventBus, long cycle, TimeUnit cycleUnit, int priority) {
		this.eventBus = eventBus;
		this.systemClock = systemClock;
		this.cycle = cycle;
		this.cycleUnit = cycleUnit;
		this.priority = priority;
	}
	
	@PostConstruct
	public void init() {
		this.systemClock.scheduleInterrupt(this.cycle, this.cycleUnit, this.priority, this);
	}

	@Override
	public void call(Long arg0) {
		this.eventBus.post(CLOCK_EVENT);
	}
}
