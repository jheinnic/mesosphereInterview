package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import info.jchein.mesosphere.elevator.simulator.passengers.IRandomVariables;

public interface IWithLobbyReturnRandomVariables extends IRandomVariables
{
   int getActivityFloor();
   
   long getActivityDuration();
}
