package info.jchein.mesosphere.elevator.domain.model

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import de.oehme.xtend.contrib.logging.slf4j.Slf4j
import info.jchein.mesosphere.elevator.domain.car.event.DepartedLanding
import info.jchein.mesosphere.elevator.domain.car.event.DriverBootstrapped
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.car.event.ParkedAtLanding
import info.jchein.mesosphere.elevator.domain.car.event.PassengerDoorsClosed
import info.jchein.mesosphere.elevator.domain.car.event.PassengerDoorsOpened
import info.jchein.mesosphere.elevator.domain.car.event.SlowedForArrival
import info.jchein.mesosphere.elevator.domain.car.event.TravelledThroughFloor
import info.jchein.mesosphere.elevator.domain.car.event.WeightLoadUpdated
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.InitialElevatorCarState
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus
import java.util.BitSet
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import javax.validation.constraints.NotNull
import org.eclipse.xtend.lib.annotations.AccessorType
import org.eclipse.xtend.lib.annotations.Accessors
import org.springframework.beans.factory.annotation.Autowired
import org.statefulj.framework.core.annotations.FSM
import org.statefulj.framework.core.annotations.StatefulController
import org.statefulj.framework.core.annotations.Transition
import org.statefulj.framework.core.annotations.Transitions
import org.statefulj.framework.core.model.StatefulFSM
import org.statefulj.persistence.annotations.State

import static extension org.eclipse.xtend.lib.annotations.AccessorType.*

@StatefulController(
	value=ElevatorCar.BEAN_NAME,
	clazz = typeof(ElevatorCar),
	startState = ElevatorCar.BEFORE_ALLOCATION,
	blockingStates = #[ElevatorCar.BOARDING],
    noops = #[
    		@Transition(from=ElevatorCar.BEFORE_ALLOCATION, event=ElevatorCar.ALLOCATED, to=ElevatorCar.WAITING_FOR_DRIVER),
    		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.LANDING_BRAKE_APPLIED, to=ElevatorCar.SLOWING),
    		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.TRAVELLED_THROUGH_FLOOR, to=ElevatorCar.TRAVELLING),
    		@Transition(from=ElevatorCar.SLOWING, event=ElevatorCar.ARRIVED_AT_FLOOR, to=ElevatorCar.ARRIVING)
	]
)
@Slf4j
class ElevatorCar implements IElevatorCar, IElevatorCarPort {
	private static val AtomicInteger ID_SEQUENCE = new AtomicInteger(0);

	public static val String BEAN_NAME = "ElevatorCar"
	
	public static val String BEFORE_ALLOCATION = "StateBeforeAllocation";
	public static val String WAITING_FOR_DRIVER = "StateWaitingForDriver";
	public static val String AVAILABLE = "StateAvailable";
	public static val String TRAVELLING = "StateTraveling";
	public static val String SLOWING = "StateSlowing"
	public static val String ARRIVING = "StateAriving"
	public static val String BOARDING = "StateBoarding"
	public static val String SAFETY_LOCKOUT = "StateSafetyLockout"
	
	public static val String ALLOCATED = "EventAllocated";
	public static val String DRIVER_ATTACHED = "EventDriverSentBootstrap";
	public static val String PARKED = "EventParked";
	public static val String DISPATCHED = "EventDispatched";
	public static val String LANDING_BRAKE_APPLIED = "EventLandingBrakeApplied"
	public static val String TRIGGERED_FLOOR_SENSOR = "EventTriggeredFloorSensor"
	public static val String TRAVELLED_THROUGH_FLOOR = "EventTravelledThroughFloor"
	public static val String ARRIVED_AT_FLOOR = "EventArrivedAtFloor"
	public static val String STOPPED_AT_LANDING = "EventStoppedAtLanding"
	public static val String DOOR_CALL_RECEIVED = "EventDoorCallReceived"
	public static val String DOOR_CALL_COMPLETED = "EventDoorCallCompleted"
	public static val String DROPOFF_REQUESTED = "EventDropOffRequested"
	public static val String WEIGHT_UPDATED = "EventWeightUpdated"
	public static val String PANICKED = "EventPanicked"
	

	@State(accessorType = State.AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
	@Accessors(#[
		AccessorType.PACKAGE_GETTER,
		AccessorType.PACKAGE_SETTER
	])
	private var String state;
	
