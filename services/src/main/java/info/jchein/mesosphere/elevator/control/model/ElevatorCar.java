package info.jchein.mesosphere.elevator.control.model;


import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.annotations.Transitions;
import org.statefulj.framework.core.model.StatefulFSM;
import org.statefulj.persistence.annotations.State;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.PendingDropOff;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.DriverBootstrapped;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.FloorSensorTriggered;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.SlowedForArrival;
import info.jchein.mesosphere.elevator.control.event.TravelledThroughFloor;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;


@StatefulController(
   value=ElevatorCar.BEAN_NAME,
   clazz = ElevatorCar.class,
   startState = ElevatorCar.BEFORE_ALLOCATION, 
   blockingStates = { ElevatorCar.BOARDING, ElevatorCar.HANDLING_CLOCK },
   noops = {
      @Transition(from=ElevatorCar.BEFORE_ALLOCATION, event=ElevatorCar.ALLOCATED, to=ElevatorCar.WAITING_FOR_DRIVER),
      @Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.LANDING_BRAKE_APPLIED, to=ElevatorCar.SLOWING),
      @Transition(from=ElevatorCar.TRAVELLING, event=ElevatorCar.TRAVELLED_THROUGH_FLOOR, to=ElevatorCar.TRAVELLING),
      @Transition(from=ElevatorCar.SLOWING, event=ElevatorCar.ARRIVED_AT_FLOOR, to=ElevatorCar.ARRIVING)
   }
)
@SuppressWarnings("all")
public class ElevatorCar
implements IElevatorCar, IElevatorCarPort
{
   private final static Logger log = LoggerFactory.getLogger(ElevatorCar.class);

   private final static AtomicInteger ID_SEQUENCE = new AtomicInteger(0);

   public final static String BEAN_NAME = "ElevatorCar";

   public final static String BEFORE_ALLOCATION = "StateBeforeAllocation";
   public final static String WAITING_FOR_DRIVER = "StateWaitingForDriver";
   public final static String AVAILABLE = "StateAvailable";
   public final static String TRAVELLING = "StateTraveling";
   public final static String SLOWING = "StateSlowing";
   public final static String ARRIVING = "StateAriving";
   public final static String BOARDING = "StateBoarding";
   public final static String SAFETY_LOCKOUT = "StateSafetyLockout";
   public final static String HANDLING_CLOCK = "StateHandlingClock";

   public final static String ALLOCATED = "EventAllocated";
   public final static String DRIVER_ATTACHED = "EventDriverSentBootstrap";
   public final static String PARKED = "EventParked";
   public final static String DISPATCHED = "EventDispatched";
   public final static String LANDING_BRAKE_APPLIED = "EventLandingBrakeApplied";
   public final static String TRIGGERED_FLOOR_SENSOR = "EventTriggeredFloorSensor";
   public final static String TRAVELLED_THROUGH_FLOOR = "EventTravelledThroughFloor";
   public final static String ARRIVED_AT_FLOOR = "EventArrivedAtFloor";
   public final static String STOPPED_AT_LANDING = "EventStoppedAtLanding";
   public final static String DOOR_CALL_RECEIVED = "EventDoorCallReceived";
   public final static String DOOR_CALL_COMPLETED = "EventDoorCallCompleted";
   public final static String DROPOFF_REQUESTED = "EventDropOffRequested";
   public final static String WEIGHT_UPDATED = "EventWeightUpdated";
   public final static String RESUME_AVAILABLE = "EventResumeAvailable";
   public final static String RESUME_TRAVELLING = "EventResumeTravelling";
   public final static String RESUME_BOARDING = "EventResumeTravelling";
   public final static String PANICKED = "EventPanicked";

   private static final String ACCEPTED_PICKUP_REQUEST = null;

   @State(accessorType = State.AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
   private String state;

   @FSM
   private StatefulFSM<ElevatorCar> fsm;

   private final int carIndex;
   private final int numFloors;
   private final IRuntimeClock clock;
   private final IRuntimeEventBus eventBus;

   private IElevatorCarDriver driver = null;

   private double currentWeightLoad = 0;
   private int currentFloorIndex = (-1);
   private BitSet currentDropRequests = null;
   private final BitSet pickupsGoingUp = new BitSet();
   private final BitSet pickupsGoingDown = new BitSet();

   private int reversePathFloorIndex = (-1);

   private DirectionOfTravel currentDirection;
   private int currentDestination;

   private DirectionOfTravel nextDirection;
   private int nextDestination;

   private long nextEventIndex = 1;


   @Autowired
   public ElevatorCar( @NotNull final IRuntimeClock clock, @NotNull final IRuntimeEventBus eventBus, @NotNull final DeploymentProperties bootstrapData )
   {
      this.carIndex = ElevatorCar.ID_SEQUENCE.incrementAndGet();
      this.numFloors =
         bootstrapData.getBuilding()
            .getNumFloors();
      this.clock = clock;
      this.eventBus = eventBus;
   }


   @PostConstruct
   public void init()
   {
      this.eventBus.registerListener(this);
   }


   public void attachDriver(final IElevatorCarDriver driver)
   {
      try {
         if ((this.driver != null)) { throw new RuntimeException("Driver has already been attached"); }
         final InitialCarState data = driver.initialize();
         this.fsm.onEvent(this, ElevatorCar.DRIVER_ATTACHED, driver, data);
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void dropOffRequested(final int floorIndex)
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.DROPOFF_REQUESTED, Integer.valueOf(floorIndex));
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void slowedForArrival()
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.LANDING_BRAKE_APPLIED);
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void parkedAtLanding()
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.STOPPED_AT_LANDING);
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void passengerDoorsClosed()
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.DOOR_CALL_COMPLETED);
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void updateWeightLoad(final double weightLoad)
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.WEIGHT_UPDATED, Double.valueOf(weightLoad));
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void confirmNextDispatch(int floorIndex)
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.DISPATCHED);
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   @Override
   public void cancelPickupRequest(final int floorIndex, final DirectionOfTravel direction)
   {}


   @Override
   public void acceptPickupRequest(final int floorIndex, final DirectionOfTravel direction)
   {
      try {
         this.fsm.onEvent(this, ElevatorCar.ACCEPTED_PICKUP_REQUEST, floorIndex, direction);
      }
      catch (Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }


   private BitSet toBitSet(final ImmutableList<Integer> list)
   {
      final BitSet bitSet = new BitSet();
      list.forEach(bitSet::set);
      return bitSet;
   }


   @Subscribe
   public void onFloorSensorTriggered(final FloorSensorTriggered event)
   {
      try {
         if (event.getCarIndex() == this.carIndex) {
            this.fsm.onEvent(
               this,
               ElevatorCar.TRIGGERED_FLOOR_SENSOR,
               event.getFloorIndex(),
               event.getDirection());
         }
      }
      catch (Throwable e) {
         throw Exceptions.sneakyThrow(e);
      }
   }


   @Transition(from=WAITING_FOR_DRIVER, event=DRIVER_ATTACHED, to=AVAILABLE)
   public void
   onBootstrap(final String event, final IElevatorCarDriver driver, final InitialCarState data)
   {
      final int initialFloor = data.getInitialFloor();
      final double initialWeight = data.passengers.stream().collect(Collectors.summingDouble((p) -> p.weight));
      final BitSet initialDropRequests = data.passengers.stream().<BitSet>collect(
         BitSet::new, (BitSet bitSet, PendingDropOff p) -> { bitSet.set(p.dropoffFloor); }, BitSet::or
      );

      this.currentDestination = -1;
      this.currentDirection = DirectionOfTravel.STOPPED;
      this.currentFloorIndex = initialFloor;
      this.currentWeightLoad = initialWeight;
      this.currentDropRequests = initialDropRequests;

      Preconditions.checkArgument(
         this.currentDropRequests.get(initialFloor) == false,
         "Initial drop requests may not include the starting floor");

      final int firstStopAbove = this.currentDropRequests.nextSetBit(initialFloor);
      final int firstStopBelow = this.currentDropRequests.previousSetBit(initialFloor);
      Preconditions.checkArgument(
         ((firstStopBelow == (-1)) || (firstStopAbove == (-1))),
         "Initial drop requests must all be above or all be below initial floor");

      if ((firstStopAbove > 0)) {
         this.nextDestination = firstStopAbove;
         this.nextDirection = DirectionOfTravel.GOING_UP;
         this.reversePathFloorIndex = this.currentDropRequests.previousSetBit((this.numFloors - 1));
      } else if ((firstStopBelow > 0)) {
         this.nextDestination = firstStopBelow;
         this.nextDirection = DirectionOfTravel.GOING_DOWN;
         this.reversePathFloorIndex = this.currentDropRequests.nextSetBit(0);
      } else {
         this.nextDestination = -1;
         this.nextDirection = DirectionOfTravel.STOPPED;
         this.reversePathFloorIndex = (-1);
      }
      this.eventBus.post(DriverBootstrapped.build((DriverBootstrapped.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex)
            .floorIndex(this.currentFloorIndex)
            .weightLoad(this.currentWeightLoad)
            .initialDirection(this.nextDirection)
            .dropRequests(((BitSet) this.currentDropRequests.clone()));
      }));
   }


   @Transitions({
      @Transition(from=AVAILABLE, event=DROPOFF_REQUESTED,
         to=AVAILABLE),
      @Transition(from=TRAVELLING, event=DROPOFF_REQUESTED,
         to=TRAVELLING),
      @Transition(from=SLOWING, event=DROPOFF_REQUESTED,
         to=SLOWING),
      @Transition(from=ARRIVING, event=DROPOFF_REQUESTED,
         to=ARRIVING),
      @Transition(from=BOARDING, event=DROPOFF_REQUESTED,
         to=BOARDING)
   })
   public void onDropOffRequested(final int floorIndex)
   {
      this.currentDropRequests.set(floorIndex);

      this.eventBus.post(DropOffRequested.build((DropOffRequested.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex)
            .dropOffFloorIndex(floorIndex);
      }));
   }


   @Transitions({
      @Transition(from=AVAILABLE, event=DISPATCHED,
         to=TRAVELLING),
      @Transition(from=BOARDING, event=DISPATCHED,
         to=TRAVELLING)
   })
   public void onDispatch(final String event)
   {
      this.currentDestination = this.nextDestination;
      if ((this.nextDestination == this.reversePathFloorIndex)) {
         if (this.currentDirection == DirectionOfTravel.GOING_UP) {
            final BitSet stopsGoingDown = (BitSet) this.currentDropRequests.clone();
            stopsGoingDown.or(this.pickupsGoingDown);
            stopsGoingDown.clear(this.reversePathFloorIndex);
            this.nextDestination = stopsGoingDown.previousSetBit(this.reversePathFloorIndex);

            // TODO!!!
            if ((this.nextDestination >= 0)) {}
            final int nextPickupDown =
               this.pickupsGoingDown.previousSetBit((this.reversePathFloorIndex - 1));
            final int firstPickupUp = this.pickupsGoingUp.nextSetBit(0);
            final int nextDropOff = this.currentDropRequests.previousSetBit(this.currentFloorIndex);
            final int lastDropOff = this.currentDropRequests.nextSetBit(0);
            int newNextDestination = Math.max(nextPickupDown, lastDropOff);
            int newReversePathIndex = firstPickupUp;
            if (((lastDropOff < 0) || (lastDropOff == this.reversePathFloorIndex))) {
               if ((newNextDestination < 0)) {
                  if ((firstPickupUp < 0)) { }
               }
            }
         }

         if (
            (this.currentDirection == DirectionOfTravel.GOING_UP) &&
            (this.currentDropRequests.get(this.reversePathFloorIndex) || this.pickupsGoingDown.get(this.reversePathFloorIndex))
         ) {
            this.nextDirection = DirectionOfTravel.GOING_DOWN;
            this.nextDestination = this.reversePathFloorIndex;
         } else if (
            (this.currentDirection == DirectionOfTravel.GOING_DOWN) &&
            (this.currentDropRequests.get(this.reversePathFloorIndex) || this.pickupsGoingDown.get(this.reversePathFloorIndex))
         ) {
            this.nextDirection = DirectionOfTravel.GOING_DOWN;
            this.nextDestination = this.reversePathFloorIndex;
         } else {
            this.nextDirection = DirectionOfTravel.STOPPED;
            this.nextDestination = (-1);
         }
      } else if (this.currentDirection == DirectionOfTravel.GOING_UP) {
         this.currentDestination = this.nextDestination;
         final int nextDrop = this.currentDropRequests.nextSetBit((this.currentDestination + 1));
         final int nextPickup = this.pickupsGoingUp.nextSetBit((this.currentDestination + 1));

         if (nextDrop > 0) {
            if (nextPickup > 0) {
               this.nextDestination = Math.min(nextDrop, nextPickup);
            } else {
               this.nextDestination = nextDrop;
            }
         } else if (nextPickup > 0) {
            this.nextDestination = nextPickup;
         }
      } else if (this.currentDirection == DirectionOfTravel.GOING_DOWN) {
         this.currentDestination = this.nextDestination;
         final int nextDrop = this.currentDropRequests.previousSetBit((this.currentDestination - 1));
         final int nextPickup = this.pickupsGoingUp.previousSetBit((this.currentDestination - 1));

         this.nextDestination = Math.max(nextDrop, nextPickup);
      }

      this.eventBus.post(DepartedLanding.build((DepartedLanding.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex)
            .origin(this.currentFloorIndex)
            .destination(this.currentDestination)
            .direction(this.currentDirection);
      }));
   }


   @Transition(from=ARRIVING, event=STOPPED_AT_LANDING)
   public String onStoppedAtLanding(final String event)
   {
      this.currentDestination = -1;
      this.currentDirection = DirectionOfTravel.STOPPED;

      if ((this.nextDirection != DirectionOfTravel.STOPPED) || this.currentDropRequests.get(this.currentFloorIndex)) {
         return ElevatorCar.DOOR_CALL_RECEIVED;
      } else {
         return ElevatorCar.PARKED;
      }
   }


   @Transitions({
      @Transition(from=ARRIVING, event=PARKED, to=AVAILABLE),
      @Transition(from=BOARDING, event=PARKED, to=AVAILABLE)
   })
   public void onParkedPendingDispatch(final String event)
   {
      this.eventBus.post(ParkedAtLanding.build((ParkedAtLanding.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex)
            .floorIndex(this.currentFloorIndex);
      }));
   }


   @Transitions({
      @Transition(from=ARRIVING, event=DOOR_CALL_RECEIVED, to=BOARDING),
      @Transition(from=AVAILABLE, event=DOOR_CALL_RECEIVED, to=BOARDING)
   })
   public void onAnsweredCall(final String event)
   {
      this.currentDropRequests.clear(this.currentFloorIndex);

      if (this.nextDirection == DirectionOfTravel.GOING_UP) {
         this.pickupsGoingUp.clear(this.currentFloorIndex);
      } else if (this.nextDirection == DirectionOfTravel.GOING_DOWN) {
         this.pickupsGoingDown.clear(this.currentFloorIndex);
      } else {
         throw new IllegalStateException("Cannot answer next all when there is no next call");
      }

      this.eventBus.post(PassengerDoorsOpened.build((PassengerDoorsOpened.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex)
            .floorIndex(this.currentFloorIndex)
            .direction(this.nextDirection);
      }));
   }


   @Transition(from=BOARDING, event=DOOR_CALL_COMPLETED)
   public String onBoardingCompleted(final String event)
   {
      this.eventBus.post(PassengerDoorsClosed.build((PassengerDoorsClosed.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex);
      }));

      if ((this.nextDestination < 0)) { return ElevatorCar.PARKED; }

      return ElevatorCar.DISPATCHED;
   }


   @Transitions({
      @Transition(from=SLOWING, event=TRIGGERED_FLOOR_SENSOR),
      @Transition(from=TRAVELLING, event=TRIGGERED_FLOOR_SENSOR)
   })
   public String
   onTriggeredFloorSensor(final String event, final int floorIndex, final DirectionOfTravel direction)
   {
      this.currentFloorIndex = floorIndex;
      if ((this.state == ElevatorCar.TRAVELLING) &&
         (this.currentDestination != floorIndex) && (this.currentDirection == direction) &&
         (((this.currentDestination > floorIndex) && (direction == DirectionOfTravel.GOING_UP)) ||
            ((this.currentDestination < floorIndex) && (direction == DirectionOfTravel.GOING_DOWN))))
      {
         this.eventBus.post(TravelledThroughFloor.build((TravelledThroughFloor.Builder bldr) -> {
            bldr.clockTime(this.clock.now())
               .carSequence(this.nextEventIndex++)
               .carIndex(this.carIndex)
               .floorIndex(floorIndex)
               .direction(direction);
         }));
         return TRAVELLED_THROUGH_FLOOR;
      } else if ((this.state == ElevatorCar.SLOWING) &&
         (this.currentDestination == floorIndex) && (this.currentDirection == direction))
      {
         this.eventBus.post(SlowedForArrival.build((SlowedForArrival.Builder bldr) -> {
            bldr.clockTime(this.clock.now())
               .carSequence(this.nextEventIndex++)
               .carIndex(this.carIndex);
         }));
         return ARRIVED_AT_FLOOR;
      }

      ElevatorCar.log.error("Stopping for panic after unexpected floor sensor trigger fired");
      return PANICKED;
   }


   @Transition(from = "*", event=WEIGHT_UPDATED, to = "*")
   public void onWeightUpdated(final double weightLoad)
   {
      this.eventBus.post(WeightLoadUpdated.build((WeightLoadUpdated.Builder bldr) -> {
         bldr.clockTime(this.clock.now())
            .carSequence(this.nextEventIndex++)
            .carIndex(this.carIndex)
            .weightLoad(weightLoad);
      }));
   }


   @Transition(from = "*", event=PANICKED, to=SAFETY_LOCKOUT)
   public void onSafetyPanic(final String event)
   {
      ElevatorCar.log.error("Safety panic triggered!");
   }


   @Pure
   String getState()
   {
      return this.state;
   }


   void setState(final String state)
   {
      this.state = state;
   }
}
