package info.jchein.mesosphere.elevator.domain.model

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import de.oehme.xtend.contrib.logging.slf4j.Slf4j
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.domain.car.event.DepartedStation
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.car.event.ReadyForDeparture
import info.jchein.mesosphere.elevator.domain.car.event.SlowedForArrival
import info.jchein.mesosphere.elevator.domain.car.event.TravelledThroughFloor
import info.jchein.mesosphere.elevator.domain.car.event.WeightLoadUpdated
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot
import info.jchein.mesosphere.elevator.domain.^dispatch.event.StopItineraryUpdated
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService
import java.util.BitSet
import java.util.Queue
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import javax.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.statefulj.framework.core.annotations.FSM
import org.statefulj.framework.core.annotations.StatefulController
import org.statefulj.framework.core.annotations.Transition
import org.statefulj.framework.core.model.StatefulFSM
import org.statefulj.persistence.annotations.State
import org.statefulj.persistence.annotations.State.AccessorType
import rx.Subscription
import org.eclipse.xtend.lib.annotations.Accessors
import org.statefulj.framework.core.annotations.Transitions
import info.jchein.mesosphere.elevator.domain.car.event.ParkedAtLanding
import info.jchein.mesosphere.elevator.domain.car.event.PassengerDoorsOpened
import info.jchein.mesosphere.elevator.domain.car.event.PassengerDoorsClosed
import com.google.common.base.Preconditions
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties

@StatefulController(
	value=ElevatorCar.BEAN_NAME,
	clazz = typeof(ElevatorCar),
	startState = ElevatorCar.BEFORE_ALLOCATION,
	blockingStates = #[ElevatorCar.BOARDING],
    noops = #[
    		@Transition(from=ElevatorCar.BEFORE_ALLOCATION, event=ElevatorCar.ALLOCATED, to=ElevatorCar.WAITING_FOR_DRIVER),
    		@Transition(from=ElevatorCar.WAITING_FOR_DRIVER, event=ElevatorCar.DRIVER_ATTACHED, to=ElevatorCar.PARKED),
    		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.LANDING_BRAKE_APPLIED, to=ElevatorCar.LANDING),
    		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.TRAVELLED_THROUGH_FLOOR, to=ElevatorCar.TRAVELLING),
    		@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.ARRIVED_AT_FLOOR, to=ElevatorCar.LANDING),
    		@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.OPENED_DOORS, to=ElevatorCar.BOARDING),
    		
	]
)
@Slf4j
class ElevatorCar implements IElevatorCar, IElevatorCarPort {
	// private static val Logger LOG = LoggerFactory.getLogger(typeof(ElevatorCar))
	
	private static val AtomicInteger ID_SEQUENCE = new AtomicInteger(0);

	public static val String BEAN_NAME = "ElevatorCar"
	
	public static val String BEFORE_ALLOCATION = "StateBeforeAllocation";
	public static val String WAITING_FOR_DRIVER = "StateWaitingForDriver";
	public static val String PARKED = "StateParked";
	public static val String TRAVELLING = "StateTraveling";
	public static val String LANDING = "StateLanding"
	public static val String BOARDING = "StateBoarding"
	public static val String SAFETY_LOCKOUT = "StateSafetyLockout"
	
	public static val String ALLOCATED = "EventAllocated";
	public static val String DRIVER_ATTACHED = "EventDriverSentBootstrap";
	public static val String PREPARED_FOR_DISPATCH = "EventPreparedForDispatch";
	public static val String DISPATCHED = "EventDispatched";
	public static val String LANDING_BRAKE_APPLIED = "EventLandingBrakeApplied"
	public static val String TRIGGERED_FLOOR_SENSOR = "EventTriggeredFloorSensor"
	public static val String TRAVELLED_THROUGH_FLOOR = "EventTravelledThroughFloor"
	public static val String ARRIVED_AT_FLOOR = "EventArrivedAtFloor"
	public static val String PARKED_AT_LANDING = "EventParkedAtLanding"
	public static val String OPENED_DOORS = "EventOpenedDoors"
	public static val String CLOSED_DOORS = "EventClosedDoors"
	public static val String DROPOFF_REQUESTED = "EventDropOffRequested"
	public static val String WEIGHT_UPDATED = "EventWeightUpdated"
	public static val String PANICKED = "EventPanicked"
	