	@FSM
	var StatefulFSM<ElevatorCar> fsm;
	
	val int carIndex
	val IRuntimeClock clock
	val IRuntimeEventBus eventBus
	val IElevatorPhysicsService physicsService
	
	var IElevatorCarDriver driver = null
	var double currentWeightLoad = 0
	var int currentFloorIndex = -1
	
	var BitSet currentDropRequests = null
	val BitSet pickupsGoingUp = new BitSet()
	val BitSet pickupsGoingDown = new BitSet()
	var int reversePathFloorIndex = -1

//	Subscription schedulerPlanSubscription
	
	var DirectionOfTravel currentDirection
	var int currentDestination
	var DirectionOfTravel nextDirection
	var int nextDestination
	
	var long nextEventIndex = 1
	var long lastAckReceived = 0
	var long lastAckRequired = 0
	
//	PriorityQueue<ScheduledStop> nextStopQueue 
//	Comparator<ScheduledStop> latestSortOrigin
	
	@Autowired
	new(
		@NotNull IRuntimeClock clock, @NotNull IRuntimeEventBus eventBus, @NotNull IElevatorPhysicsService physicsService
	) {
		this.carIndex = ID_SEQUENCE.incrementAndGet();
		this.physicsService = physicsService
		this.clock = clock;
		this.eventBus = eventBus
	}
	
	@PostConstruct
	def init() {
		this.eventBus.registerListener(this);
	}

	def void attachDriver(IElevatorCarDriver driver, InitialElevatorCarState data)
	{
		if (this.driver !== null) {
			throw new RuntimeException("Driver has already been attached")
		}
		this.fsm.onEvent(this, ElevatorCar.DRIVER_ATTACHED, driver, data);
	}
	
	override dropOffRequested(int floorIndex) {
		this.fsm.onEvent(this, ElevatorCar.DROPOFF_REQUESTED, floorIndex);
	}

//	override readyForDeparture() {
//		if (this.state.equals(ElevatorCar.BOARDING)) {
//			this.fsm.onEvent(this, ElevatorCar.DOOR_CALL_COMPLETED)
//		} else {
//			this.fsm.onEvent(this, ElevatorCar.STOPPED_AT_LANDING)
//		}
//	}

    override slowedForArrival() {
 		this.fsm.onEvent(this, ElevatorCar.LANDING_BRAKE_APPLIED)   	
    }

	override parkedAtLanding() {
		this.fsm.onEvent(this, ElevatorCar.STOPPED_AT_LANDING)
	}
	
	override passengerDoorsClosed() {
		this.fsm.onEvent(this, ElevatorCar.DOOR_CALL_COMPLETED)
	}
	
	override updateWeightLoad(double weightLoad) {
		this.fsm.onEvent(this, ElevatorCar.WEIGHT_UPDATED, weightLoad)
	}
	
	//
	// EventBus dispatch methods
	//
	
	def onFloorSensorTriggered(FloorSensorTriggered event) {
		if (event.carIndex == this.carIndex) {
			this.fsm.onEvent(this, ElevatorCar.TRIGGERED_FLOOR_SENSOR, event.floorIndex, event.direction)
		}
	}
	
