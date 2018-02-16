package info.jchein.mesosphere.elevator.simulator.passengers;

import info.jchein.mesosphere.elevator.common.PassengerId;

public interface ITravellerFactory<V extends IRandomVariables, T extends AbstractTraveller<V, T>>
{
//   V generate();
   
   T allocateNext(PassengerId passengerId, V randomValues);
   
//   T allocateNext();
}