	@State(accessorType = AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
	@Accessors(#[
		org.eclipse.xtend.lib.annotations.AccessorType.PACKAGE_GETTER,
		org.eclipse.xtend.lib.annotations.AccessorType.PACKAGE_SETTER
	])
	private var String state;
	
	@FSM
	var StatefulFSM<ElevatorCar> fsm;
	
	val int carIndex
	val IClock systemClock
	val EventBus eventBus
	val IElevatorPhysicsService physicsService
	val BuildingProperties bldgProps
	
	val BitSet pickupsGoingUp = new BitSet()
	val BitSet pickupsGoingDown = new BitSet()
	var int reversePathFloorIndex = -1

	IElevatorCarDriver driver = null
	BitSet currentDropRequests = null
	double currentWeightLoad = 0
	int currentFloorIndex = -1
	int currentDestination
	
	Subscription schedulerPlanSubscription
	
	DirectionOfTravel currentDirection
	
	int nextDestination
	
	
	@Autowired
	new(
		@NotNull IClock systemClock, @NotNull EventBus eventBus,
		@NotNull IElevatorPhysicsService physicsService, @NotNull BuildingProperties bldgProps
	) {
		this.carIndex = ID_SEQUENCE.incrementAndGet();
		this.physicsService = physicsService
		this.systemClock = systemClock;
		this.eventBus = eventBus
		this.bldgProps = bldgProps
	}
	
	@PostConstruct
	def init() {
		this.eventBus.register(this);
	}

