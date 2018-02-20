package info.jchein.mesosphere.elevator.simulator.model;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.control.model.IElevatorGroupControl;
import info.jchein.mesosphere.elevator.emulator.model.EmulatorRegistrationConfigurationProperties;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorControl;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulationEcosystem;


@Configuration
//And scan the location for profile-dependent workload defiitions to bootstrap.  There should be one simulation profile
//in the active set to determine which worklad's contents to create and manage.
@ComponentScan({"info.jchein.mesosphere.elevator.simulator.workloads", "info.jchein.mesosphere.elevator.simulator.model", "info.jchein.mesosphere.elevator.simulator.passengers"})
@EnableConfigurationProperties(EmulatorRegistrationConfigurationProperties.class)
public class ElevatorSimulationConfiguration {
   /*
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ElevatorSimulation elevatorSimulation(
      IEmulatorControl emulatedControl, IElevatorGroupControl groupControl, IRuntimeEventBus eventBus )
   {
      return new ElevatorSimulation(emulatedControl, groupControl, eventBus);
   }
  
   @Bean
   @Lazy
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   SimulationEcosystem simulatedTravellerGenerator(
      List<ISimulatedPopulation> travellerSources, IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus) 
   {
      return new SimulationEcosystem(travellerSources, clock, scheduler, eventBus);
   }
   */
}
