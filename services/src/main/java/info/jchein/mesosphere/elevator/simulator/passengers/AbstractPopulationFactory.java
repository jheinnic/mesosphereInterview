package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.function.Supplier;

import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;

public abstract class AbstractPopulationFactory<V extends IRandomVariables, S extends ISimulatedTraveller<V>> implements ISimulatedTravellerSourceFactory<V, S>>
{
   protected AbstractPopulationFactory( IRuntimeEventBus eventBus )
   {
   }

   S createPopulation(Supplier<V> prng) {

   }
}
