package info.jchein.mesosphere.elevator.domain.model

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import java.util.BitSet
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDispatcherPort
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDispatchingStrategy
import com.google.common.base.Preconditions
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.car.event.DepartedLanding
import info.jchein.mesosphere.elevator.domain.car.event.WeightLoadUpdated
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallRemoved
import info.jchein.mesosphere.elevator.domain.car.event.TravelledThroughFloor
import info.jchein.mesosphere.elevator.domain.car.event.ParkedAtLanding
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded
import info.jchein.mesosphere.elevator.domain.car.event.PassengerDoorsOpened
import info.jchein.mesosphere.elevator.domain.car.event.PassengerDoorsClosed
import com.google.common.eventbus.Subscribe
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import info.jchein.mesosphere.elevator.domain.scheduler.ElevatorCarPlan

class ElevatorCallDispatcher implements IElevatorDispatcherPort {
	private var IElevatorDispatchingStrategy strategy;
	private var GraphBuilder<?, ?, DefaultDirectedWeightedGraph<?, ?>> graphBuilder
	private val ElevatorCarPlan[] carPlans = null
	private val DefaultDirectedWeightedGraph<?, ?> aGraph = null

	def void attachStrategy(IElevatorDispatchingStrategy strategy) {
		Preconditions.checkState(this.strategy === null, "Elevator Dispatcher already has an attached strategy object");

		this.strategy = strategy
	}

	override assignPickupCar(int floorIndex, DirectionOfTravel initialDirection, int carIndex)
	{
	}

	@Subscribe
	public def updateWeightLoad(WeightLoadUpdated event) {}

	@Subscribe
	public def onPickupCallAdded(PickupCallAdded event) {}

	@Subscribe
	public def onPickupCallRemoved(PickupCallRemoved event) {}

	@Subscribe
	public def onDropOffRequested(DropOffRequested event) {}

	@Subscribe
	public def onParkedAtLanding(ParkedAtLanding event) {}

	@Subscribe
	public def onDepartedLanding(DepartedLanding event) {}

	@Subscribe
	public def onPassengerDoorsOpened(PassengerDoorsOpened event) {}

	@Subscribe
	public def onPassengerDoorsClosed(PassengerDoorsClosed event) {}

	@Subscribe
	public def onTravelledThroughFloor(TravelledThroughFloor event) {}
}
