package info.jchein.mesosphere.elevator.scheduler.tracking;

import java.util.PriorityQueue;

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.configuration.properties.PassengerToleranceProperties;
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested;
import info.jchein.mesosphere.elevator.domain.car.event.ParkedForBoarding;
import info.jchein.mesosphere.elevator.domain.car.event.ReadyForDeparture;
import info.jchein.mesosphere.elevator.domain.car.event.SlowedForArrival;
import info.jchein.mesosphere.elevator.domain.car.event.TravelledThroughFloor;
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallRemoved;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerPort;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;

public class HeuristicElevatorSchedulingStrategy implements IElevatorSchedulerDriver {

	// private final GraphBuilder<FloorStopReport, SequentialStops, DefaultDirectedWeightedGraph<FloorStopReport,SequentialStops>> routeBuilder;
	private IElevatorSchedulerPort port;
	private ITrafficPredictor trafficPredictor;
	private BuildingProperties bldgProps;
	private ElevatorDoorProperties doorProps;
	private ElevatorMotorProperties motorProps;
	private ElevatorWeightProperties weightProps;
	private PassengerToleranceProperties toleranceProps;
	private IElevatorPhysicsService physicsService;
	

	public HeuristicElevatorSchedulingStrategy(
		final IElevatorSchedulerPort port, final ITrafficPredictor trafficPredictor,
		final BuildingProperties bldgProps, final ElevatorDoorProperties doorProps,
		final ElevatorMotorProperties motorProps, final ElevatorWeightProperties weightProps,
		final PassengerToleranceProperties toleranceProps, final IElevatorPhysicsService physicsService
	) {
		this.port = port;
		this.trafficPredictor = trafficPredictor;
		this.bldgProps = bldgProps;
		this.doorProps = doorProps;
		this.motorProps = motorProps;
		this.weightProps = weightProps;
		this.toleranceProps = toleranceProps;
		this.physicsService = physicsService;
	}


	@Override
	public void pollForClock() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void bootstrapModel(ElevatorCarSnapshot[] carState) {
		
		
	}


	@Override
	public int assignPickupCall(PickupCallAdded event) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void onPickupCallRemoved(PickupCallRemoved event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDropOffRequested(DropOffRequested event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onReadyForDeparture(ReadyForDeparture event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onParkedForBoarding(ParkedForBoarding event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSlowedForArrival(SlowedForArrival event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTravelledThroughFloor(TravelledThroughFloor event) {
		// TODO Auto-generated method stub
		
	}
	
	static class ElevatorCarPlan {
		PriorityQueue itinerary
	}

}
