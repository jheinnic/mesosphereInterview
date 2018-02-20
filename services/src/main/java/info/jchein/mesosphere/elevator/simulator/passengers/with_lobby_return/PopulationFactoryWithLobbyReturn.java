package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.impl.StateImpl;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractPopulationFactory;


public class PopulationFactoryWithLobbyReturn
extends
AbstractPopulationFactory<VariablesWithLobbyReturn, TravellerWithLobbyReturn, PopulationWithLobbyReturn>
{
   private final State<TravellerWithLobbyReturn> activityState;


   public PopulationFactoryWithLobbyReturn( String populationName, Supplier<PassengerId> idFactory,
      Supplier<VariablesWithLobbyReturn> randomVariableSupplier,
      ExponentialDistribution arrivalInterval )
   {
      super(populationName, idFactory, randomVariableSupplier, arrivalInterval);
      this.activityState = new StateImpl<TravellerWithLobbyReturn>(WithLobbyReturn.States.IN_ACTIVITY);
   }


   @Override
   protected void appendAdditionalStates()
   {
      this.activityState.addTransition(
         WithLobbyReturn.Events.FINISHED_ACTIVITY,
         this.queuedForPickup,
         this.queueForPickupAction);

      this.listBuilder.add(this.activityState);
   }


   @Override
   public Class<PopulationWithLobbyReturn> getObjectType()
   {
      return PopulationWithLobbyReturn.class;
   }


   @Override
   protected PopulationWithLobbyReturn createPopulation(Supplier<PassengerId> idFactory,
      FSM<TravellerWithLobbyReturn> stateMachine, Supplier<VariablesWithLobbyReturn> variablesSupplier,
      ExponentialDistribution arrivalRate)
   {
      return new PopulationWithLobbyReturn(
         idFactory, stateMachine, this.activityState, variablesSupplier, arrivalRate);
   }
}
