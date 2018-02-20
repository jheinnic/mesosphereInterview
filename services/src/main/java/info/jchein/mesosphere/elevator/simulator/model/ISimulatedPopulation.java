package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;

public interface ISimulatedPopulation
{
   String getPopulationName();
   void initPoissonProcess(IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus);
}
