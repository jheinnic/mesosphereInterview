package info.jchein.mesosphere.elevator.runtime.event;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import rx.Emitter;
import rx.Emitter.BackpressureMode;
import rx.Observable;

@Primary
@Component
@ValidateOnExecution(type= {ExecutableType.ALL})
public class RuntimeEventBus implements IRuntimeEventBus 
{
   private final EventBus eventBus;
 
   @Autowired
   public RuntimeEventBus(@NotNull EventBus eventBus) {
      this.eventBus = eventBus;
   }

   @Override
   public void post(@NotNull Object event)
   {
      this.eventBus.post(event);
   }

   @Override
   public void registerListener(@NotNull Object listener)
   {
      this.eventBus.register(listener);
   }

   @Override
   public void unregisterListener(@NotNull Object listener)
   {
      this.eventBus.unregister(listener);
   }

   @Override
   public <E> Observable<E> toObservable()
   {
      return Observable.using(
         EventBusEmitter<E>::new,
         (emitter) -> Observable.create(emitter::attach, BackpressureMode.BUFFER),
         this.eventBus::unregister, true
      );
   }
   
   class EventBusEmitter<E>
   {
      private Emitter<E> emitter = null;
      private volatile boolean guard;

      void attach( final Emitter<E> emitter )
      {
         // TODO: This is not synchronized on premise that the thread performing an attach-inducing subscription is the same thread that will
         //       subsequently be used to emit events.  If that should not hold true (e.g. AsyncEventBus?) then the thread calling emit() may
         //       lack visibilty on the assigned emitter reference.  If this happens to be so, then add a volatile write.
         this.emitter = emitter;
         this.guard = true;
         RuntimeEventBus.this.eventBus.register(this);
      }

      @Subscribe
      public void emit(E event)
      {
         if (this.emitter != null || (this.guard && this.emitter != null)) {
            System.out.println("Emit: " + event.toString());
            this.emitter.onNext(event);
         } else {
            System.out.println("Bad Ju Ju");
         }
      }
   }
}
