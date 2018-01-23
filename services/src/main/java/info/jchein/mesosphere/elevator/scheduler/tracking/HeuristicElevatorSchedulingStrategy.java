package info.jchein.mesosphere.elevator.scheduler.tracking;

import info.jchein.mesosphere.elevator.domain.car.event.DestinationRetained;
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested;
import info.jchein.mesosphere.elevator.domain.car.event.ParkedForBoarding;
import info.jchein.mesosphere.elevator.domain.car.event.ReadyForDeparture;
import info.jchein.mesosphere.elevator.domain.car.event.SlowedForArrival;
import info.jchein.mesosphere.elevator.domain.car.event.TravelledThroughFloor;
import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupSnapshot;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallRemoved;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerPort;
import info.jchein.mesosphere.elevator.physics.BuildingProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.physics.PassengerToleranceProperties;

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
	public void onPickupCallAdded(PickupCallAdded event) {
	}

	@Override
	public void onPickupCallRemoved(PickupCallRemoved event) {
	}

	@Override
	public void onDropOffRequested(DropOffRequested event) {
	}

	@Override
	public void onDestinationRetained(DestinationRetained event) {
	}

	@Override
	public void onReadyForDeparture(ReadyForDeparture event) {
	}

	@Override
	public void onParkedForBoarding(ParkedForBoarding event) {
	}

	@Override
	public void onSlowedForArrival(SlowedForArrival event) {
	}

	@Override
	public void onTravelledThroughFloor(TravelledThroughFloor event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bootstrapModel(ElevatorGroupSnapshot currentState) {
		// TODO Auto-generated method stub
		
	}
}
