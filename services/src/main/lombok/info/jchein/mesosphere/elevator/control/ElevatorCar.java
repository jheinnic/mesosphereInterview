package info.jchein.mesosphere.elevator.control;


import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.ACCEPTED_PICKUP_REQUEST;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.ALLOCATED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.ANSWERED_CALL;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.ARRIVED_AT_FLOOR;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DISPATCHED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DOOR_ACTIVATED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DOOR_CLOSED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DOOR_OPENED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DRIVER_INITIALIZED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DROPOFF_REQUESTED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.DROPPED_PICKUP_REQUEST;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.LANDING_BRAKE_APPLIED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.PANICKED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.PARKED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.STOPPED_AT_LANDING;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.TRAVELLED_THROUGH_FLOOR;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.TRIGGERED_FLOOR_SENSOR;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.Events.WEIGHT_UPDATED;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.AVAILABLE;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.BEFORE_ALLOCATION;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.BOARDING;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.CLOSING_DOORS;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.HANDLING_CLOCK;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.LANDING;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.OPENING_DOORS;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.SAFETY_LOCKOUT;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.SLOWING;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.TRAVELLING;
import static info.jchein.mesosphere.elevator.control.IElevatorCar.States.WAITING_FOR_DRIVER;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.annotations.Transitions;
import org.statefulj.framework.core.model.StatefulFSM;
import org.statefulj.persistence.annotations.State;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.ICarContext;
import info.jchein.mesosphere.elevator.common.bootstrap.BuildingDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.DoorTimeDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.WeightDescription;
import info.jchein.mesosphere.elevator.common.graph.TravelPathStageNodes;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.elevator.common.physics.JourneyArcMomentSeries;
import info.jchein.mesosphere.elevator.common.physics.PathMoment;
import info.jchein.mesosphere.elevator.common.physics.TravelPathDownComparator;
import info.jchein.mesosphere.elevator.common.physics.TravelPathUpComparator;
import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.DriverBootstrapped;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.SlowedForArrival;
import info.jchein.mesosphere.elevator.control.event.TravelledPastFloor;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.manifest.ITravelGraph;
import info.jchein.mesosphere.elevator.control.manifest.PassengersOrigin;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledDrop;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledPickup;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledStop;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Scope(IElevatorCarScope.SCOPE_NAME)
@StatefulController(value = ElevatorCar.BEAN_NAME, clazz = ElevatorCar.class,
   factoryId = IElevatorCar.FACTORY_BEAN_NAME, startState = BEFORE_ALLOCATION, blockingStates =
   {
      BOARDING, HANDLING_CLOCK
   }, noops = {
      @Transition(from = TRAVELLING, event = LANDING_BRAKE_APPLIED, to = SLOWING),
      @Transition(from = TRAVELLING, event = TRAVELLED_THROUGH_FLOOR, to = TRAVELLING),
      @Transition(from = SLOWING, event = ARRIVED_AT_FLOOR, to = LANDING)
   })
