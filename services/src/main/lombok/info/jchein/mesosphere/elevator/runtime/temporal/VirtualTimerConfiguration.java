package info.jchein.mesosphere.elevator.runtime.temporal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.runtime.IRuntime;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
@ComponentScan
public class VirtualTimerConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   Worker runtimeWorker() {
      return this.runtimeScheduler().createWorker();
   }
   
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler runtimeScheduler() {
      return Schedulers.test();
   }
}