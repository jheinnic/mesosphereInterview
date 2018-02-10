package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.concurrent.TimeUnit;

import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTravellerSource;

public abstract class AbstractSimulatedTravellerSource<V extends ITravellerRandomVariables> implements ISimulatedTravellerSource
{
   private final IRuntimeScheduler scheduler;
   protected final V randomVariables;
   
   protected AbstractSimulatedTravellerSource(IRuntimeScheduler scheduler, V randomVariables) {
      this.scheduler = scheduler;
      this.randomVariables = randomVariables;
   }

   @Override
   public void scheduleFirstArrival()
   {
      this.scheduleNextArrival();
   }

   protected void scheduleNextArrival()
   {
      final long timeUntilNext = Math.round(this.randomVariables.getSecondsToNextArrival() * 1000);
      this.scheduler.scheduleOnce(timeUntilNext, TimeUnit.MILLISECONDS, 0, this::onArrival);
   }
   
   protected abstract void onArrival(long interval);
}
