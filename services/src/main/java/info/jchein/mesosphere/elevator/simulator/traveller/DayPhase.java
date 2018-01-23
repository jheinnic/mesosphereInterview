package info.jchein.mesosphere.elevator.simulator.traveller;

import java.util.HashMap;

import org.javasim.streams.NormalStream;

import info.jchein.mesosphere.elevator.simulator.util.TravellerContext;

class DayPhase {
	private final HashMap<String, Activity> activitiesByName;
	private final NormalStream phaseDuration;

	DayPhase(HashMap<String, Activity> activitiesByName, NormalStream phaseDuration) {
		this.activitiesByName = activitiesByName;
		this.phaseDuration = phaseDuration;
	}
	
	public TravellerContext beginPhase(TravellerContext lastState, String initialActivity) {
		final Activity nextActivity = this.activitiesByName.get(initialActivity);
		final long now = lastState.get
	}
}
