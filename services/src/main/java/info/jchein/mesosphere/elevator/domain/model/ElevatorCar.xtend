package info.jchein.mesosphere.elevator.domain.model

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.domain.car.event.DriverBootstrapped
import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested
import info.jchein.mesosphere.elevator.domain.car.event.LocationUpdated
import info.jchein.mesosphere.elevator.domain.car.event.ParkedForBoarding
import info.jchein.mesosphere.elevator.domain.car.event.ReadyForDeparture
import info.jchein.mesosphere.elevator.domain.car.event.WeightLoadUpdated
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot
import info.jchein.mesosphere.elevator.domain.^dispatch.event.StopItineraryUpdated
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService
import java.util.BitSet
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import javax.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.statefulj.framework.core.annotations.FSM
import org.statefulj.framework.core.annotations.StatefulController
import org.statefulj.framework.core.annotations.Transition
import org.statefulj.framework.core.annotations.Transitions
import org.statefulj.framework.core.model.StatefulFSM
import org.statefulj.persistence.annotations.State
import org.statefulj.persistence.annotations.State.AccessorType
import rx.Observable
import rx.Observer
import rx.observables.SyncOnSubscribe

@StatefulController(
	value=ElevatorCar.BEAN_NAME,
	clazz = typeof(ElevatorCar),
	startState = ElevatorCar.BOOTSTRAPPING,
	blockingStates = #[ElevatorCar.BOARDING],
    noops = #[
//    		@Transition(from=ElevatorCar.BOOTSTRAPPING, event=ElevatorCar.DRIVER_SENT_BOOTSTRAP, to=ElevatorCar.READY_FOR_DESTINATION),
//    		@Transition(from=ElevatorCar.ASCENDING_FAST, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR, to=ElevatorCar.ASCENDING_FAST),
//    		@Transition(from=ElevatorCar.DESCENDING_FAST, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR, to=ElevatorCar.DESCENDING_FAST),
//    		@Transition(from=ElevatorCar.ASCENDING_SLOW, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR, to=ElevatorCar.ASCENDING_SLOW),
//    		@Transition(from=ElevatorCar.DESCENDING_SLOW, event=ElevatorCar.TRIGGERED_FLOOR_SENSOR, to=ElevatorCar.DESCENDING_SLOW)
	]
)
class ElevatorCar implements IElevatorCar, IElevatorCarPort {
	private static final AtomicInteger ID_SEQUENCE = new AtomicInteger(0);

	public static final String BEAN_NAME = "ElevatorCar"
	
	public static final String BOOTSTRAPPING = "StateBootstrapping";
	public static final String WAITING_TO_START = "StateWaitingToStart";
	public static final String READY_FOR_DESTINATION = "StateAwaitingDestination";
	public static final String ASCENDING_FAST = "StateAscendingFast";
	public static final String DESCENDING_FAST = "StateDescendingFast";
	public static final String ASCENDING_SLOW = "StateAscendingSlow";
	public static final String DESCENDING_SLOW = "StateDescendingSlow";
	public static final String BOARDING = "StateBoarding"
//	public static final String LANDING = "StateLanding"
//	public static final String PARKED = "StateParked";
	
	public static final String DRIVER_SENT_BOOTSTRAP = "EventDriverSentBootstrap";
	public static final String DISPATCHED = "EventDispatched";
	public static final String DISPATCHED_UP_FAST = "EventDispatchedUpFast";
	public static final String DISPATCHED_DOWN_FAST = "EventDispatchedDownFast";
	public static final String DISPATCHED_UP_SLOW = "EventDispatchedUpSlow";
	public static final String DISPATCHED_DOWN_SLOW = "EventDispatchedDownSlow";
//	public static final String TRIGGERED_FLOOR_SENSOR = "EventTriggeredFloorSensor"
//	public static final String TRAVELLED_THROUGH_FLOOR = "EventTravelledThroughFloor"
//	public static final String BRAKES_APPLIED = "EventBrakesApplied"
//	public static final String OPENED_DOORS = "EventOpenedDoors"
//	public static final String CLOSED_DOORS = "EventClosedDoors"
	public static final String LANDED = "EventLanded"
	public static final String RETURNED_TO_SERVICE = "EventReturnedToService"
	public static final String DROPOFF_REQUESTED = "EventDropOffRequested"
	public static final String LOCATION_UPDATED = "EventLocationUpdated"
	public static final String WEIGHT_UPDATED = "EventWeightUpdated"
	

