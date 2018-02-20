package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractPopulation;

public class PopulationWithLobbyReturn extends AbstractPopulation<VariablesWithLobbyReturn, TravellerWithLobbyReturn, PopulationWithLobbyReturn>
{
   private final State<TravellerWithLobbyReturn> inActivityState;

   PopulationWithLobbyReturn( Supplier<PassengerId> idFactory, FSM<TravellerWithLobbyReturn> stateMachine, State<TravellerWithLobbyReturn> inActivityState, 
      Supplier<VariablesWithLobbyReturn> randomGenerator, ExponentialDistribution arrivalRate )
   {
      super(idFactory, stateMachine, randomGenerator, arrivalRate);
      this.inActivityState = inActivityState;
   }

   @Override
   protected TravellerWithLobbyReturn createTraveller(PassengerId id, VariablesWithLobbyReturn randomVariables,
      FSM<TravellerWithLobbyReturn> stateMachine, IRuntimeClock clock, IRuntimeScheduler scheduler,
      IRuntimeEventBus eventBus)
   {
      return new TravellerWithLobbyReturn(id, randomVariables, stateMachine, inActivityState, clock, scheduler, eventBus);
   }
}
