package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import org.statefulj.fsm.model.State;


public interface IWithLobbyReturnStateSupplier
{
   public static final String IN_ACTIVITY = "StateInActivity";


   State<TravellerWithLobbyReturn> getInActivityState();
}
