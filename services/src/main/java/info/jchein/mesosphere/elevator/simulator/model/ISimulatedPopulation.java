package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;

public interface ISimulatedPopulation
{
   String getPopulationName();
   void initPoissonProcess(IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus);
}
