package info.jchein.mesosphere.elevator.simulator.traveller;

import info.jchein.mesosphere.elevator.simulator.util.BehaviorModel;
import info.jchein.mesosphere.elevator.simulator.util.LocationCdf;

public interface IBehaviorStrategyFactory {
	IProbabilityResolver<String> parseCdfString(String activityCdf);
	
	IProbabilityResolver<ActivityLocation> compile(LocationCdf locationCdf);
	
	IBehaviorStrategy initializeBehaviorStrategy(BehaviorModel model, int floorA, int floorB, int floorC);
}
