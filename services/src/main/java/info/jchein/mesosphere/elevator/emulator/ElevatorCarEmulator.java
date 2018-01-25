package info.jchein.mesosphere.elevator.emulator;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.common.TravelItineraryItem;
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.physics.JourneyArc;
import info.jchein.mesosphere.elevator.physics.PathMoment;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties;

@Component
public class ElevatorCarEmulator implements IElevatorCarDriver {
	private final IElevatorCarPort port;
	private ElevatorMotorProperties motorProperties;
	private ElevatorCarSnapshot carSnapshot;

	private LinkedList<TravelItineraryItem> plannedRoute;
	private PathMoment physicsState;
	private JourneyArc trajectory;
	
	public ElevatorCarEmulator(IElevatorCarPort port, ElevatorCarSnapshot carSnapshot, ElevatorMotorProperties motorProperties) {
		this.port = port;
		this.carSnapshot = carSnapshot;
		this.motorProperties = motorProperties;
	}

	@Override
	public void queue(long timestamp, int floorIndex, DirectionOfTravel direction) {
		this.carSnapshot.copy(bldr -> {

		})
	}

//	@Override
//	public void parkForBoarding() {
//	}

	@Override
	public void launchForService() {
		
	}

	@Override
	public void onFloorSensorEvent(FloorSensorTriggered event) {
		// TODO Auto-generated method stub
		
	}
}
