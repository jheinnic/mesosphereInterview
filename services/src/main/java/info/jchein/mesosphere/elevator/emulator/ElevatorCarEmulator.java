package info.jchein.mesosphere.elevator.emulator;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.physics.ElevatorMotorProperties;

public class ElevatorCarEmulator implements IElevatorCarDriver {
	private final IElevatorCarPort port;
	private ElevatorMotorProperties motorProperties;
	
	private double floorHeight;
	private double speed;
	private double acceleration;
	private double jerk;
	private ElevatorCarSnapshot carSnapshot;

	public ElevatorCarEmulator(IElevatorCarPort port, ElevatorCarSnapshot carSnapshot, ElevatorMotorProperties motorProperties) {
		this.port = port;
		this.carSnapshot = carSnapshot;
		this.motorProperties = motorProperties;
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
