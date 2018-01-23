package info.jchein.mesosphere.elevator.simulator.traveller;

import java.io.IOException;

import org.javasim.streams.NormalStream;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.simulator.util.TravellerContext;

class Activity {
	private final String name;
	private final NormalStream normalDuration;
	private final IProbabilityResolver<String> nextFn;
	private final ActivityLocation location;
	private final IClock systemClock;
	private final EventBus eventBus;

	public Activity(String name, NormalStream normalDuration, IProbabilityResolver<String> nextFn,
			ActivityLocation location, IClock systemClock, EventBus eventBus) {
		this.name = name;
		this.normalDuration = normalDuration;
		this.nextFn = nextFn;
		this.location = location;
		this.systemClock = systemClock;
		this.eventBus = eventBus;
	}
	
	public TravellerContext beginActivity(TravellerContext previousState) {
		final long now = this.systemClock.now();
		final long activityEnd;
		try {
			activityEnd = Math.round(now + this.normalDuration.getNumber());
		} catch (ArithmeticException | IOException e) {
			throw new RuntimeException("TODO", e);
		}
		
		this.systemClock.scheduleOnce()

		return previousState.copy(copier -> {
			copier.currentActivity(this.name)
				.currentLocation(this.location)
				.lastEventTime(previousState.getNextEventTime())
				.nextEventTime(nextTime);
		});
	}

}
