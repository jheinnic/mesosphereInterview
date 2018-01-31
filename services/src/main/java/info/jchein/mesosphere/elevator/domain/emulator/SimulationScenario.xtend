package info.jchein.mesosphere.elevator.emulator

import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler
import info.jchein.mesosphere.elevator.simulator.ISimulatedPassenger
import java.util.BitSet
import java.util.Queue
import java.util.concurrent.TimeUnit
import javax.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.LinkedList

//@StatefulController(
//	value=LandingControls.BEAN_ID,
//	clazz=typeof(LandingControls),
//	startState=LandingControls.BOOTSTRAPPING
//)
@Component
class SimulationScenario implements ISimulationScenario {
	@NotNull private val IRuntimeEventBus eventBus
	@NotNull private val IRuntimeClock clock
	@NotNull private val IRuntimeScheduler scheduler
	@NotNull private val BitSet upwardCalls
	@NotNull private val BitSet downwardCalls
	@NotNull private val Queue<ISimulatedPassenger> simulatedPassengers
	@NotNull private val ElevatorGroupBootstrap bldgProperties

	@Autowired
	new(
		@NotNull IRuntimeClock clock,
		@NotNull IRuntimeEventBus eventBus,
		@NotNull IRuntimeScheduler scheduler,
		@NotNull ElevatorGroupBootstrap bldgProperties
	) {
		this.scheduler = scheduler
		this.eventBus = eventBus
		this.clock = clock
		this.upwardCalls = new BitSet()
		this.downwardCalls = new BitSet()
		this.bldgProperties = bldgProperties
		this.simulatedPassengers = new LinkedList<ISimulatedPassenger>()
	}

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

}
