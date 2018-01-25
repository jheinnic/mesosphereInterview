package info.jchein.mesosphere.elevator.domain.model

import com.google.common.eventbus.EventBus
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.car.event.ParkedForBoarding
import info.jchein.mesosphere.elevator.domain.car.event.ReadyForDeparture
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import info.jchein.mesosphere.elevator.domain.common.TravelItineraryItem
import java.util.concurrent.atomic.AtomicInteger

class ElevatorCar implements IElevatorCar, IElevatorCarPort {
	private static final AtomicInteger ID_SEQUENCE = new AtomicInteger(0);
	
	final IClock systemClock
	final EventBus eventBus
	final int carIndex

	IElevatorCarDriver driver = null
	ElevatorCarSnapshot state = null
	TravelItineraryItem[] itinerary = null

	new(@NotNull IClock systemClock, @NotNull EventBus eventBus) {
		this.systemClock = systemClock;
		this.eventBus = eventBus
		this.carIndex = ID_SEQUENCE.incrementAndGet();
	}

	public def void attachDriver(IElevatorCarDriver driver) {
		if (this.driver !== null) {
			throw new RuntimeException("Driver has already been attached")
		}
		this.driver = driver
	}


	override pollForBootstrap() {
		return this.driver.pollForBootstrap();
	}
	
	override pollForClock() {
//		ElevatorCarSnapshot state this.driver.pollForService(this.itinerary);
	}
	
	override cancelPickupRequest(int floorIndex, DirectionOfTravel direction) {
//		this.driver.cancelQueuedPickup(floorIndex, direction);
	}
	
	override enqueuePickupRequest(int floorIndex, DirectionOfTravel direction) {
//		this.driver.queueForPickup(floorIndex, direction);
	}	
	
	override dropOffRequested(int floorIndex) {
		this.eventBus.post(
			DropOffRequested.build[
				it.timeIndex(this.systemClock.now())
				.carIndex(this.carIndex)
				.dropOffFloorIndex(floorIndex)
			]
		)
	}

	override readyForDeparture(int floorIndex, double weightLoad) {
		this.eventBus.post(
			ReadyForDeparture.build[ bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(floorIndex)
					.timeIndex(this.systemClock.now())
					.weightOnDeparture(weightLoad)
			]
		);
	}
	
	override parkedForBoarding(int floorIndex, double weightLoad) {
		this.eventBus.post(
			ParkedForBoarding.build[ bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(floorIndex)
					.timeIndex(this.systemClock.now())
					.weightOnArrival(weightLoad)
			]
		);
	}
	
//	override slowedForArrival(int floorIndex) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
	
//	override travelledThroughFloor(int floorIndex, DirectionOfTravel direction) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
	
//	override doorStateChanging(DoorState newStatus) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}

}
