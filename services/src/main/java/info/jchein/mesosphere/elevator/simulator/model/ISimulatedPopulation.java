package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.simulator.passengers.AbstractTraveller;
import info.jchein.mesosphere.elevator.simulator.passengers.IRandomVariables;

public interface ISimulatedPopulation
{
   void initPoissonProcess();
}
