import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.Action;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.impl.StateImpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import info.jchein.mesosphere.elevator.common.IValueFactory;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.event.PickupRequested;
import info.jchein.mesosphere.elevator.simulator.event.TravellerArrived;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractPopulation;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonStates.CommonStateNames;


public class PopulationWithLobbyReturn
extends AbstractPopulation<IWithLobbyReturnRandomVariables, TravellerWithLobbyReturn>
private static final Logger log = LoggerFactory.getLogger(PopulationWithLobbyReturn.class);


PopulationWithLobbyReturn( IRuntimeScheduler scheduler, IRuntimeClock clock, IRuntimeEventBus eventBus,
   IValueFactory valueFactory, Supplier<IWithLobbyReturnRandomVariables> randomVariablesGenerator,
   ExponentialDistribution expoDist )
{
      super(scheduler, clock, eventBus, valueFactory, randomVariablesGenerator, expoDist);
   }


   @Override
   protected TravellerWithLobbyReturn
   allocateNextArrival(PassengerId passengerId, IWithLobbyReturnRandomVariables randomValues)
   {
      // TODO Auto-generated method stub
      return null;
   }


   @Override
   protected FSM<TravellerWithLobbyReturn> constructStateMachine()
   {
      final Builder<State<TravellerWithLobbyReturn>> listBuilder =
         ImmutableList.<State<TravellerWithLobbyReturn>>builder();

      final State<TravellerWithLobbyReturn> BEFORE_ARRIVAL = new StateImpl<TravellerWithLobbyReturn>(CommonStateNames.BEFORE_ARRIVAL);

      final State<TravellerWithLobbyReturn> QUEUED_FOR_PICKUP = new StateImpl<TravellerWithLobbyReturn>(CommonStateNames.QUEUED_FOR_PICKUP);
      final State<TravellerWithLobbyReturn> RIDING_ELEVATOR = new StateImpl<TravellerWithLobbyReturn>(CommonStateNames.RIDING_ELEVATOR);
      final State<TravellerWithLobbyReturn> IN_ACTIVITY = new StateImpl<TravellerWithLobbyReturn>(CommonStateNames.IN_ACTIVITY);
      final State<TravellerWithLobbyReturn> AFTER_DEPARTURE = new StateImpl<TravellerWithLobbyReturn>(CommonStateNames.AFTER_DEPARTURE, true);
      
      final Action<TravellerWithLobbyReturn> ON_ENTERED_SIMULATION_ACTION = (stateful, event, args) -> {
         IWithLobbyReturnRandomVariables randomVariables = (IWithLobbyReturnRandomVariables) args[0];
         stateful.initRandomVariables(randomVariables);
         
      };
      final Action<TravellerWithLobbyReturn> ON_REQUESTED_PICKUP_ACTION = (stateful, event, args) -> {
         this.eventBus.post(
            PickupRequested.build(bldr -> {
               bldr.clockTime(TravellerWithLobbyReturn.this.clock.now())
               .travellerId(TravellerWithLobbyReturn.this.getId())
               .populationName(TravellerWithLobbyReturn.this.getPopulationName())
               .floorIndex(TravellerWithLobbyReturn.this.getCurrentFloor())
               .direction(TravellerWithLobbyReturn.this.get)
            })
         );
      };
      
      
      return new Fsa
   }
}£