@SuppressWarnings("all")
@Slf4j
public class ElevatorCar
implements IElevatorCar, IElevatorCarPort
{
   public final static String BEAN_NAME = "ElevatorCar";

   @State(accessorType = State.AccessorType.METHOD, getMethodName = "getState",
      setMethodName = "setState")
   private String state;

   @FSM
   private StatefulFSM<ElevatorCar> fsm;

   private final int carIndex;
   // private final IPassengerManifest passengerManifest;
   private final BuildingDescription bldgDesc;
   private final WeightDescription weightDesc;
   private final DoorTimeDescription doorDesc;
   private final IRuntimeClock clock;
   private final IRuntimeScheduler scheduler;
   private final IRuntimeEventBus eventBus;
   private final IElevatorPhysicsService physicsService;
   private final IElevatorDriverLocator driverLocator;
   private IElevatorCarDriver driver = null;

   private ArrayList<PassengersOrigin> onboardPassengers;
   private ArrayList<ScheduledStop> scheduledStops;
   private Comparator<ScheduledStop> currentComparator;
   // private BitSet currentDropRequests = null;
   // private final BitSet pickupsGoingUp = new BitSet();
   // private final BitSet pickupsGoingDown = new BitSet();

   private TravelPathStageNodes travelPathNodes;
   private JourneyArcMomentSeries iterableTrajectory;
   private JourneyArc pathToDestination;
   private PathMoment estimatedLocation;

//   private int reversePathFloorIndex = (-1);
   private int currentFloorIndex = (-1);
   private ScheduledStop currentDestination;
   private DirectionOfTravel currentDirection;
//   private ScheduledStop nextDestination;

   private ITravelGraph currentTravelGraph;
   private ITravelGraph nextTravelGraph;
//   private DirectionOfTravel nextDirection;

   private double incomingWeightLoad;
   private double outgoingWeightLoad;
   private double currentWeightLoad = 0;

   private long tickDurationMillis;


   @Autowired
   public ElevatorCar( @NotNull ICarContext carIndexContext, IElevatorDriverLocator driverLocator,
      @NotNull IElevatorPhysicsService physicsService, @NotNull final IRuntimeClock clock,
      @NotNull final IRuntimeScheduler scheduler, @NotNull final IRuntimeEventBus eventBus,
      @NotNull final DeploymentConfiguration bootstrapData,
      @NotNull final VirtualRuntimeConfiguration runtimeConfig )
   {
      this.physicsService = physicsService;
      this.driverLocator = driverLocator;
      this.carIndex = carIndexContext.getCarIndex();
      this.clock = clock;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
      this.bldgDesc = bootstrapData.getBuilding();
      this.weightDesc = bootstrapData.getWeight();
      this.doorDesc = bootstrapData.getDoors();
      this.tickDurationMillis = runtimeConfig.getTickDurationMillis();
   }


   @Override
   @SneakyThrows
   public void bootstrapDriver(InitialCarState data)
   {
      this.fsm.onEvent(this, DRIVER_INITIALIZED, data);
   }


   @Override
   @SneakyThrows
   public void dropOffRequested(final int floorIndex)
   {
      this.fsm.onEvent(this, DROPOFF_REQUESTED, Integer.valueOf(floorIndex));
   }


   // @Override
   @SneakyThrows
   public void slowForArrival()
   {
      this.fsm.onEvent(this, LANDING_BRAKE_APPLIED);
   }


   @Override
   @SneakyThrows
   public void parkedAtLanding(long timeDelta)
   {
      this.fsm.onEvent(this, STOPPED_AT_LANDING);
   }


   @Override
   @SneakyThrows
   public void passengerDoorsOpened(long timeDelta)
   {
      this.fsm.onEvent(this, DOOR_OPENED);
   }


   @Override
   @SneakyThrows
   public void passengerDoorsClosed(long timeDelta)
   {
      this.fsm.onEvent(this, DOOR_CLOSED);
   }


   @Override
   @SneakyThrows
   public void
   updateWeightLoad(final double initialWeight, final double weightDelta, final double finalWeight)
   {
      this.fsm.onEvent(this, WEIGHT_UPDATED, initialWeight, weightDelta, finalWeight);
   }


   @Override
   @SneakyThrows
   public void confirmNextDispatch(int floorIndex)
   {
      // TODO: Add floorIndex argument or not??
      this.fsm.onEvent(this, DISPATCHED);
   }


   void onClockInterval(long timeDelta)
   {
      log.info(
         "Elevator car {} handles clock interval at {} with {}",
         this.carIndex,
         this.clock.now(),
         timeDelta);
   }


   @Override
   @SneakyThrows
   public void removePickupRequest(final int floorIndex, final DirectionOfTravel direction)
   {
      this.fsm.onEvent(this, DROPPED_PICKUP_REQUEST, floorIndex, direction);
   }


   @Override
   @SneakyThrows
   public void acceptPickupRequest(final int floorIndex, final DirectionOfTravel direction)
   {
      this.fsm.onEvent(this, ACCEPTED_PICKUP_REQUEST, floorIndex, direction);
   }


   @SneakyThrows
   public void notifyFloorSensorTriggered(int floorIndex, DirectionOfTravel direction)
   {
      this.fsm.onEvent(this, TRIGGERED_FLOOR_SENSOR, floorIndex, direction);
   }


   @Transition(from = BEFORE_ALLOCATION, event = ALLOCATED, to = WAITING_FOR_DRIVER)
   public ElevatorCar onAllocation(final String event)
   {
      this.driver = this.driverLocator.locateCarDriver();
      
      return this;
   }


   @Transition(from = WAITING_FOR_DRIVER, event = DRIVER_INITIALIZED, to = AVAILABLE)
   public void onBootstrap(final String event, final InitialCarState data)
   {
      log.info(
         "Driver for elevator car {} called onBootstrap() at {} with {}",
         this.carIndex,
         this.clock.now(),
         data);

      final int initialFloor = data.getInitialFloor();
      final double initialWeight =
         data.passengers.stream()
            .collect(Collectors.summingDouble((p) -> p.weight));
      final List<ScheduledStop> initialDropRequests =
         data.passengers.stream()
            .<ScheduledStop> map(pendingDrop -> {
               return ScheduledDrop.builder()
                  .build();
            })
            .collect(Collectors.toList());
      
      this.currentWeightLoad = initialWeight;
      this.currentFloorIndex = initialFloor;
      this.scheduledStops = new ArrayList<ScheduledStop>(this.bldgDesc.getNumFloors());

      if (initialDropRequests.size() == 0) {
         this.currentDirection = DirectionOfTravel.STOPPED;
         this.currentDestination = null;
         this.currentComparator = null;
      } else {
         ScheduledStop aNewTarget = initialDropRequests.get(0);
         // TODO: Consider relaxing the following assertion for cases where a pickup call is early on a post-rerversal return route.
         if (initialFloor < aNewTarget.getFloorIndex()) {
            Preconditions.checkArgument(
               initialDropRequests.stream().allMatch(stop -> {
                  return stop.getFloorIndex() > this.currentFloorIndex;
               }), "Initial requests must either all initially ascend or all initially descend."
            );
            this.currentComparator = new TravelPathUpComparator(initialFloor);
            this.currentDirection = DirectionOfTravel.GOING_UP;
         } else if (initialFloor > aNewTarget.getFloorIndex()) {
            Preconditions.checkArgument(
               initialDropRequests.stream().allMatch(stop -> {
                  return stop.getFloorIndex() < this.currentFloorIndex;
               }), "Initial requests must either all initially ascend or all initially descend."
            );
            this.currentComparator = new TravelPathDownComparator(initialFloor);
            this.currentDirection = DirectionOfTravel.GOING_DOWN;
         }
      this.scheduledStops = new ArrayList<ScheduledStop>(initialDropRequests);
      Collections.sort(this.scheduledStops, this.currentComparator);
   }


      this.scheduler.scheduleInterrupt(
         this.tickDurationMillis,
         TimeUnit.MILLISECONDS,
         Priorities.STEP_DRIVERS.getValue(),
         this::onClockInterval);
      
      // At this point, scheduledStops only includes drop requests.
      this.eventBus.post(DriverBootstrapped.build(bldr -> {
         bldr.clockTime(this.clock.now())
            .carIndex(this.carIndex)
            .floorIndex(this.currentFloorIndex)
            .weightLoad(this.currentWeightLoad)
            .initialDirection(this.currentDirection)
            .dropRequests(this.toBitSet(this.scheduledStops));
      }));
   }


   @Transition(from = AVAILABLE, event = ACCEPTED_PICKUP_REQUEST)
   public String onAssignedPickupWhileAvailable(final String event, final int floorIndex,
      final DirectionOfTravel direction)
   {
      this.currentDestination = ScheduledPickup.builder().floorIndex(floorIndex).direction(direction).build();
      this.currentDirection = direction;

      if (floorIndex == this.currentFloorIndex) {
         // TODO: Need to notify passengerManifest or no??
         return "event:" + ANSWERED_CALL;
      } else if ((direction == DirectionOfTravel.GOING_UP) || (direction == DirectionOfTravel.GOING_DOWN)) {
         return "event:" + DISPATCHED;
      } else {
         throw new IllegalArgumentException("Cannot be called without an up or down direction");
      }
   }


   @Transition(from = AVAILABLE, event = DROPOFF_REQUESTED)
   public String onAssignedDropOffWhileAvailable(final String event, final int floorIndex)
   {
      this.currentDestination = ScheduledDrop.builder().floorIndex(floorIndex).build();

      if (floorIndex == this.currentFloorIndex) {
         // TODO: This is not yet setting currentDirection because there is no known next destination at time doors open.  Should we
         //       instead make an educated guess?
         this.currentDirection = DirectionOfTravel.STOPPED;
         return "event:" + ANSWERED_CALL;
      } else {
         this.currentDirection = (this.currentFloorIndex < floorIndex) ? DirectionOfTravel.GOING_UP : DirectionOfTravel.GOING_DOWN;
         return "event:" + DISPATCHED;
      }
   }


   @Transitions({
      @Transition(from = TRAVELLING, event = ACCEPTED_PICKUP_REQUEST, to = TRAVELLING),
      @Transition(from = SLOWING, event = ACCEPTED_PICKUP_REQUEST, to = SLOWING),
      @Transition(from = LANDING, event = ACCEPTED_PICKUP_REQUEST, to = LANDING),
      @Transition(from = BOARDING, event = ACCEPTED_PICKUP_REQUEST, to = BOARDING)
   })
   public void
   onAssignedPickupRequest(final String event, final int floorIndex, final DirectionOfTravel direction)
   {
      this.currentDestination = ScheduledPickup.builder().floorIndex(floorIndex).direction(direction).build();
      this.currentDropRequests.set(floorIndex);

      this.eventBus.post(DropOffRequested.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .dropOffFloorIndex(floorIndex);
      }));
   }


   @Transitions({
      @Transition(from = AVAILABLE, event = DROPPED_PICKUP_REQUEST, to = AVAILABLE),
      @Transition(from = TRAVELLING, event = DROPPED_PICKUP_REQUEST, to = TRAVELLING),
      @Transition(from = SLOWING, event = DROPPED_PICKUP_REQUEST, to = SLOWING),
      @Transition(from = LANDING, event = DROPPED_PICKUP_REQUEST, to = LANDING),
      @Transition(from = BOARDING, event = DROPPED_PICKUP_REQUEST, to = BOARDING)
   })
   public void onDroppedPickupRequest(final int floorIndex, final DirectionOfTravel direction)
   {
      if (direction == DirectionOfTravel.GOING_UP) {
         this.pickupsGoingUp.clear(floorIndex);
      } else if (direction == DirectionOfTravel.GOING_DOWN) {
         this.pickupsGoingDown.clear(floorIndex);
      } else {
         throw new IllegalArgumentException("Pickup requests must be ascending or descending");
      }

      this.eventBus.post(DropOffRequested.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .dropOffFloorIndex(floorIndex);
      }));
   }


   @Transitions({
      @Transition(from = TRAVELLING, event = DROPOFF_REQUESTED, to = TRAVELLING),
      @Transition(from = SLOWING, event = DROPOFF_REQUESTED, to = SLOWING),
      @Transition(from = LANDING, event = DROPOFF_REQUESTED, to = LANDING),
      @Transition(from = BOARDING, event = DROPOFF_REQUESTED, to = BOARDING)
   })
   public void onDropOffRequested(final int floorIndex)
   {
      this.currentDropRequests.set(floorIndex);

      this.eventBus.post(DropOffRequested.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .dropOffFloorIndex(floorIndex);
      }));
   }


   @Transitions({
      @Transition(from = AVAILABLE, event = DISPATCHED, to = TRAVELLING),
      @Transition(from = BOARDING, event = DISPATCHED, to = TRAVELLING)
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
                  if ((firstPickupUp < 0)) {}
               }
            }
         }

         if ((this.currentDirection == DirectionOfTravel.GOING_UP) &&
            (this.currentDropRequests.get(this.reversePathFloorIndex) ||
               this.pickupsGoingDown.get(this.reversePathFloorIndex)))
         {
            this.nextDirection = DirectionOfTravel.GOING_DOWN;
            this.nextDestination = this.reversePathFloorIndex;
         } else if ((this.currentDirection == DirectionOfTravel.GOING_DOWN) &&
            (this.currentDropRequests.get(this.reversePathFloorIndex) ||
               this.pickupsGoingDown.get(this.reversePathFloorIndex)))
         {
            this.nextDirection = DirectionOfTravel.GOING_DOWN;
            this.nextDestination = this.reversePathFloorIndex;
         } else {
            this.nextDirection = DirectionOfTowel.STOPPED;
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

      this.eventBus.post(DepartedLanding.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .origin(this.currentFloorIndex)
            .destination(this.currentDestination)
            .direction(this.currentDirection);
      }));
   }


   @Transition(from = LANDING, event = STOPPED_AT_LANDING)
   public String onStoppedAtLanding(final String event)
   {
      this.currentDestination = -1;
      this.currentDirection = DirectionOfTravel.STOPPED;

      if ((this.nextDirection != DirectionOfTravel.STOPPED) ||
         this.currentDropRequests.get(this.currentFloorIndex))
      {
         return "event:" + ANSWERED_CALL;
      } else {
         return "event:" + PARKED;
      }
   }


   @Transitions({
      @Transition(from = AVAILABLE, event = DOOR_ACTIVATED, to = OPENING_DOORS),
      @Transition(from = CLOSING_DOORS, event = DOOR_ACTIVATED, to = OPENING_DOORS)
   })
   public void onRequestDoorCycleOpen(final String event)
   {
      this.driver.openDoors(this.nextDirection);
   }


   @Transitions({
      @Transition(from = LANDING, event = PARKED, to = AVAILABLE),
      @Transition(from = CLOSING_DOORS, event = PARKED, to = AVAILABLE)
   })
   public void onParkedPendingDispatch(final String event)
   {
      this.eventBus.post(ParkedAtLanding.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .floorIndex(this.currentFloorIndex);
      }));
   }


   @Transitions({
      @Transition(from = LANDING, event = ANSWERED_CALL, to = OPENING_DOORS),
      @Transition(from = AVAILABLE, event = ANSWERED_CALL, to = OPENING_DOORS),
      @Transition(from = CLOSING_DOORS, event = ANSWERED_CALL, to = OPENING_DOORS),
   })
   public void onAnsweredCall(final String event)
   {
      ScheduledStop thisCall = this.currentDestination;
      this.currentDestination = this.scheduledStops.
      this.currentDropRequests.clear(this.currentFloorIndex);

      if (this.nextDirection == DirectionOfTravel.GOING_UP) {
         this.pickupsGoingUp.clear(this.currentFloorIndex);
      } else if (this.nextDirection == DirectionOfTravel.GOING_DOWN) {
         this.pickupsGoingDown.clear(this.currentFloorIndex);
      } else {
         throw new IllegalStateException("Cannot answer next all when there is no next call");
      }

      this.driver.openDoors(
         this.currentDestination.getDirection());
   }


   @Transition(from = OPENING_DOORS, event = DOOR_OPENED, to = BOARDING)
   public void onOpenedDoors(final String event)
   {
      this.eventBus.post(PassengerDoorsOpened.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .floorIndex(this.currentFloorIndex)
            .direction(this.currentDestination.getDirection());
      }));
   }


   @Transition(from = CLOSING_DOORS, event = DOOR_CLOSED)
   public String onDoorCloseCompleted(final String event)
   {
      this.eventBus.post(PassengerDoorsClosed.build(bldr -> {
         bldr.carIndex(this.carIndex);
      }));

      if ((this.scheduledStops.size() == 0)) { return "event:" + PARKED; }

      return "event:" + DISPATCHED;
   }


   @Transition(from = TRAVELLING, event = TRIGGERED_FLOOR_SENSOR)
   public String onTravellingThroughFloorSensor(final String event, final int floorIndex,
      final DirectionOfTravel direction)
   {
      this.currentFloorIndex = floorIndex;
      if ((this.currentDirection == direction) &&
         (((this.currentDestination.getFloorIndex() > floorIndex) && (direction == DirectionOfTravel.GOING_UP)) ||
            ((this.currentDestination.getFloorIndex() < floorIndex) && (direction == DirectionOfTravel.GOING_DOWN))))
      {
         this.eventBus.post(TravelledPastFloor.build(bldr -> {
            bldr.carIndex(this.carIndex)
               .floorIndex(floorIndex)
               .direction(direction);
         }));
         return "event:" + TRAVELLED_THROUGH_FLOOR;
      }

      log.error("Stopping for panic after unexpected floor sensor trigger fired");
      return "event:" + PANICKED;
   }


   @Transition(from = SLOWING, event = TRIGGERED_FLOOR_SENSOR)
   public String onSlowingThroughFloorSensor(final String event, final int floorIndex,
      final DirectionOfTravel direction)
   {
      if (
         (this.currentDestination != null) && 
         (this.currentDestination.getFloorIndex() == floorIndex) &&
         (this.currentDirection == direction)) {
         this.eventBus.post(SlowedForArrival.build(bldr -> {
            bldr.carIndex(this.carIndex);
         }));
         return "event:" + ARRIVED_AT_FLOOR;
      }

      log.error("Stopping for panic after unexpected floor sensor trigger fired");
      return "event:" + PANICKED;
   }


   @Transition(from = "*", event = WEIGHT_UPDATED, to = "*")
   public void
   onWeightUpdated(final double previousWeight, final double weightDelta, final double currentWeight)
   {
      this.currentWeightLoad = currentWeight;
      this.eventBus.post(WeightLoadUpdated.build(bldr -> {
         bldr.carIndex(this.carIndex)
            .previous(previousWeight)
            .delta(weightDelta)
            .current(currentWeight);
      }));
   }


   @Transition(from = "*", event = PANICKED, to = SAFETY_LOCKOUT)
   public void onSafetyPanic(final String event)
   {
      log.error("Safety panic triggered!");
   }


   private BitSet toBitSet(final List<ScheduledStop> list)
   {
      final BitSet bitSet = new BitSet();
      list.stream().map(stop -> stop.getFloorIndex()).forEach(bitSet::set);
      return bitSet;
   }


   public String getState()
   {
      return this.state;
   }


   public void setState(final String state)
   {
      this.state = state;
   }


   @Override
   public int getCarIndex()
   {
      return this.carIndex;
   }


   @Override
   public int getCurrentFloorIndex()
   {
      return this.currentFloorIndex;
   }


   @Override
   public double getExpectedLocation()
   {
      return this.estimatedLocation.getHeight();
   }


   @Override
   public double getCurrentWeightLoad()
   {
      return this.currentWeightLoad;
   }


   @Override
   public double getMaximumWeightLoad()
   {
      return this.weightDesc.getMaxWeightAllowed();
   }
}
