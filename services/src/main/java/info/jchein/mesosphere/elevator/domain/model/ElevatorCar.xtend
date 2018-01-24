package info.jchein.mesosphere.elevator.domain.model

import com.google.common.eventbus.EventBus
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.domain.car.event.DestinationChanged
import info.jchein.mesosphere.elevator.domain.car.event.DestinationRetained
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.DoorState
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

class ElevatorCar implements IElevatorCar, IElevatorCarPort {
	final IClock systemClock
	final EventBus eventBus
	final ElevatorCarSnapshot state
	final int carIndex

	IElevatorCarDriver driver = null

	new(@Min(0) int carIndex, @NotNull IClock systemClock, @NotNull EventBus eventBus) {
		this(carIndex, systemClock, eventBus, null);
	}

	new(@Min(0) int carIndex, @NotNull IClock systemClock, @NotNull EventBus eventBus, ElevatorCarSnapshot initialState) {
		this.systemClock = systemClock;
		this.eventBus = eventBus
		this.carIndex = carIndex
		if (initialState === null) {
			this.state = ElevatorCarSnapshot.builder().build()
		} else {
			this.state = initialState.copy().build()
		}
	}

	public def void attachDriver(IElevatorCarDriver driver) {
		if (this.driver !== null) {
			throw new RuntimeException("Driver has already been attached")
		}
		this.driver = driver
	}


	override assignDestination(int floorIndex, DirectionOfTravel direction) {
		this.driver.moveToDestination(floorIndex, direction);
	}

	override destinationAccepted(int floorIndex, DirectionOfTravel direction) {
		this.eventBus.post(
			DestinationChanged.build[bldr |
				bldr.floorIndex(floorIndex)
				.direction(direction)
				.timeIndex(this.systemClock.now())]
		);
	}

	override destinationRejected(int rejectedFloorIndex, int retainedFloorIndex, DirectionOfTravel direction, String reason)
	{
		this.eventBus.post(
			DestinationRetained.build[bldr |
				bldr.carIndex(carIndex)
				.rejectedDestination(rejectedFloorIndex)
				.retainedDestination(retainedFloorIndex)
				.currentDirection(this.state.direction)
				.currentSpeed(this.state.speed)
				.timeIndex(this.systemClock.now())
				.reason(reason)]
		);
	}

	
	override idleForDeparture(int floorIndex) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override parkedForBoarding(int floorIndex) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override passengerLoadUpdated(int estimatedArrivals, int estimatedDepartures, int estimatedPassengerCount, double weightOnArrival, double weightOnDeparture) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override slowedForArrival(int floorIndex) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override travelledThroughFloor(int floorIndex, DirectionOfTravel direction) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override doorStateChanging(DoorState newStatus) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	override dropOffRequsted(int floorIndex) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

}
