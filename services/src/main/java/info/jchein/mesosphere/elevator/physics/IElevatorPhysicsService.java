package info.jchein.mesosphere.elevator.physics;

public interface IElevatorPhysicsService {
	int floorDistance( int fromFloorIndex, int toFloorIndex );

	double travelTime( int fromFloorIndex, int toFloodIndex );
	
	double expectedStopDuration( int boardingCount, int disembarkingCount );
	
	int idealPassengerCount();
	
	int maxTolerancePassengerCount();
	
	
}
