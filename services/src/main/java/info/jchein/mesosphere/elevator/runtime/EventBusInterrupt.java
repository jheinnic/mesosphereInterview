package info.jchein.mesosphere.elevator.runtime;


import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeProperties;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EventBusInterrupt // implements IIntervalHandler {
{
	public static class ClockTickLeadingEvent {
		ClockTickLeadingEvent() { }
	}

	public static class ClockTickTrailingEvent {
		ClockTickTrailingEvent() { }
	}

	public static final ClockTickLeadingEvent LEADING_CLOCK_EVENT = new ClockTickLeadingEvent();
	public static final ClockTickTrailingEvent TRAILING_CLOCK_EVENT = new ClockTickTrailingEvent();

	private final IRuntimeEventBus eventBus;
	private final IRuntimeScheduler scheduler;
   private final VirtualRuntimeProperties runtimeProps;
	
   @Autowired
	public EventBusInterrupt(VirtualRuntimeProperties runtimeProps, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus) {
		this.runtimeProps = runtimeProps;
      this.eventBus = eventBus;
		this.scheduler = scheduler;
	}
	
	@PostConstruct
	public void init() {
	   final long tickDurationMillis = this.runtimeProps.getTickDurationMillis();
      this.scheduler.scheduleInterrupt(tickDurationMillis, TimeUnit.MILLISECONDS, IRuntimeScheduler.HI_PRIORITY, this::leadingCall);
		this.scheduler.scheduleInterrupt(tickDurationMillis, TimeUnit.MILLISECONDS, IRuntimeScheduler.LO_PRIORITY, this::trailingCall);
	}

	void leadingCall(long arg0) {
		this.eventBus.post(LEADING_CLOCK_EVENT);
	}

	void trailingCall(long arg0) {
		this.eventBus.post(TRAILING_CLOCK_EVENT);
	}
}
