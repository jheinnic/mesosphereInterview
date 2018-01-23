package info.jchein.mesosphere.elevator.simulator;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;

public class ElevatorCarEmulator implements IElevatorCarDriver {
	private final IElevatorCarPort port;

	public ElevatorCarEmulator(IElevatorCarPort port) {
		this.port = port;
	}

	@Override
	public void moveToDestination(int floorIndex, DirectionOfTravel direction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parkForBoarding() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void launchForService() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFloorSensorEvent(FloorSensorTriggered event) {
		// TODO Auto-generated method stub
		
	}
}
