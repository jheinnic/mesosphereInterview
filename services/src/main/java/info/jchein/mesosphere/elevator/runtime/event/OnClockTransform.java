package info.jchein.mesosphere.elevator.runtime.event;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEventQueue;
import rx.Observable;
import rx.Observable.Transformer;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OnClockTransform<T> implements Transformer<T, T>
{
   private RuntimeEventBus eventBus;
   private Supplier<ElevatorCarEventQueue> queueFactory;

   @Autowired
   public OnClockTransform(RuntimeEventBus eventBus, Supplier<ElevatorCarEventQueue> queueFactory)
   {
      this.eventBus = eventBus;
      this.queueFactory = queueFactory;
   }
   
   @Override
   public Observable<T> call(Observable<T> t)
   {
      return null;
   }

}
