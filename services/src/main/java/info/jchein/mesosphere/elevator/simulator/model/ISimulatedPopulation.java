package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;

public interface ISimulatedPopulation
{
   String getPopulationName();
   void initPoissonProcess(IRuntimeScheduler scheduler);
}
