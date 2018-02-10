package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.CompletedTrip;
import info.jchein.mesosphere.elevator.common.IValueFactory;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.common.PendingDropOff;
import info.jchein.mesosphere.elevator.common.PendingPickup;
import info.jchein.mesosphere.elevator.runtime.IIntervalHandler;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;
import info.jchein.mesosphere.elevator.simulator.model.ITravellerQueueService;
import info.jchein.mesosphere.validator.annotation.Positive;

public abstract class AbstractSimulatedTraveller<V extends ITravellerRandomVariables> implements ISimulatedTraveller
{
   @NotNull
   private PassengerId id;
   
   @NotNull
   private final ITravellerQueueService queueService;
   
   @Positive
   private double weight;

   @Min(0)
   private int currentFloor;
   
   private int destinationFloor;
   
   private long latestCallTime;
   
   private int boardedCarIndex;
   
   private long latestPickupTime;
   
   private long latestDropOffTime;

   private final IRuntimeScheduler scheduler;

   private final IRuntimeClock clock;
   
   protected AbstractSimulatedTraveller(
      @NotNull IRuntimeScheduler scheduler, @NotNull IRuntimeClock clock, @NotNull IValueFactory valueFactory, @NotNull ITravellerQueueService queueService)
   {
      this.id = valueFactory.getNextPassengerId();
      this.scheduler = scheduler;
      this.clock = clock;
      this.queueService = queueService;
   }
   
   protected void initRandomVariables(@NotNull V randomVariables)
   {
      this.currentFloor = randomVariables.getInitialFloor();
      this.weight = randomVariables.getWeight();
   }

   @Override
   public abstract void onQueuedForPickup( );

   @Override
   public abstract void onSuccessfulPickup(int boardedCarIndex);

   @Override
   public abstract void onSuccessfulDropOff();

   /**
    * Override the memory for current floor.  Not likely to be needed often, but present for scenarios such as a traveller having used a stairwell.
    * 
    * @param currentFloor New value for currentFloor that will be used on next call to {@link #callForPickup(int)}
    */
   protected void setCurrentFloor(int currentFloor) {
      this.currentFloor = currentFloor;
   }

   protected void callForPickup(int destinationFloor) 
   {
      // TODO: Verify upper bound on floorIndices too!!
      Preconditions.checkArgument( 0 <= destinationFloor);
      Preconditions.checkArgument(this.currentFloor != destinationFloor);
      Preconditions.checkState(this.currentFloor >= 0);
      Preconditions.checkState(this.weight > 0);

      this.destinationFloor = destinationFloor;
      this.latestCallTime = this.clock.now();
      this.queueService.queueForPickup(this);
   }
   
   protected void scheduleCallback(@Min(1) long millisecondInterval, int priority, @NotNull IIntervalHandler handler) {
      this.scheduler.scheduleOnce(millisecondInterval, TimeUnit.MILLISECONDS, priority, handler);
   }

   public PassengerId getId()
   {
      return id;
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

   public int getBoardedCarIndex()
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
