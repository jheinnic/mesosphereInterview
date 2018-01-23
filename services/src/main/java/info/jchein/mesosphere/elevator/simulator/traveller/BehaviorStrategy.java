package info.jchein.mesosphere.elevator.simulator.traveller;

import java.io.IOException;
import java.util.ArrayList;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.javasim.streams.UniformStream;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;

class BehaviorStrategy implements IBehaviorStrategy {

	private final ArrayList<DayPhase> dayPhases;
	private final IProbabilityResolver<String> initActivity;
	private final IProbabilityResolver<ActivityLocation> initLocation;
	private final UniformStream prng;
	private final int floorA;
	private final int floorB;
	private final int floorC;
	private final IClock systemClock;
	private final EventBus eventBus;

	BehaviorStrategy(@NotNull IClock systemClock, @NotNull EventBus eventBus,
			@NotNull @Size(min = 1) ArrayList<DayPhase> dayPhases, @NotNull UniformStream prng,
			IProbabilityResolver<String> initActivity, IProbabilityResolver<ActivityLocation> initLocation,
			@Min(1) int floorA, @Min(1) int floorB, @Min(1) int floorC) {
		this.systemClock = systemClock;
		this.eventBus = eventBus;
		this.dayPhases = dayPhases;
		this.initActivity = initActivity;
		this.initLocation = initLocation;
		this.prng = prng;
		this.floorA = floorA;
		this.floorB = floorB;
		this.floorC = floorC;
	}

	@Override
	public TravellerContext allocateNewTraveller() {
		final ActivityLocation initialLocation;
		final String initialActivity;
		final long now;

		try {
			initialLocation = initLocation.resolve(this.prng.getNumber());
			initialActivity = initActivity.resolve(this.prng.getNumber());
			now = this.systemClock.now();
		} catch (ArithmeticException | IOException e) {
			throw new RuntimeException("TODO", e);
		}

		// Travelers get an initial location and point in time. Their first activity has
		// been selected, but not yet activated, nor has their initial time-of-day phase
		// been randomized yet. These pending things are to occur on first use, just
		// like the first activity shift of every other ToD phase after this first one.
		return TravellerContext.build(bldr -> {
			bldr.currentTime(now)
				.currentActivity("")
				.currentLocation(initialLocation)
				.nextActivity(initialActivity)
				.nextActivityStartTime(now)
				.currentPhaseIndex(-1)
				.nextPhaseStartTime(now)
				.workFloorA(this.floorA)
				.workFloorB(this.floorB)
				.workFloorC(this.floorC)
				.activityLifecycleStage(ActivityLifecycleStage.BOOTSTRAP);
		});
	}
}