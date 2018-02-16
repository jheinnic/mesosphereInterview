package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.common.physics.IPopulationSampler;

//@Component
//@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WithLobbyReturnRandomVariableSupplier implements Supplier<WithLobbyReturnRandomVariables>
{
   private IPopulationSampler weightSampler;

//   @Autowired
   public WithLobbyReturnRandomVariableSupplier(IPopulationSampler weightSampler) {
      this.weightSampler = weightSampler;
   }

   @Override
   public WithLobbyReturnRandomVariables get()
   {
      return WithLobbyReturnRandomVariables.build(bldr -> {
         return bldr.
      });
   }

}
