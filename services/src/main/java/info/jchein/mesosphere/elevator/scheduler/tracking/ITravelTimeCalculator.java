package info.jchein.mesosphere.elevator.scheduler.tracking;

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;

public interface ITravelTimeCalculator {
	double getFloorTravelTime(int fromFloorIndex, int toFloorIndex);
}
