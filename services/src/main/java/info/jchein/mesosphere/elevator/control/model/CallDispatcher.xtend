package info.jchein.mesosphere.elevator.control.model

import com.google.common.base.Preconditions
import com.google.common.eventbus.Subscribe
import info.jchein.mesosphere.elevator.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.control.event.DepartedLanding
import info.jchein.mesosphere.elevator.control.event.DropOffRequested
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded
import info.jchein.mesosphere.elevator.control.event.PickupCallRemoved
import info.jchein.mesosphere.elevator.control.event.TravelledThroughFloor
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated
import info.jchein.mesosphere.elevator.control.sdk.IElevatorDispatcherPort
import info.jchein.mesosphere.elevator.control.sdk.IElevatorDispatchingStrategy
import info.jchein.mesosphere.elevator.domain.scheduler.ElevatorCarPlan
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.builder.GraphBuilder

class CallDispatcher implements IElevatorDispatcherPort {
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
