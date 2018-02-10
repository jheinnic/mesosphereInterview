package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.model.StatefulFSM;
import org.statefulj.fsm.TooBusyException;

import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractSimulatedTravellerSource;

public class WithLobbyReturnTravellerSource extends AbstractSimulatedTravellerSource<IWithLobbyReturnRandomVariables>
{
   private static final Logger log = LoggerFactory.getLogger(WithLobbyReturnTravellerSource.class);

   @FSM
   StatefulFSM<WithLobbyReturnTraveller> fsm;

   WithLobbyReturnTravellerSource(IRuntimeScheduler scheduler, IWithLobbyReturnRandomVariables randomVariables) {
      super(scheduler, randomVariables);
   }

   protected void onArrival(long interval)
   {
      try {
         this.fsm.onEvent(WithLobbyReturnTraveller.ENTERED_SIMULATION, this.randomVariables);
      }
      catch (TooBusyException e) {
         // TODO Auto-generated catch block
         log.error("Failed to allocate traveller at arrival time.  Skipping...?", e);
      }

      this.scheduleNextArrival();
   }
}
