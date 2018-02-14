package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.IIntervalHandler;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.event.TravellerArrived;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;
import info.jchein.mesosphere.validator.annotation.Positive;


public abstract class AbstractTraveller<V extends IRandomVariables>
implements ISimulatedTraveller
{
   @NotNull
   private final PassengerId id;

   @NotNull
   private final IRuntimeScheduler scheduler;

   @NotNull
   private final IRuntimeClock clock;

   @NotNull
   private final IRuntimeEventBus eventBus;

   @NotNull
   private final String populationName;
   
   @Positive
   private double weight;

   @Min(0)
   private int currentFloor;

   @Min(0)
   private int destinationFloor;

   private long latestCallTime;

   private int boardedCarIndex;

   private long latestPickupTime;

   private long latestDropOffTime;


   protected AbstractTraveller( @NotNull PassengerId id, @NotNull String populationName,
      @NotNull IRuntimeScheduler scheduler, @NotNull IRuntimeClock clock, @NotNull IRuntimeEventBus eventBus)
   {
      this.id = id;
      this.populationName = populationName;
      this.scheduler = scheduler;
      this.clock = clock;
      this.eventBus = eventBus;
   }


   protected void initRandomVariables(@NotNull V randomVariables)
   {
      this.currentFloor = randomVariables.getInitialFloor();
      this.weight = randomVariables.getWeight();
      
      this.eventBus.post(
         TravellerArrived.build(bldr -> {
            bldr.clockTime(this.clock.now());
         })
      );
   }


   @Override
   public abstract void onQueuedForPickup();


   @Override
   public abstract void onSuccessfulPickup(int pickupCarIndex);


   @Override
   public abstract void onSuccessfulDropOff();


   /**
    * Override memory for current floor. Not likely to be needed often, but present for scenarios such as a
    * traveller having used a stairwell.  The stairwell requires special case handling because it moves the
    * starting point of the next elevator ride, and does so without havinso==========================
    * 
    * @param currentFloor
    *           New value for currentFloor that will be used on next call to {@link #callForPickup(int)}
    */
   protected void setCurrentFloor(int currentFloor)
   {
      this.currentFloor = currentFloor;
   }


//   protected void callForPickup(int destinationFloor)
//   {
//       TODO: Verify upper bound on floorIndices too!!
//      Preconditions.checkArgument(0 <= destinationFloor);
//      Preconditions.checkArgument(this.currentFloor != destinationFloor);
//      Preconditions.checkState(this.currentFloor >= 0);
//      Preconditions.checkState(this.weight > 0);

//      this.destinationFloor = destinationFloor;
//      this.latestCallTime = this.clock.now();
//      this.queueService.queueForPickup(this);
//   }


   /**
    * @deprecated Use the protected final IRuntimeScheduler reference directly instrzd.
    * 
    * @param millisecondInterval
    * @param priority
    * @param handler
    */
   @Deprecated
   protected void
   scheduleCallback(@Min(1) long millisecondInterval, int priority, @NotNull IIntervalHandler handler)
   {
      this.scheduler.scheduleOnce(millisecondInterval, TimeUnit.MILLISECONDS, priority, handler);
   }


   /*
    * protected void setRandomVariables(RandomVariables variableOverrides) { this.random/initRan }
    */

   public PassengerId getId()
   {
      return id;
   }

   public String getPopulationName() {
      return this.populationName;
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


   public long getLatestCallTime()
   {
      return latestCallTime;
   }


   public int getPickupCarIndex()
   {
      return boardedCarIndex;
   }


   public long getLatestPickupTime()
   {
      return latestPickupTime;
   }


   public long getLatestDropOffTime()
   {
      return latestDropOffTime;
   }
}