    @Transition(from=ElevatorCar.WAITING_FOR_DRIVER, event=ElevatorCar.DRIVER_ATTACHED, to=ElevatorCar.AVAILABLE)
    def onBootstrap(String event, IElevatorCarDriver driver, InitialElevatorCarState data)
    {
    		val initialFloor = data.getInitialFloor
    		val initialWeight = data.getWeightLoaded
    		val initialDropRequests = data.getRequestFloors
    		
		this.currentDestination = -1
		this.currentFloorIndex = initialFloor
		this.currentWeightLoad = initialWeight
		this.currentDropRequests = initialDropRequests.toBitSet()
		
		Preconditions.checkArgument(
			this.currentDropRequests.get(initialFloor) == false, 
			"Initial drop requests may not include the starting floor")

		val firstStopAbove = this.currentDropRequests.nextSetBit(initialFloor)
		val firstStopBelow = this.currentDropRequests.previousSetBit(initialFloor)

		Preconditions.checkArgument(
			firstStopBelow == -1 || firstStopAbove == -1,
			"Initial drop requests must all be above or all be below initial floor"
		);

		if (firstStopAbove > 0) {
			this.currentDirection = DirectionOfTravel.GOING_UP
			this.currentDestination = -1
			this.nextDestination = firstStopAbove
			this.reversePathFloorIndex = 
				this.currentDropRequests.previousSetBit(
					this.physicsService.numFloors - 1)
		} else if (firstStopBelow > 0) {
			this.currentDirection = DirectionOfTravel.GOING_DOWN
			this.currentDestination = -1
			this.nextDestination = firstStopBelow
			this.reversePathFloorIndex =
				this.currentDropRequests.nextSetBit(0)
		} else {
			this.currentDirection = DirectionOfTravel.STOPPED
			this.nextDestination = -1
			this.currentDestination = -1
			this.reversePathFloorIndex = -1
		}
		
		// This final event carries no state, but signals that the stream of floor request inforation is done.
		this.eventBus.post(
			DriverBootstrapped.build[bldr|
				bldr.clockTime(this.clock.now())
					.eventIndex(this.nextEventIndex++)
					.carIndex(this.carIndex)
					.floorIndex(this.currentFloorIndex)
					.weightLoad(this.currentWeightLoad)
					.initialDirection(this.currentDirection)
					.dropRequests(this.currentDropRequests.clone() as BitSet)
			]
		)
	}

