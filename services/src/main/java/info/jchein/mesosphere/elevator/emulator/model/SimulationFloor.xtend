package info.jchein.mesosphere.elevator.emulator.model

import info.jchein.mesosphere.elevator.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPassenger
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.TimeUnit
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import org.springframework.stereotype.Component
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded
import info.jchein.mesosphere.elevator.control.event.DropOffRequested

//@StatefulController(
//	value=LandingControls.BEAN_ID,
//	clazz=typeof(LandingControls),
//	startState=LandingControls.BOOTSTRAPPING
//)
@Component
class SimulationFloor implements IEmulatedLandingControls {
	@Min(0) private val int floorIndex
	@NotNull private val IRuntimeEventBus eventBus
	@NotNull private val IRuntimeClock clock
	@NotNull private val IRuntimeScheduler scheduler
	@NotNull private val Queue<ISimulatedPassenger> goingUp
	@NotNull private val Queue<ISimulatedPassenger> goingDown

	ElevatorGroupBootstrap bldgProperties

	new(
		@Min(1) int floorIndex,
		@NotNull IRuntimeClock clock,
		@NotNull IRuntimeEventBus eventBus,
		@NotNull IRuntimeScheduler scheduler,
		@NotNull ElevatorGroupBootstrap bldgProperties
	) {
		this.floorIndex = floorIndex
		this.clock = clock
		this.eventBus = eventBus
		this.scheduler = scheduler
		this.bldgProperties = bldgProperties
		this.goingDown = new LinkedList<ISimulatedPassenger>()
		this.goingUp = new LinkedList<ISimulatedPassenger>()
	}
	
	override pressLandingPickupCall(DirectionOfTravel direction) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

/*
	override injectTraveller(long clockTime, int arrivalFloor, int destinationFloor) {
		this.scheduler.scheduleOnce(
		    [
			this.eventBus.post(
				PickupCallAdded.build [ b |
					b.clockTime(clockTime).floorIndex(arrivalFloor).direction(
						if (arrivalFloor < destinationFloor) {
							DirectionOfTravel.GOING_UP
						} else {
							DirectionOfTravel.GOING_DOWN
						}
					)
				]
			);
		], (clockTime - this.clock.now()), TimeUnit.MILLISECONDS);
		
		// TODO: Record bookkeeping to put the passenger on the elevator when it arrives and a BoardingCar event
		// is fired.
		// TODO: Isn't this redundant with pressLandingPickupCall() ?  Remove the latter as it has less information,
		// and the simulation should have that even if the scheduler itself does not.  Especially so in fact.
	}

	override pressLandingPickupCall(long clockTime, int floorIndex, DirectionOfTravel direction) {
		this.scheduler.scheduleOnce(
		    [
			this.eventBus.post(
				PickupCallAdded.build [ b |
					b.clockTime(clockTime).floorIndex(floorIndex).direction(direction)
				]
			);
		], (clockTime - this.clock.now()), TimeUnit.MILLISECONDS);
	}

	override pressPassengerDropCall(long clockTime, int carIndex, int floorIndex) {
		this.scheduler.scheduleOnce(
		    [
			this.eventBus.post(
				DropOffRequested.build [ b |
					b.clockTime(clockTime).carIndex(carIndex).dropOffFloorIndex(floorIndex)
				]
			);
		], (clockTime - this.clock.now()), TimeUnit.MILLISECONDS);
	}
*/
}
