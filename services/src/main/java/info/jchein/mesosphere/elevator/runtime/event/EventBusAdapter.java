package info.jchein.mesosphere.elevator.runtime.event;


import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;


public class EventBusAdapter<E, S extends PollableEventQueue<E>> implements IEventBusAdapter<E>
{
   private final IRuntimeEventBus bus;
   private final Supplier<S> listenerFactory;
   private Observable<E> observable;

   public EventBusAdapter(IRuntimeEventBus bus, Supplier<S> listenerFactory)
   {
      this.bus = bus;
      this.listenerFactory = listenerFactory;
   }
   
   @PostConstruct
   void init() {
      this.observable = Observable.create(
         SyncOnSubscribe.<S, E> createSingleState(this::subscribeListener, this::observeOne, this::unsubscribeListener));
      this.bus.registerListener(this);
   }
   
   public Observable<E> toObservable() {
      if (this.observable == null) { this.init(); }
      return this.observable;
   }

   S subscribeListener()
   {
      final S retVal = this.listenerFactory.get();
      this.bus.registerListener(retVal);
      return retVal;
   }

   void observeOne(S queue, Observer<? super E> observer) {
      queue.drainOne(observer::onNext);
   }
   
   void unsubscribeListener(S listener)
   {
      this.bus.unregisterListener(listener);
      listener.clear();
   }
}
