package info.jchein.mesosphere.elevator.runtime.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rx.Observable;
import rx.Observable.Transformer;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OnClockTransform<T> implements Transformer<T, T>
{

   private final IEventBusAdapter<T> eventBusAdapter;

   @Autowired
   public OnClockTransform(IEventBusAdapter<T> eventBusAdapter) 
   {
      this.eventBusAdapter = eventBusAdapter;
   }
   
   @Override
   public Observable<T> call(Observable<T> t)
   {
      return null;
   }

}
