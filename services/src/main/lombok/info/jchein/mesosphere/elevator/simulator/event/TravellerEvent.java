package info.jchein.mesosphere.elevator.simulator.event;

import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;

public interface TravellerEvent extends SimulationEvent
{
   ISimulatedTraveller getTraveller();
}
