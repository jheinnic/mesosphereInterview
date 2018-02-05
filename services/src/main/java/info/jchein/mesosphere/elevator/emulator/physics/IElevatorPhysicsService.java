package info.jchein.mesosphere.elevator.emulator.physics;

public interface IElevatorPhysicsService {
	boolean isTravelFast( int fromFloorIndex, int toFloorIndex );

	double getFloorDistance( int fromFloorIndex, int toFloorIndex );

	double getTravelTime( int fromFloorIndex, int toFloodIndex );
	
	double getExpectedStopDuration( int boardingCount, int disembarkingCount );
	
	int getIdealPassengerCount();
	
	int getMaxTolerancePassengerCount();
	
	JourneyArc getTraversalPath( int fromFloorIndex, int toFloorIndex );
	
	int getNumFloors();
	
	int getNumElevators();
	
	double getMetersPerFloor();
}
