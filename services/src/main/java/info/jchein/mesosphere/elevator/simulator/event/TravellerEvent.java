package info.jchein.mesosphere.elevator.simulator.event;

import info.jchein.mesosphere.elevator.common.PassengerId;

public interface TravellerEvent extends SimulationEvent
{
   PassengerId getTravellerId();
   String getPopulationName();
}
