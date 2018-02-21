package info.jchein.mesosphere.elevator.control.model;

import java.util.function.Supplier;

import org.jctools.queues.atomic.SpscLinkedAtomicQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.statefulj.framework.core.StatefulFactory;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.control.event.ElevatorCarEventQueue;
import info.jchein.mesosphere.elevator.runtime.event.EventBusAdapter;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;

@Configuration
@ComponentScan({"info.jchein.mesosphere.elevator.control.model"})
public class ElevatorControlConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public static StatefulFactory statefulJFactory() {
      return new StatefulFactory();
   }
    
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ServiceLocatorFactoryBean dispatchStrategyLocator()
   {
      final ServiceLocatorFactoryBean slFactory = new ServiceLocatorFactoryBean();
      slFactory.setServiceLocatorInterface(IDispatchStrategyLocator.class);
      return slFactory;
   }
    
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ServiceLocatorFactoryBean elevatorCarDriverLocator()
   {
      final ServiceLocatorFactoryBean slFactory = new ServiceLocatorFactoryBean();
      slFactory.setServiceLocatorInterface(IElevatorDriverLocator.class);
      return slFactory;
   }
   
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public EventBusAdapter<ElevatorCarEvent, ElevatorCarEventQueue> eventBusAdapter(IRuntimeEventBus eventBus)
   {
      return new EventBusAdapter<ElevatorCarEvent, ElevatorCarEventQueue>(eventBus, elevatorCarEventQueueFactory());
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public Supplier<ElevatorCarEventQueue> elevatorCarEventQueueFactory()
   {
      return () -> {
         return new ElevatorCarEventQueue(
            new SpscLinkedAtomicQueue<ElevatorCarEvent>());
      };
   }
}