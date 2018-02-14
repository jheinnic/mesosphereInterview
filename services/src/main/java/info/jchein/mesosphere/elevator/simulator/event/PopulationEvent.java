package info.jchein.mesosphere.elevator.simulator.event;

import info.jchein.mesosphere.elevator.common.PassengerId;

public interface PopulationEvent extends SimulationEvent
{
   String getPopulationName();
}
