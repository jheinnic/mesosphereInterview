package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.impl.StateImpl;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WithLobbyReturnStateSupplier
implements IWithLobbyReturnStateSupplier
{
   private State<TravellerWithLobbyReturn> inActivityState;

   WithLobbyReturnStateSupplier() {
      this.inActivityState = new StateImpl<>(IWithLobbyReturnStateSupplier.IN_ACTIVITY);
   }

   @Override
   public State<TravellerWithLobbyReturn> getInActivityState()
   {
      return this.inActivityState;
   }

}
