package info.jchein.mesosphere.elevator.simulator.passengers;

import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;

/**
 * Generator interface with a starting point of cross-recurring variables from population arrival simulators {@link ISimulatedPopulation}.   
 * 
 * This interface is intended to be extended by any given ISimulatedTravellerSource subtype.
 * 
 * @author jheinnic
 */
public interface IRandomVariables
{
   long getMillisecondsToNextArrival();

   int getInitialFloor();

   double getWeight();
}
