package info.jchein.mesosphere.elevator.simulator.passengers;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.event.BoardedElevator;
import info.jchein.mesosphere.elevator.simulator.event.DisembarkedElevator;
import info.jchein.mesosphere.elevator.simulator.event.PickupRequested;
import info.jchein.mesosphere.elevator.simulator.event.TravellerArrived;
import info.jchein.mesosphere.elevator.simulator.event.TravellerDeparted;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;
import info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return.VariablesWithLobbyReturn;
import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.SneakyThrows;


public abstract class AbstractTraveller<V extends IRandomVariables, T extends AbstractTraveller<V, T>>
implements ISimulatedTraveller
{
   @NotNull
   protected final PassengerId id;

   @NotNull
   protected final IRuntimeScheduler scheduler;

   @NotNull
   protected final IRuntimeClock clock;

   @NotNull
   protected final IRuntimeEventBus eventBus;

   @NotNull
   protected final FSM<T> stateMachine;

   @Positive
   protected final double weight;

   @Min(0)
   private int currentFloor;

   private int destinationFloor = -1;

   private long enterSimulationTime = -1;

   private long latestCallTime = -1;

   private int pickupCarIndex = -1;

   private long latestPickupTime = -1;

   private long latestDropOffTime = -1;

   private long exitSimulationTime = -1;

   private DirectionOfTravel nextDirection = DirectionOfTravel.STOPPED;

   private State<T> destinationState = null;


   protected AbstractTraveller( @NotNull PassengerId id, @NotNull VariablesWithLobbyReturn randomVariables,
      @NotNull FSM<T> stateMachine, @NotNull IRuntimeClock clock, @NotNull IRuntimeScheduler scheduler,
      @NotNull IRuntimeEventBus eventBus )
   {
      this.id = id;
      this.stateMachine = stateMachine;
      this.clock = clock;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
      this.currentFloor = randomVariables.getInitialFloor();
      this.weight = randomVariables.getWeight();
   }


   protected abstract State<T> getInitialState();

   @SneakyThrows
   @SuppressWarnings("unchecked")
   public void queueForPickup() { 
      this.stateMachine.onEvent((T) this, CommonEvents.REQUESTED_PICKUP.asEventName());
   }
   
   @SneakyThrows
   @SuppressWarnings("unchecked")
   public void boardElevator(int carIndex) {
      this.stateMachine.onEvent((T) this, CommonEvents.BOARDED_ELEVATOR.asEventName(), carIndex);
   }
   
   @SneakyThrows
   @SuppressWarnings("unchecked")
   public void disembarkElevator() {
      this.stateMachine.onEvent((T) this, CommonEvents.DISEMBARKED_ELEVATOR.asEventName());
   }

   void onEnteredSimulation()
   {
      this.enterSimulationTime = this.clock.now();
      this.eventBus.post(TravellerArrived.build(bldr -> {
         bldr.clockTime(this.enterSimulationTime)
            .traveller(this);
      }));
      this.afterEnteredSimulation();
   }


   void onQueuedForPickup()
   {
      this.latestCallTime = this.clock.now();
      
      this.eventBus.post(PickupRequested.build(bldr -> {
         bldr.clockTime(this.latestCallTime)
            .traveller(this)
            .originIndex(this.currentFloor)
            .destinationIndex(this.destinationFloor)
            .direction(this.nextDirection);
      }));

      this.afterQueuedForPickup();
   }


   void onBoardedElevator(@Min(0) int pickupCarIndex)
   {
      this.latestPickupTime = this.clock.now();
      this.pickupCarIndex = pickupCarIndex;

      this.eventBus.post(BoardedElevator.build(bldr -> {
         bldr.clockTime(this.latestPickupTime)
            .traveller(this)
            .carIndex(pickupCarIndex)
            .originFloorIndex(this.currentFloor)
            .destinationFloorIndex(this.destinationFloor);
      }));

      this.afterBoardedElevator(pickupCarIndex);
   }


   void onDisembarkedElevator()
   {
      final int carIndex = this.pickupCarIndex;

      this.latestDropOffTime = this.clock.now();
      this.currentFloor = this.destinationFloor;
      this.destinationFloor = -1;
      this.destinationState = null;
      this.pickupCarIndex = -1;
      this.nextDirection = DirectionOfTravel.STOPPED;
      
      this.eventBus.post(DisembarkedElevator.build(bldr -> {
         bldr.clockTime(this.latestDropOffTime)
            .traveller(this)
            .carIndex(carIndex)
            .floorIndex(this.currentFloor);
      }));

      this.afterDisembarkedElevator();
   }


   protected void onExitedSimulation()
   {
      this.exitSimulationTime = this.clock.now();
      this.eventBus.post(TravellerDeparted.build(bldr -> {
         bldr.clockTime(this.exitSimulationTime)
            .traveller(this);
      }));
      
      this.afterExitedSimulation();
   }


   abstract protected void afterEnteredSimulation();
   abstract protected void afterQueuedForPickup();
   abstract protected void afterBoardedElevator(int pickupCarIndex);
   abstract protected void afterDisembarkedElevator();
   abstract protected void afterExitedSimulation();


   /**
    * Override memory for current floor. Not likely to be needed often, but present for scenarios such as a traveller
    * having used a stairwell. The stairwell requires special case handling because it moves the starting point of the
    * next elevator ride, and does so without having used the elevator to get there.
    * 
    * @param currentFloor
    *           New value for currentFloor that will be used after next call to {@link #callForPickup(int)}
    */
   protected void setCurrentFloor(int currentFloor)
   {
      this.currentFloor = currentFloor;
   }


   /**
    * Sets the next elevator destination floor and sets up to leave the simulation on disembarking.
    * 
    * @param destinationFloor
    */
   protected void setDestinationFloor(int destinationFloor)
   {
      this.setDestinationFloor(destinationFloor, null);
   }


   /**
    * Sets the next elevator destination floor and sets the next state to transition to on disembarking.
    * 
    * @param destinationFloor
    */
   protected void setDestinationFloor(int destinationFloor, State<T> destinationState)
   {
      this.destinationFloor = destinationFloor;
      if (this.destinationFloor > this.currentFloor) {
         this.nextDirection = DirectionOfTravel.GOING_UP;
      } else if (this.destinationFloor < this.currentFloor) {
         this.nextDirection = DirectionOfTravel.GOING_DOWN;
      } else {
         throw new IllegalArgumentException("Destination and origin floors may not be the same");
      }

      this.destinationState = destinationState;
   }


   /**
    * deprecated Use the protected final IRuntimeScheduler reference directly instrzd.
    * 
    * @param millisecondInterval
    * @param priority
    * @param handler
    */
//   protected void
//   scheduleCallback(@Min(1) long millisecondInterval, int priority, @NotNull IIntervalHandler handler)
//   {
//      this.scheduler.scheduleOnce(millisecondInterval, TimeUnit.MILLISECONDS, priority, handler);
//   }


   /*
    * protected void setRandomVariables(RandomVariables variableOverrides) { this.random/initRan }
    */
   public PassengerId getId()
   {
      return id;
   }


   public String getPopulationName()
   {
      return this.stateMachine.getName();
   }


   public double getWeight()
   {
      return weight;
   }


   public int getCurrentFloor()
   {
      return currentFloor;
   }


   public int getDestinationFloor()
   {
      return destinationFloor;
   }


   public State<T> getDestinationState()
   {
      return this.destinationState;
   }


   protected long getLatestCallTime()
   {
      return latestCallTime;
   }


   public int getPickupCarIndex()
   {
      return pickupCarIndex;
   }


   protected long getLatestPickupTime()
   {
      return latestPickupTime;
   }


   protected long getLatestDropOffTime()
   {
      return latestDropOffTime;
   }
}
