package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;


@Configuration
@ComponentScan("info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return")
public class WithLobbyReturnPathStyleConfiguration
{
   private Supplier<PassengerId> idFactory;
   private IRuntimeClock clock;
   private IRuntimeScheduler scheduler;
   private IRuntimeEventBus eventBus;


   @Autowired
   WithLobbyReturnPathStyleConfiguration( Supplier<PassengerId> idFactory, IRuntimeClock clock,
      IRuntimeScheduler scheduler, IRuntimeEventBus eventBus )
   {
      this.idFactory = idFactory;
      this.clock = clock;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
   }


   /**
    * SimulatedTravelllers for the "WithLobbyReturn" bundle are created with the help of a Java configuration
    * class by injecting a handle to this bean prototype method into the Population Factory that creates the
    * Populations that create Travellers.
    * 
    * The Population objects are not subtyped, so they have no way of specializing their construction behavior
    * sufficiently to understand how to inject unique arguments into the Traveller allocation requests.  
    * @param randomVariables
    * @param stateMachine
    * @return
   @Bean
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   TravellerWithLobbyReturn travellerWithLobbyReturn(VariablesWithLobbyReturn randomVariables, FSM<TravellerWithLobbyReturn> stateMachine) {
      return new TravellerWithLobbyReturn(this.idFactory.get(), randomVariables,
         stateMachine, this.withLobbyReturnStateSupplier.getInActivityState(), this.clock, this.scheduler, this.eventBus);
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   PopulationFactoryWithLobbyReturn populationFactoryWithLobbyReturn() {
      return new PopulationFactoryWithLobbyReturn(this::travellerWithLobbyReturn, this.withLobbyReturnStateSupplier.getInActivityState());
   }
    */
}
