package info.jchein.mesosphere.elevator.emulator.model;


import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.InitialElevatorCarState;
import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.control.model.EnableElevatorController;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionDescriptor;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionType;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriverFactory;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.emulator.physics.IElevatorPhysicsService;


@Configuration
@EnableElevatorController
@EnableConfigurationProperties(EmulatorRegistrationConfigurationProperties.class)
public class EmulatedElevatorDriverConfiguration
{
   public static final String QUALIFIER_PREFIX = "mesosphere.elevator.emulator";

   public static final String CAR_DRIVER_FACTORY_BEAN_NAME = QUALIFIER_PREFIX + ".emulatedCarDriverFactory";


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


   @Autowired
   @Bean(name=CAR_DRIVER_FACTORY_BEAN_NAME)
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   IElevatorCarDriverFactory emulatedCarDriverFactory(
      IElevatorPhysicsService physicsService, ElevatorGroupBootstrap configData)
   {
      final Iterator<InitialElevatorCarState> configIter = configData.getCars().iterator();
      return new IElevatorCarDriverFactory() {
         
         @Override
         public IElevatorCarDriver allocateDriver(IElevatorCarPort port)
         {
            final InitialElevatorCarState data = configIter.next();
            return EmulatedElevatorDriverConfiguration.this.emulatedElevatorCar(port, physicsService, data);
         }
      };
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   EmulatedElevatorCar emulatedElevatorCar( IElevatorCarPort port, IElevatorPhysicsService physicsService, InitialElevatorCarState data) {
      return new EmulatedElevatorCar(port, physicsService, data);
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
