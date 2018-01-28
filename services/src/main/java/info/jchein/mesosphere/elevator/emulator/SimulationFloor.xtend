package info.jchein.mesosphere.elevator.emulator

import com.google.common.eventbus.EventBus
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded
import info.jchein.mesosphere.elevator.emulator.ISimulationScenario
import info.jchein.mesosphere.elevator.simulator.ISimulatedPassenger
import java.util.concurrent.TimeUnit
import javax.validation.constraints.NotNull
import org.springframework.stereotype.Component
import java.util.Queue
import java.util.LinkedList
import javax.validation.constraints.Min

//@StatefulController(
//	value=LandingControls.BEAN_ID,
//	clazz=typeof(LandingControls),
//	startState=LandingControls.BOOTSTRAPPING
//)
@Component
class SimulationFloor implements ISimulationScenario {
	private val int floorIndex
	@NotNull private val EventBus eventBus
	@NotNull private val IClock systemClock
	@NotNull private val Queue<ISimulatedPassenger> goingUp
	@NotNull private val Queue<ISimulatedPassenger> goingDown

	BuildingProperties bldgProperties

	new(
		@Min(1) int floorIndex,
		@NotNull IClock systemClock,
		@NotNull EventBus eventBus,
		@NotNull BuildingProperties bldgProperties
	) {
		this.floorIndex = floorIndex
		this.eventBus = eventBus
		this.systemClock = systemClock
		this.bldgProperties = bldgProperties
		this.goingDown = new LinkedList<ISimulatedPassenger>()
		this.goingUp = new LinkedList<ISimulatedPassenger>()
	}

	override injectTraveller(long clockTime, int arrivalFloor, int destinationFloor) {
		this.systemClock.scheduleOnce(
		    [
			this.eventBus.post(
				PickupCallAdded.build [ b |
					b.timeIndex(clockTime).floorIndex(arrivalFloor).direction(
						if (arrivalFloor < destinationFloor) {
							DirectionOfTravel.GOING_UP
						} else {
							DirectionOfTravel.GOING_DOWN
						}
					)
				]
			);
		], (clockTime - this.systemClock.now()), TimeUnit.MILLISECONDS);
		
		// TODO: Record bookkeeping to put the passenger on the elevator when it arrives and a BoardingCar event
		// is fired.
		// TODO: Isn't this redundant with pressLandingPickupCall() ?  Remove the latter as it has less information,
		// and the simulation should have that even if the scheduler itself does not.  Especially so in fact.
	}

	override pressLandingPickupCall(long clockTime, int floorIndex, DirectionOfTravel direction) {
		this.systemClock.scheduleOnce(
		    [
			this.eventBus.post(
				PickupCallAdded.build [ b |
					b.timeIndex(clockTime).floorIndex(floorIndex).direction(direction)
				]
			);
		], (clockTime - this.systemClock.now()), TimeUnit.MILLISECONDS);
	}

	override pressPassengerDropCall(long clockTime, int carIndex, int floorIndex) {
		this.systemClock.scheduleOnce(
		    [
			this.eventBus.post(
				DropOffRequested.build [ b |
					b.clockTime(clockTime).carIndex(carIndex).dropOffFloorIndex(floorIndex)
				]
			);
		], (clockTime - this.systemClock.now()), TimeUnit.MILLISECONDS);
	}

}
