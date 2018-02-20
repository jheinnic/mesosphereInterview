package info.jchein.mesosphere.elevator.runtime.event;

import rx.Observable;

public interface IRuntimeEventBus
{
   void post(Object event);
   
   void registerListener(Object listener);
   
   void unregisterListener(Object listener);

   <E> Observable<E> toObservable();
}
