package info.jchein.mesosphere.elevator.runtime.event;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.runtime.IRuntime;
import rx.Observable;

@Configuration
@ComponentScan()
public class EventBusConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   public EventBus getGuavaEventBus() {
      return new EventBus();
   }

   @Bean
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   public <E> Observable<E> observableEventEmitter(IRuntimeEventBus eventBus) {
      return eventBus.<E>toObservable();
   }
}