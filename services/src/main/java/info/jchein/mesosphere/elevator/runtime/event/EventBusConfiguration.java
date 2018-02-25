package info.jchein.mesosphere.elevator.runtime.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.control.IElevatorCarScope;
import info.jchein.mesosphere.elevator.runtime.IRuntime;

@Configuration
//@ComponentScan
public class EventBusConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   public EventBus guavaEventBus() {
      return new EventBus();
   }

   
   @Bean
   @Primary
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public IRuntimeEventBus runtimeEventBus(@Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER) EventBus guavaEventBus)
   {
      return new RuntimeEventBus(guavaEventBus);
   }

   
   @Bean
//   @Scope(IElevatorCarScope.SCOPE_NAME)
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   @Qualifier(IElevatorCarScope.SCOPE_NAME)
   public EventBus carLocalGuavaEventBus() {
      return new EventBus();
   }
   
   
   @Bean(IElevatorCarScope.LOCAL_EVENT_BUS_NAME)
   @Autowired
   @Scope(IElevatorCarScope.SCOPE_NAME)
   @Qualifier(IElevatorCarScope.SCOPE_NAME)
   public IRuntimeEventBus carLocalRuntimeEventBus( @Qualifier(IElevatorCarScope.SCOPE_NAME) EventBus carLocalGuavaEventBus )
   {
      return new RuntimeEventBus(carLocalGuavaEventBus);
   }
   
  @Bean
  @Scope(BeanDefinition.SCOPE_SINGLETON)
  public ServiceLocatorFactoryBean eventBusLocator()
  {
     final ServiceLocatorFactoryBean slFactory = new ServiceLocatorFactoryBean();
     slFactory.setServiceLocatorInterface(IRuntimeEventBusLocator.class);
     return slFactory;
  }

//   @Bean
//   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//   public <E> Observable<E> observableEventEmitter(IRuntimeEventBus eventBus) {
//      return eventBus.<E>toObservable();
//   }
}