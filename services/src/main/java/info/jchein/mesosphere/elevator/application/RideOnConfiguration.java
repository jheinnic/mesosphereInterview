package info.jchein.mesosphere.elevator.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulationEcosystem;

@Configuration
public class RideOnConfiguration
{
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public RideOnRunner runner(IRuntimeScheduler scheduler, IRuntimeClock clock, SimulationEcosystem simulationEcosystem ) 
   {
      return new RideOnRunner(scheduler, clock, simulationEcosystem);
   }
}
