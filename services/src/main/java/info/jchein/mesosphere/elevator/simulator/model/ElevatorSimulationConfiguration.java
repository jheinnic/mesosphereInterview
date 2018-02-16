package info.jchein.mesosphere.elevator.simulator.model;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.control.model.IElevatorGroupControl;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorControl;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulationEcosystem;


@Configuration
@ComponentScan({"info.jchein.mesosphere.elevator.simulator.workloads", "info.jchein.mesosphere.elevator.simulator.model", "info.jchein.mesosphere.elevator.simulator.passengers"})
public class ElevatorSimulationConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ElevatorSimulation elevatorSimulation(
      IEmulatorControl emulatedControl, IElevatorGroupControl groupControl, IRuntimeEventBus eventBus )
//      Observable<PickupRequested> callRequestEvents,
//      Observable<PassengerDoorsOpened> doorsOpenedEvents,
//      Observable<PassengerDoorsClosed> doorsClosedEvents )
   {
      return new ElevatorSimulation(emulatedControl, groupControl, eventBus);
   }
   
//   @Bean
//   @Autowired
//   @Scope(BeanDefinition.SCOPE_SINGLETON)
//   @Qualifier(IElevatorSimulation.TRAVELLER_SOURCE_QUALIFIER)
//   EventBus travellerSourceGuavaEventBus() {
//      return new EventBus();
//   }
//
//   @Bean
//   @Autowired
//   @Scope(BeanDefinition.SCOPE_SINGLETON)
//   @Qualifier(IElevatorSimulation.TRAVELLER_SOURCE_QUALIFIER)
//   IRuntimeEventBus travellerSourceEventBus(@Qualifier(IElevatorSimulation.TRAVELLER_SOURCE_QUALIFIER) EventBus guavaEventBus) {
//      return new RuntimeEventBus(guavaEventBus);
//   }

   @Bean
   @Lazy
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   SimulationEcosystem simulatedTravellerGenerator(List<ISimulatedPopulation> travellerSources, IRuntimeScheduler scheduler) 
   {
      return new SimulationEcosystem(travellerSources, scheduler);
   }

//   @Bean
//   @Autowired
//   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//   <E> Observable<E> simulationEventObservable(@Qualifier(IElevatorSimulation.TRAVELLER_SOURCE_QUALIFIER) IRuntimeEventBus eventBus) {
//      return eventBus.<E> toObservable();
//   }
   
}