	@Transitions(#[
		@Transition(from=ElevatorCar.AVAILABLE, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.AVAILABLE),
		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.TRAVELLING),
		@Transition(from=ElevatorCar.SLOWING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.SLOWING),
		@Transition(from=ElevatorCar.ARRIVING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.ARRIVING),
		@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.BOARDING)
	])
	def onDropOffRequested(int floorIndex) {
//		this.nextStopQueue.add(
//			ScheduledPickup.builder().floorIndex(floorIndex.intValue).build())
		this.currentDropRequests.set(floorIndex)

		this.eventBus.post(
			DropOffRequested.build[
				it.clockTime(this.clock.now())
					.eventIndex(this.nextEventIndex++)
					.carIndex(this.carIndex)
					.dropOffFloorIndex(floorIndex)
			]
		)
	}
	
	@Transitions(#[
		@Transition(from=ElevatorCar.AVAILABLE, event=ElevatorCar.DISPATCHED, to=ElevatorCar.TRAVELLING),
		@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.DISPATCHED, to=ElevatorCar.TRAVELLING)
	])
	def void onDispatch(String event)
	{
		this.currentDestination = this.nextDestination

		if (this.nextDestination == this.reversePathFloorIndex) {
			// No need to consider STOPPED here by definition
			if (this.currentDirection == DirectionOfTravel.GOING_UP) {
				val stopsGoingDown = (this.currentDropRequests.clone() as BitSet)
				stopsGoingDown.or(this.pickupsGoingDown)
				stopsGoingDown.clear(this.reversePathFloorIndex)
				
				this.nextDestination = stopsGoingDown.previousSetBit(this.reversePathFloorIndex)
				if (this.nextDestination >= 0) {
					// this.nextDirection 
				}
				val nextPickupDown =
					this.pickupsGoingDown.previousSetBit(this.reversePathFloorIndex - 1)
				val firstPickupUp = this.pickupsGoingUp.nextSetBit(0)
				val nextDropOff = this.currentDropRequests.previousSetBit(this.currentFloorIndex)
				val lastDropOff = this.currentDropRequests.nextSetBit(0)
				
				var newNextDestination = Math.max(nextPickupDown, lastDropOff)
				var newReversePathIndex = firstPickupUp
				
				if ((lastDropOff < 0) || (lastDropOff === this.reversePathFloorIndex))
				if (newNextDestination < 0) {
					if (firstPickupUp < 0) {
					}
				}
			}

			if (
				(this.currentDirection == DirectionOfTravel.GOING_UP) && 
				(
					this.currentDropRequests.get(this.reversePathFloorIndex) ||
				 	this.pickupsGoingDown.get(this.reversePathFloorIndex)
				)
			) {
				this.nextDirection = DirectionOfTravel.GOING_DOWN
				this.nextDestination = this.reversePathFloorIndex
			} else if (
				(this.currentDirection == DirectionOfTravel.GOING_DOWN) && 
				(
					this.currentDropRequests.get(this.reversePathFloorIndex) ||
				 	this.pickupsGoingDown.get(this.reversePathFloorIndex)
				)
			) {
				this.nextDirection = DirectionOfTravel.GOING_DOWN
				this.nextDestination = this.reversePathFloorIndex
			} else {
				this.nextDirection = DirectionOfTravel.STOPPED
				this.nextDestination = -1
			}
		} else if (this.currentDirection == DirectionOfTravel.GOING_UP) {
			this.currentDestination = this.nextDestination
			var nextDrop = this.currentDropRequests.nextSetBit(this.currentDestination + 1)
			var nextPickup = this.pickupsGoingUp.nextSetBit(this.currentDestination + 1)
			if (nextDrop > 0) {
				if (nextPickup > 0) {
					this.nextDestination = Math.min(nextDrop, nextPickup)
				} else {
					this.nextDestination = nextDrop
				}
			} else if (nextPickup > 0) {
				this.nextDestination = nextPickup
			}
		} else if (this.currentDirection == DirectionOfTravel.GOING_DOWN) {
			this.currentDestination = this.nextDestination
			var nextDrop = this.currentDropRequests.previousSetBit(this.currentDestination - 1)
			var nextPickup = this.pickupsGoingUp.previousSetBit(this.currentDestination - 1)

			// If nextPickup is negative, max will still return nextDrop, so no need to check
			// whether it is or isn't before callng Math.max()
			this.nextDestination = Math.max(nextDrop, nextPickup)
		}

		this.eventBus.post(
			DepartedLanding.build[ bldr |
				bldr.carIndex(this.carIndex)
				    .eventIndex(this.nextEventIndex++)
					.origin(this.currentFloorIndex)	
					.destination(this.currentDestination)
					.direction(this.currentDirection)
					.clockTime(this.clock.now())
			]
		)
	}
	

	@Transition(from=ElevatorCar.ARRIVING, event=ElevatorCar.STOPPED_AT_LANDING)
	def onStoppedAtLanding(String event)
	{
		this.currentDestination = -1
		this.currentDirection = DirectionOfTravel.STOPPED

		if ((this.nextDirection != DirectionOfTravel.STOPPED) ||
			(this.currentDropRequests.get(this.currentFloorIndex))) {
			return ElevatorCar.DOOR_CALL_RECEIVED
		} else {
			return ElevatorCar.PARKED
		}
	}

    /*
	private def checkNextDirectionAfterGoingUp() {
		return if (this.currentDropRequests.nextSetBit(this.currentFloorIndex) > 0) {
			DirectionOfTravel.GOING_UP
		} else {
			if (this.pickupsGoingUp.nextSetBit(this.currentFloorIndex) > 0) {
				DirectionOfTravel.STOPPED
			} else {
				DirectionOfTravel.GOING_UP
			}
		}
	}
	
	private def checkNextDirectionAfterGoingDown() {
		return if (this.currentDropRequests.previousSetBit(this.currentFloorIndex) > 0) {
			DirectionOfTravel.GOING_DOWN
		} else {
			if (this.pickupsGoingDown.previousSetBit(this.currentFloorIndex) > 0) {
				DirectionOfTravel.STOPPED
			} else {
				DirectionOfTravel.GOING_DOWN
			}
		}
	}
	*/
	
	@Transitions(#[
		@Transition(from=ElevatorCar.ARRIVING, event=ElevatorCar.PARKED, to=ElevatorCar.AVAILABLE),
		@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.PARKED, to=ElevatorCar.AVAILABLE)
	])
	def onParkedPendingDispatch(String event)
	{
		this.eventBus.post(
			ParkedAtLanding.build[bldr |
				bldr.clockTime(this.clock.now())
				    .eventIndex(this.nextEventIndex++)
					.carIndex(this.carIndex)
					.floorIndex(this.currentFloorIndex)
			]
		);
	}
			
	@Transitions(#[
		@Transition(from=ElevatorCar.ARRIVING, event=ElevatorCar.DOOR_CALL_RECEIVED, to=ElevatorCar.BOARDING),
		@Transition(from=ElevatorCar.AVAILABLE, event=ElevatorCar.DOOR_CALL_RECEIVED, to=ElevatorCar.BOARDING)
	])
	def onAnsweredCall(String event)
	{
		this.currentDropRequests.clear(this.currentFloorIndex)
		if (this.nextDirection == DirectionOfTravel.GOING_UP) {
			this.pickupsGoingUp.clear(this.currentFloorIndex)
		} else if(this.nextDirection == DirectionOfTravel.GOING_DOWN) {
			this.pickupsGoingDown.clear(this.currentFloorIndex)
		} else {
			throw new IllegalStateException("Cannot answer a call while planning to travel in the STOPPED direction");
		}

		this.eventBus.post(
			PassengerDoorsOpened.build[bldr |
				bldr.clockTime(this.clock.now())
				    .eventIndex(this.nextEventIndex++)
					.carIndex(this.carIndex)
					.floorIndex(this.currentFloorIndex)
					.direction(this.nextDirection)
			]
		)
	}
	
			
	@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.DOOR_CALL_COMPLETED)
	def onBoardingCompleted(String event)
	{
		this.eventBus.post(
			PassengerDoorsClosed.build[bldr |
				bldr.clockTime(this.clock.now())
				    .eventIndex(this.nextEventIndex++)
					.carIndex(this.carIndex)
			]
		)

		if (this.nextDestination < 0) {
			return ElevatorCar.PARKED;
		} else {
			return ElevatorCar.DISPATCHED;
		}
	}

	
	@Transitions(#[
		@Transition(from=ElevatorCar.SLOWING, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR),
		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR)
	])
	def String onTriggeredFloorSensor(String event, int floorIndex, DirectionOfTravel direction) 
	{
		this.currentFloorIndex = floorIndex

		if (
			(this.state === ElevatorCar.TRAVELLING) &&
			(this.currentDestination !== floorIndex) &&
			(this.currentDirection == direction) && (
				((this.currentDestination > floorIndex) && (direction == DirectionOfTravel.GOING_UP)) ||
				((this.currentDestination < floorIndex) && (direction == DirectionOfTravel.GOING_DOWN))
			)
		) {
			this.eventBus.post(
				TravelledThroughFloor.build[bldr|
					bldr.clockTime(this.clock.now())
					    .eventIndex(this.nextEventIndex++)
						.carIndex(this.carIndex)
						.floorIndex(floorIndex)
						.direction(direction)
				]
			);

			return ElevatorCar.TRAVELLED_THROUGH_FLOOR
		} else if(
			(this.state === ElevatorCar.SLOWING) &&
			(this.currentDestination === floorIndex) && 
			(this.currentDirection === direction)
		) {
			this.eventBus.post(
				SlowedForArrival.build[bldr|
					bldr.clockTime(this.clock.now())
					    .eventIndex(this.nextEventIndex++)
						.carIndex(this.carIndex)
						.floorIndex(floorIndex)
				]
			);

			return ElevatorCar.ARRIVED_AT_FLOOR
		}

		log.error("Stopping for panic after unexpected floor sensor trigger fired");
		return ElevatorCar.PANICKED	
	}
	

	@Transition(from="*", event=ElevatorCar.WEIGHT_UPDATED, to="*")
	def onWeightUpdated(double weightLoad) {
		this.eventBus.post(
			WeightLoadUpdated.build[bldr|
				bldr.clockTime(this.clock.now())
				    .eventIndex(this.nextEventIndex++)
					.carIndex(this.carIndex)
					.weightLoad(weightLoad)
			]
		);
	} 
	
	@Transition(from="*", event=ElevatorCar.PANICKED, to=ElevatorCar.SAFETY_LOCKOUT)
	def onSafetyPanic(String event) {
		log.error("Safety panic triggered!");
	}
	
	override cancelPickupRequest(int floorIndex, DirectionOfTravel direction) {
	}
	
	override acceptPickupRequest(int floorIndex, DirectionOfTravel direction) {
	}
	
	override dispatchToNextStop() {
		this.fsm.onEvent(this, ElevatorCar.DISPATCHED)
	}
	
//	override travelledThroughFloor(int floorIndex, DirectionOfTravel direction) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")

//	}
	
//	override doorStateChanging(DoorState newStatus) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}
	
	private def BitSet toBitSet(ImmutableList<Integer> list)
	{
		return new BitSet() => [bits | list.forEach[nextBit | bits.set(nextBit)]]
	}

}
