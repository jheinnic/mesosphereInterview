package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.function.BiFunction;

import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;

import info.jchein.mesosphere.elevator.simulator.passengers.AbstractPopulationFactory;


public class PopulationFactoryWithLobbyReturn
extends AbstractPopulationFactory<WithLobbyReturnRandomVariables, TravellerWithLobbyReturn>
{
   private final State<TravellerWithLobbyReturn> activityState;


   PopulationFactoryWithLobbyReturn(
      BiFunction<WithLobbyReturnRandomVariables, FSM<TravellerWithLobbyReturn>, TravellerWithLobbyReturn> factoryFn,
      State<TravellerWithLobbyReturn> activityState )
   {
      super(factoryFn);
      this.activityState = activityState;
   }


   @Override
   protected void appendAdditionalStates()
   {
      this.activityState.addTransition(TravellerWithLobbyReturn.FINISHED_ACTIVITY, this.queuedForPickup, this.queueForPickupAction);

      this.listBuilder.add(this.activityState);
   }
}
