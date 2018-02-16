package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;


@Configuration
@ComponentScan("info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return")
public class WithLobbyReturnPathStyleConfiguration
{
   private Supplier<PassengerId> idFactory;
   private IRuntimeClock clock;
   private IRuntimeScheduler scheduler;
   private IRuntimeEventBus eventBus;
   private IWithLobbyReturnStateSupplier withLobbyReturnStateSupplier;


   @Autowired
   WithLobbyReturnPathStyleConfiguration( Supplier<PassengerId> idFactory, IRuntimeClock clock,
      IRuntimeScheduler scheduler, IRuntimeEventBus eventBus,
      IWithLobbyReturnStateSupplier withLobbyReturnStateSupplier )
   {
      this.idFactory = idFactory;
      this.clock = clock;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
      this.withLobbyReturnStateSupplier = withLobbyReturnStateSupplier;
   }


   @Bean
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   TravellerWithLobbyReturn travellerWithLobbyReturn(WithLobbyReturnRandomVariables randomVariables, FSM<TravellerWithLobbyReturn> stateMachine) {
      return new TravellerWithLobbyReturn(this.idFactory.get(), randomVariables,
         stateMachine, this.withLobbyReturnStateSupplier.getInActivityState(), this.clock, this.scheduler, this.eventBus);
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   PopulationFactoryWithLobbyReturn populationFactoryWithLobbyReturn() {
      return new PopulationFactoryWithLobbyReturn(this::travellerWithLobbyReturn, this.withLobbyReturnStateSupplier.getInActivityState());
   }
}
