package info.jchein.mesosphere.elevator.physics;

public interface IElevatorPhysicsService {
	boolean isTravelFast( int fromFloorIndex, int toFloorIndex );

	double floorDistance( int fromFloorIndex, int toFloorIndex );

	double travelTime( int fromFloorIndex, int toFloodIndex );
	
	double expectedStopDuration( int boardingCount, int disembarkingCount );
	
	int idealPassengerCount();
	
	int maxTolerancePassengerCount();
	
	JourneyArc getTraversalPath( int fromFloorIndex, int toFloorIndex );
	
	int getNumFloors();
	
	int getNumElevators();
	
	double getMetersPerFloor();
}
