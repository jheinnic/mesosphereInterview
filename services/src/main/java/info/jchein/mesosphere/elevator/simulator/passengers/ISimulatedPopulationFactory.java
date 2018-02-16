package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;


public interface ISimulatedPopulationFactory<V extends IRandomVariables>
{
   ISimulatedPopulation generate(String populationName, Supplier<V> randomGenerator, ExponentialDistribution arrivalRate);
}
