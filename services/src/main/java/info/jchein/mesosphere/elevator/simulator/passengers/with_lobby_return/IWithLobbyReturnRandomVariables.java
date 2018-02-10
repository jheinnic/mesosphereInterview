package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import info.jchein.mesosphere.elevator.simulator.passengers.ITravellerRandomVariables;

public interface IWithLobbyReturnRandomVariables extends ITravellerRandomVariables
{
   int getActivityFloor();
   
   long getActivityDuration();
}
