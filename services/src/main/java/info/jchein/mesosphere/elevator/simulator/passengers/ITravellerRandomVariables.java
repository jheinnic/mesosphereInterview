package info.jchein.mesosphere.elevator.simulator.passengers;


import info.jchein.mesosphere.elevator.common.PassengerId;


public interface ITravellerRandomVariables
{
   double getSecondsToNextArrival();

   PassengerId getNextPassengerId();

   int getInitialFloor();

   double getWeight();
}
