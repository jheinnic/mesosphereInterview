package info.jchein.mesosphere.elevator.simulator.traveller.events;

public enum TravellerEventType {
	ENTERED_SIMULATION,
	BEGAN_NEXT_DAY_PHASE,
	CALLED_FOR_ELEVATOR,
	BOARDED_ELEVATOR,
	LEFT_ELEVATOR,
	STARTED_ACTIVITY,
	COMPLETED_ACTIVITY,
	EXITED_SIMULATION
}
