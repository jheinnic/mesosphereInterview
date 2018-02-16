package info.jchein.mesosphere.elevator.simulator.passengers;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;

/**
 * Constructor conveinence interface for bundling all the things a Traveller acquires from its parent Population into a single argument
 * 
 * @author jheinnic
 */
public interface ITravellerParentPopulation
{
   String getPopulationName();
   
   IRuntimeScheduler getScheduler();
   
   IRuntimeClock getClock();
   
   IRuntimeEventBus getEventBus();
}