	@State(accessorType = AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
	private var String state;
	
	@FSM
	var StatefulFSM<ElevatorCar> fsm;
	
	final IClock systemClock
	final EventBus eventBus
	final int carIndex

	Observer<StopItineraryUpdated> driver = null
	int currentFloorIndex
	
	IElevatorPhysicsService physicsService
	
	@Autowired
	new(@NotNull IClock systemClock, @NotNull EventBus eventBus, @NotNull IElevatorPhysicsService physicsService ) {
		this.carIndex = ID_SEQUENCE.incrementAndGet();
		this.physicsService = physicsService
		this.systemClock = systemClock;
		this.eventBus = eventBus
	}
	
	@PostConstruct
	def init() {
		this.eventBus.register(this);
	}

	def Observable<ElevatorCarSnapshot> attachDriver(Observer<StopItineraryUpdated> driver) {
		if (this.driver !== null) {
			throw new RuntimeException("Driver has already been attached")
		}
		
		val driverFeed = Observable.create(SyncOnSubscribe.<ItineraryPublisher, StopItineraryUpdated>createStateful(
		[   val LinkedList<StopItineraryUpdated> queue = new LinkedList<StopItineraryUpdated>();
			val eventHandler = new ItineraryPublisher(queue);
			this.eventBus.register(eventHandler);
			return eventHandler;
		], [queue, observer | 
			observer.onNext(queue.getQueue().removeFirst());
			return queue;
		], [ItineraryPublisher queue| 
			this.eventBus.unregister(queue);
		]))/*.subscribeOn(this.systemScheduler)*/.share();
		
		// TODO: What to do with the subscription returned?
		driverFeed.subscribe(driver);
		
		return driverFeed.map[dispatchUpdate| this.getSnapshot()]
	}
	
	
	/**
	 * Helper class to honor Reactive Stream contract requiring no more than one value sent to onNext() per call to
	 * the handler passed to create.  We do not control the rate of arrival, so must 
	 */
	static class ItineraryPublisher {
		private val LinkedList<StopItineraryUpdated> queue;

		new(LinkedList<StopItineraryUpdated> queue) {
			this.queue = queue;
		}

		@Subscribe
		def void receiveItineraryUpdate(StopItineraryUpdated event) {
			this.queue.offer(event);
		}
		
		def LinkedList<StopItineraryUpdated> getQueue() {
			return this.queue;
		}
	}
	
	def ElevatorCarSnapshot getSnapshot() {
		return ElevatorCarSnapshot.build[bldr|
			bldr.clockTime(this.systemClock.now())
			.carIndex(this.carIndex)
		]
	}

//	override bootstrapStatus(double floorHeight, double weightLoad, BitSet dropRequests) {
//		this.fsm.onEvent(this, ElevatorCar.DRIVER_SENT_BOOTSTRAP, floorHeight, weightLoad, dropRequests);
//	}
	
	override dropOffRequested(int floorIndex) {
		this.fsm.onEvent(this, ElevatorCar.DROPOFF_REQUESTED, floorIndex);
	}

	override readyForDeparture() {
		this.fsm.onEvent(this, ElevatorCar.READY_FOR_DESTINATION)
	}

	override landedAtFloor(int floorIndex) {
		this.fsm.onEvent(this, ElevatorCar.LANDED, floorIndex);
	}

	override updateLocation(double floorHeight, boolean isBraking) {
		this.fsm.onEvent(this, ElevatorCar.LOCATION_UPDATED, floorHeight)
	}
	
	override updateWeightLoad(double weightLoad) {
		this.fsm.onEvent(this, ElevatorCar.WEIGHT_UPDATED, weightLoad)
	}

	@Transition(from=ElevatorCar.BOOTSTRAPPING, event=ElevatorCar.DRIVER_SENT_BOOTSTRAP, to=ElevatorCar.READY_FOR_DESTINATION)
	def void onBootstrapStatus(double floorHeight, double weightLoad, BitSet dropRequests) {
		val long now = this.systemClock.now();
		
		this.eventBus.post(
			LocationUpdated.build[bldr|
				bldr.clockTime(now)
				.carIndex(this.carIndex)
				.floorHeight(floorHeight)
			]
		);
		this.eventBus.post(
			WeightLoadUpdated.build[bldr|
				bldr.clockTime(now)
				.carIndex(this.carIndex)
				.weightLoad(weightLoad)
			]
		);
		for (long requestedFloor : dropRequests.toLongArray() ) {
			this.eventBus.post(
				DropOffRequested.build[bldr|
					bldr.clockTime(now)
					.carIndex(this.carIndex)
					.dropOffFloorIndex(requestedFloor as int)
				]
			);
		}
		
		// This final event carries no state, but signals that the stream of floor request inforation is done.
		this.eventBus.post(
			DriverBootstrapped.build[bldr|
				bldr.clockTime(now).carIndex(this.carIndex)
			]
		)
	}
	
	@Transition(from="*", event=ElevatorCar.DROPOFF_REQUESTED, to="*")
	def onDropOffRequested(int floorIndex) {
		this.eventBus.post(
			DropOffRequested.build[
				it.clockTime(this.systemClock.now())
				.carIndex(this.carIndex)
				.dropOffFloorIndex(floorIndex)
			]
		)
	}

	@Transitions(
		@Transition(from=ElevatorCar.BOARDING, event=ElevatorCar.RETURNED_TO_SERVICE, to=ElevatorCar.READY_FOR_DESTINATION)
	)
	def onReadyForDeparture(int floorIndex) {
		this.eventBus.post(
			ReadyForDeparture.build[ bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(floorIndex)
					.clockTime(this.systemClock.now())
			]
		);
	}
	
	@Transition(from=ElevatorCar.READY_FOR_DESTINATION, event=ElevatorCar.DISPATCHED)
	def String onDispatch(int toFloorIndex) {
		if (toFloorIndex > this.currentFloorIndex) {
			if (this.physicsService.isTravelFast(this.currentFloorIndex, toFloorIndex)) {
				return ElevatorCar.DISPATCHED_UP_FAST
			} else {
				return ElevatorCar.DISPATCHED_UP_SLOW
			}
		} else if (toFloorIndex < this.currentFloorIndex) {
			if (this.physicsService.isTravelFast(this.currentFloorIndex, toFloorIndex)) {
				return ElevatorCar.DISPATCHED_DOWN_FAST
			} else {
				return ElevatorCar.DISPATCHED_DOWN_SLOW
			}
		}
	}
	
	@Transitions(
		@Transition(from=ElevatorCar.ASCENDING_FAST, event=ElevatorCar.LANDED, to=ElevatorCar.BOARDING),
		@Transition(from=ElevatorCar.DESCENDING_FAST, event=ElevatorCar.LANDED, to=ElevatorCar.BOARDING),
		@Transition(from=ElevatorCar.ASCENDING_SLOW, event=ElevatorCar.LANDED, to=ElevatorCar.BOARDING),
		@Transition(from=ElevatorCar.DESCENDING_SLOW, event=ElevatorCar.LANDED, to=ElevatorCar.BOARDING)
	)
	def onLandedAtFloor(int floorIndex) {
		this.eventBus.post(
			ParkedForBoarding.build[ bldr |
				bldr.carIndex(this.carIndex)
					.floorIndex(floorIndex)
					.clockTime(this.systemClock.now())
			]
		);
	}
	
	@Transition(from="*", event=ElevatorCar.LOCATION_UPDATED, to="*")
	def onLocationUpdated(double floorHeight) {
		this.eventBus.post(
			LocationUpdated.build[bldr|
				bldr.clockTime(this.systemClock.now())
				.carIndex(this.carIndex)
				.floorHeight(floorHeight)
			]
		);
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
	
	override pollForClock() {
//		ElevatorCarSnapshot state this.driver.pollForService(this.itinerary);
	}
	
	override cancelPickupRequest(int floorIndex, DirectionOfTravel direction) {
//		this.driver.cancelQueuedPickup(floorIndex, direction);
	}
	
	override enqueuePickupRequest(int floorIndex, DirectionOfTravel direction) {
//		this.driver.queueForPickup(floorIndex, direction);
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