	def void attachDriver(IElevatorCarDriver driver)
	{
		if (this.driver !== null) {
			throw new RuntimeException("Driver has already been attached")
		}
		
		driver.bootstrap[int floorHeight, double weightLoad, BitSet dropRequests|
			this.currentDestination = -1
			this.currentFloorIndex = floorHeight
			this.currentWeightLoad = weightLoad
			this.currentDropRequests = dropRequests.clone() as BitSet
			
			Preconditions.checkArgument(
				this.currentDropRequests.get(floorHeight) == false, 
				"Initial drop requests may not include the starting floor")

			val firstStopAbove = this.currentDropRequests.nextSetBit(floorHeight)
			val firstStopBelow = this.currentDropRequests.previousSetBit(floorHeight)

			if (firstStopAbove > 0) {
				Preconditions.checkArgument(
					firstStopBelow == -1,
					"Initial drop requests must all be above or all be below initial floor"
				)

				this.currentDirection = DirectionOfTravel.GOING_UP
				this.currentDestination = -1
				this.nextDestination = firstStopAbove
				this.reversePathFloorIndex = 
					this.currentDropRequests.previousSetBit(
						this.bldgProps.numFloors - 1)
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
/*
			val driverFeed = Observable.create(SyncOnSubscribe.<ItineraryPublisher, StopItineraryUpdated>createStateful([
				val Queue<StopItineraryUpdated> queue = new LinkedList<StopItineraryUpdated>();
				val eventHandler = new ItineraryPublisher(queue);
				this.eventBus.register(eventHandler);
				return eventHandler;
			], [eventHandler, observer | 
				val StopItineraryUpdated nextPlan = eventHandler.getQueue().poll();
				if (nextPlan !== null) {
					observer.onNext(nextPlan);
				}
				return eventHandler;
			], [ItineraryPublisher eventHandler| 
				this.eventBus.unregister(eventHandler);
			]));
		
			// TODO: What to do with the subscription returned?
			this.schedulerPlanSubscription = driverFeed.subscribe(driver.scheduleObserver);
*/

			// This final event carries no state, but signals that the stream of floor request inforation is done.
			this.eventBus.post(
				DriverBootstrapped.build[bldr|
					bldr.clockTime(this.systemClock.now())
						.carIndex(this.carIndex)
						.dispatchTarget(this)
						.floorIndex(this.currentFloorIndex)
						.weightLoad(this.currentWeightLoad)
						.initialDirection(this.currentDirection)
						.dropRequests(this.currentDropRequests.clone() as BitSet)
				]
			)

			this.fsm.onEvent(this, ElevatorCar.DRIVER_ATTACHED, driver);
		]
	}
	
	/**
	 * Helper class to honor Reactive Stream contract requiring no more than one value sent to onNext() per call to
	 * the handler passed to create.  We do not control the rate of arrival, so must 
	 */
	static class ItineraryPublisher {
		private val Queue<StopItineraryUpdated> queue;

		new(Queue<StopItineraryUpdated> queue) {
			this.queue = queue;
		}

		@Subscribe
		def void receiveItineraryUpdate(StopItineraryUpdated event) {
			this.queue.offer(event);
		}
		
		def Queue<StopItineraryUpdated> getQueue() {
			return this.queue;
		}
	}
	
	def ElevatorCarSnapshot getSnapshot() {
		return ElevatorCarSnapshot.build[bldr|
			bldr.clockTime(this.systemClock.now())
			.carIndex(this.carIndex)
		]
	}

	override dropOffRequested(int floorIndex) {
		this.fsm.onEvent(this, ElevatorCar.DROPOFF_REQUESTED, floorIndex);
	}

//	override readyForDeparture() {
//		if (this.state.equals(ElevatorCar.BOARDING)) {
//			this.fsm.onEvent(this, ElevatorCar.CLOSED_DOORS)
//		} else {
//			this.fsm.onEvent(this, ElevatorCar.PARKED_AT_LANDING)
//		}
//	}

    override slowedForArrival() {
 		this.fsm.onEvent(this, ElevatorCar.LANDING_BRAKE_APPLIED)   	
    }

	override parkedAtLanding() {
		this.fsm.onEvent(this, ElevatorCar.PARKED_AT_LANDING)
	}
	
	override passengerDoorsClosed() {
		this.fsm.onEvent(this, ElevatorCar.CLOSED_DOORS)
	}

//	override updateLocation(double floorHeight, boolean isBraking) {
//		this.fsm.onEvent(this, ElevatorCar.LOCATION_UPDATED, floorHeight)
//	}
	
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

	@Transitions(#[
		@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.TRAVELLING),
		@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.LANDING),
		@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.DROPOFF_REQUESTED, to=ElevatorCar.BOARDING)
	])
	def onDropOffRequested(int floorIndex) {
		this.currentDropRequests.set(floorIndex)
		switch (this.currentDirection) {
			case GOING_UP: {
				
			}
			case GOING_DOWN: {
				
			}
			case STOPPED: {
				
			}
		}
	
		this.eventBus.post(
			DropOffRequested.build[
				it.clockTime(this.systemClock.now())
				.carIndex(this.carIndex)
				.dropOffFloorIndex(floorIndex)
			]
		)
	}
	

	@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.CLOSED_DOORS, to=ElevatorCar.PARKED)
	def onReadyForDeparture(String event) {
		this.eventBus.post(
			ReadyForDeparture.build[ bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(floorIndex)
					.clockTime(this.systemClock.now())
			]
		);
	}
	
	@Transition(from=ElevatorCar.PARKED, event=ElevatorCar.DISPATCHED, to=ElevatorCar.TRAVELLING)
	def void onDispatch(String event) {
		if (this.nextDestination == this.reversePathFloorIndex) {
			// No need to consider STOPPED here by definition
			if (this.currentDirection == DirectionOfTravel.GOING_UP) {
				if (this.currentDestination > 0) {
					var nextDrop = this.currentDropRequests.previousSetBit(this.currentDestination - 1)
					if (nextDrop >= 0) {
						this.currentDestination = this.nextDestination
						this.nextDirection = DirectionOfTravel.GOING_DOWN
						this.nextDestination = nextDrop
					} else {
						nextDrop = this.pickupsGoingDown.previousSetBit(this.currentDestination - 1)
						if (nextDrop >= 0) {
							this.currentDestination = this.nextDestination
							this.nextDirection = DirectionOfTravel.GOING_DOWN
							this.nextDestination = nextDrop
						} else {
							this.currentDestination = this.nextDestination
							this.nextDirection = DirectionOfTravel.STOPPED
							this.nextDestination = -1
						}
					}
				} else {
					this.currentDestination = this.nextDestination
					this.nextDirection = DirectionOfTravel.STOPPED
					this.nextDestination = -1
				}
			} else {
				if (this.currentDestination < (this.bldgProps.numFloors - 1)) {
					var nextDrop = this.currentDropRequests.nextSetBit(this.currentDestination + 1)
					if (nextDrop >= 0) {
						this.currentDestination = this.nextDestination
						this.nextDirection = DirectionOfTravel.GOING_UP
						this.nextDestination = nextDrop
					} else {
						nextDrop = this.pickupsGoingUp.nextSetBit(this.currentDestination + 1)
						if (nextDrop >= 0) {
							this.currentDestination = this.nextDestination
							this.nextDirection = DirectionOfTravel.GOING_UP
							this.nextDestination = nextDrop
						} else {
							this.currentDestination = this.nextDestination
							this.nextDirection = DirectionOfTravel.STOPPED
							this.nextDestination = -1
						}
					}
				} else {
					this.currentDestination = this.nextDestination
					this.nextDirection = DirectionOfTravel.STOPPED
					this.nextDestination = -1
				}
			}
		} else if (this.currentDirection == DirectionOfTravel.GOING_UP) {
			this.currentDestination = this.nextDestination
			var nextDrop = this.currentDropRequests.nextSetBit(this.currentDestination + 1)
			var nextPickup = this.pickups
		} else {
			this.currentDestination = this.nextDestination
			this.nextDestination = this.currentDropRequests.previousSetBit(this.currentDestination - 1)
		}

		this.eventBus.post(
			DepartedStation.build[ bldr |
				bldr.carIndex(this.carIndex)
				.origin(this.currentFloorIndex)	
				.destination(this.currentDestination)
				.direction(this.currentDirection)
				.clockTime(this.systemClock.now())
			]
		)
	}
	

	@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.PARKED_AT_LANDING)
	def onParkedAtLanding(String event)
	{
		this.eventBus.post(
			ParkedAtLanding.build[bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(this.currentFloorIndex)
					.direction(this.currentDirection)
					.clockTime(this.systemClock.now())
			]
		);

		this.currentDestination = -1
		this.currentDirection = 
			if (this.currentDirection == DirectionOfTravel.GOING_UP) {
				var nextDirection = this.checkNextDirectionAfterGoingUp()
				if (nextDirection == DirectionOfTravel.STOPPED) {
					nextDirection = this.checkNextDirectionAfterGoingDown()
					if (nextDirection == DirectionOfTravel.STOPPED) {
						DirectionOfTravel.STOPPED
					} else {
						DirectionOfTravel.GOING_DOWN
					}
				} else {
					DirectionOfTravel.GOING_UP
				} 
			} else if (this.currentDirection == DirectionOfTravel.GOING_DOWN) {
				var nextDirection = this.checkNextDirectionAfterGoingDown()
				if (nextDirection == DirectionOfTravel.STOPPED) {
					nextDirection = this.checkNextDirectionAfterGoingUp()
					if (nextDirection == DirectionOfTravel.STOPPED) {
						DirectionOfTravel.STOPPED
					} else {
						DirectionOfTravel.GOING_UP
					}
				} else {
					DirectionOfTravel.GOING_DOWN
				} 
			} else {
				DirectionOfTravel.STOPPED
			}

		if ((this.currentDirection == DirectionOfTravel.STOPPED) &&
			(! this.currentDropRequests.get(this.currentFloorIndex))) {
			return ElevatorCar.PREPARED_FOR_DISPATCH
		} else {
			return ElevatorCar.OPENED_DOORS
		}
	}

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
	
	@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.PREPARED_FOR_DISPATCH, to=ElevatorCar.PARKED)
	def onReadyForDispatch(String event)
	{
		this.eventBus.post(
			ReadyForDeparture.build[bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(this.currentFloorIndex)
					.clockTime(this.systemClock.now())
			]
		);
	}
			
	@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.OPENED_DOORS, to=ElevatorCar.BOARDING)
	def onAnsweredCall(String event)
	{
		this.currentDropRequests.clear(this.currentFloorIndex)
		if (this.currentDirection == DirectionOfTravel.GOING_UP) {
			this.pickupsGoingUp.clear(this.currentFloorIndex)
		} else if(this.currentDirection == DirectionOfTravel.GOING_DOWN) {
			this.pickupsGoingDown.clear(this.currentFloorIndex)
		} else {
			throw new IllegalStateException("Cannot answer a call while planning to travel in the STOPPED direction");
		}

		this.eventBus.post(
			PassengerDoorsOpened.build[bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(this.currentFloorIndex)
					.direction(this.currentDirection)
					.clockTime(this.systemClock.now())
			]
		)
	}
			
	@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.CLOSED_DOORS, to=ElevatorCar.PARKED)
	def onBoardingCompleted(String event)
	{
		this.eventBus.post(
			PassengerDoorsClosed.build[bldr |
				bldr.carIndex(this.carIndex)
					.clockTime(this.systemClock.now())
			]
		)
	}

	
	@Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR)
	def String onTriggeredFloorSensor(String event, int floorIndex, DirectionOfTravel direction) 
	{
		if (
			(this.currentDestination !== floorIndex) && (this.currentDirection == direction) && (
				((this.currentDestination < floorIndex) && (direction == DirectionOfTravel.GOING_UP)) ||
				((this.currentDestination > floorIndex) && (direction == DirectionOfTravel.GOING_DOWN))
			)
		) {
			this.currentFloorIndex = floorIndex

			this.eventBus.post(
				TravelledThroughFloor.build[bldr|
					bldr.clockTime(this.systemClock.now())
					.carIndex(this.carIndex)
					.floorIndex(floorIndex)
					.direction(direction)
				]
			);

			return ElevatorCar.TRAVELLED_THROUGH_FLOOR
		}

		return ElevatorCar.PANICKED	
	}
	
	@Transition(from=ElevatorCar.LANDING, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR)
	def String onTriggeredFloorSensorWithBrake(String event, int floorIndex, DirectionOfTravel direction)
	{
		if (this.currentDestination == floorIndex && this.currentDirection == direction) {
			this.currentFloorIndex = floorIndex
			
			this.eventBus.post(
				SlowedForArrival.build[bldr|
					bldr.clockTime(this.systemClock.now())
					.carIndex(this.carIndex)
					.floorIndex(floorIndex)
				]
			);

			return ElevatorCar.ARRIVED_AT_FLOOR
		}

		return ElevatorCar.PANICKED	
	}

	@Transition(from="*", event=ElevatorCar.WEIGHT_UPDATED, to="*")
	def onWeightUpdated(double weightLoad) {
		this.eventBus.post(
			WeightLoadUpdated.build[bldr|
				bldr.clockTime(this.systemClock.now())
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
	
	override dispatchNextDestination() {
		this.
	}
	
//	override travelledThroughFloor(int floorIndex, DirectionOfTravel direction) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")

//	}
	
//	override doorStateChanging(DoorState newStatus) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
//	}

}
