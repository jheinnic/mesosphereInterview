package info.jchein.mesosphere.elevator.control;

import java.util.function.BiFunction;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.statefulj.framework.core.StatefulFactory;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.model.StatefulFSM;
import org.statefulj.fsm.TooBusyException;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.control.event.ElevatorCarEventEmitter;
import info.jchein.mesosphere.elevator.runtime.event.EventBusAdapter;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.event.RuntimeEventBus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rx.Emitter;

@Slf4j
@Configuration
@ComponentScan
public class ElevatorControlConfiguration {
   private final IRuntimeEventBus eventBus;
   
   @Autowired
   ElevatorControlConfiguration(@NotNull IRuntimeEventBus eventBus) {
      this.eventBus = eventBus;
   }

   
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
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public EventBusAdapter<ElevatorCarEvent, ElevatorCarEventEmitter> eventBusAdapter()
   {
      return new EventBusAdapter<ElevatorCarEvent, ElevatorCarEventEmitter>(this.eventBus, elevatorCarEventEmitterFactory());
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public BiFunction<Emitter<ElevatorCarEvent>, IRuntimeEventBus, ElevatorCarEventEmitter> elevatorCarEventEmitterFactory()
   {
      return (emitter, eventBus) -> {
         return new ElevatorCarEventEmitter(emitter, eventBus);
      };
   }
}