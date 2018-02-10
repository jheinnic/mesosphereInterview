package info.jchein.mesosphere.elevator.emulator.model;


import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.control.model.EnableElevatorController;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionDescriptor;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionType;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriverFactory;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;


@Configuration
@EnableElevatorController
@EnableConfigurationProperties({
   EmulatorRegistrationConfigurationProperties.class, EmulatorProperties.class
})
public class EmulatedElevatorDriverConfiguration
{
   public static final String QUALIFIER_PREFIX = "mesosphere.elevator.emulator";

   public static final String CAR_DRIVER_FACTORY_BEAN_NAME =
      QUALIFIER_PREFIX + ".emulatedCarDriverFactory";


   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ExtensionDescriptor
   registerMesosphereElevatorEmulatorDriver(EmulatorRegistrationConfigurationProperties config)
   {
      return ExtensionDescriptor.build(bldr -> {
         bldr.beanName(CAR_DRIVER_FACTORY_BEAN_NAME)
            .lookupKey(config.getDriverAlias())
            .extensionType(ExtensionType.ELEVATOR_CAR_DRIVER);
      });
   }


   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public EmulatorConfiguration emulatorConfiguration(EmulatorProperties mutableProperties)
   {
      return EmulatorConfiguration.toImmutable(mutableProperties);
   }


   @Autowired
   @Bean(name = CAR_DRIVER_FACTORY_BEAN_NAME)
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   IElevatorCarDriverFactory
   emulatedCarDriverFactory(EmulatedElevator rootEmulator, IElevatorPhysicsService physicsService, EmulatorConfiguration configData)
   {
      final Iterator<InitialCarState> configIter =
         configData.getCars()
            .iterator();
      return new IElevatorCarDriverFactory() {

         @Override
         public IElevatorCarDriver allocateDriver(IElevatorCarPort port)
         {
            final InitialCarState data = configIter.next();
            return EmulatedElevatorDriverConfiguration.this
               .emulatedElevatorCar(port, physicsService, data);
         }
      };
   }


   @Bean
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   EmulatedElevatorCar emulatedElevatorCar(EmulatedElevatorCar elevator, IElevatorCarPort port, IElevatorPhysicsService physicsService, InitialCarState data)
   {
      return new EmulatedElevatorCar(elevator, port, physicsService, data);
   }

   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   ElevatorEmulator elevatorEmulator(
      IRuntimeScheduler scheduler, IRuntimeClock clock, IRuntimeEventBus eventBus, Collection<EmulatedElevatorCar> EmulatorConfiguration emulatorConfig) {
      return new 
   }
   /*
    * @Bean
    * 
    * @Scope(BeanDefinition.SCOPE_PROTOTYPE) SimulationFloor getSimulationFloor() { return new SimulationFloor(); }
    * 
    * @Bean
    * 
    * @Scope(BeanDefinition.SCOPE_SINGLETON) public SimulationScenario getSimulationScenario( IRuntimeClock clock,
    * IRuntimeEventBus eventBus, IRuntimeScheduler scheduler, ElevatorGroupBootstrap bldgProps ) { return new
    * SimulationScenario(clock, eventBus, scheduler, bldgProps); }
    */
}
