package info.jchein.mesosphere.elevator.simulator.passengers;

import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTravellerSource;

public interface ISimulatedTravellerSourceLookup
{
    ISimulatedTravellerSource getTravellerSource(String beanName);
}
