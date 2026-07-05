package info.jchein.mesosphere.elevator.runtime.event;


import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import rx.Emitter;
import rx.Emitter.BackpressureMode;
import rx.Observable;


public class EventBusAdapter<T, E extends EventBusEmitter<T>> implements IEventBusAdapter<T>
{
   private final IRuntimeEventBus bus;
   private final BiFunction<Emitter<T>, IRuntimeEventBus, E> listenerFactory;
   private Observable<T> observable;

   public EventBusAdapter(IRuntimeEventBus bus, BiFunction<Emitter<T>, IRuntimeEventBus, E> listenerFactory)
   {
      this.bus = bus;
      this.listenerFactory = listenerFactory;
   }
   
   @PostConstruct
   void init() {
      this.observable = Observable.create( (Emitter<T> emitter) -> {
         System.out.println("Create");
         E listener = this.listenerFactory.apply(emitter, bus);
         bus.registerListener(listener);
         emitter.setCancellation(listener::close);
      }, BackpressureMode.BUFFER);
   }
   
   public Observable<T> toObservable() {
      if (this.observable == null) { this.init(); }
      return this.observable;
   }
}