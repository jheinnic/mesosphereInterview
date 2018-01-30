package info.jchein.mesosphere.elevator.scheduler.tracking;

public interface ITravelTimeCalculator {
	double getFloorTravelTime(int fromFloorIndex, int toFloorIndex);
}
