package info.jchein.mesosphere.elevator.runtime.event;


import org.jctools.queues.MessagePassingQueue.Consumer;
import org.jctools.queues.atomic.SpscLinkedAtomicQueue;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import rx.Observable;
import rx.observables.SyncOnSubscribe;


public class PollableEventQueue<E>
{
   private final SpscLinkedAtomicQueue<E> eventQueue;

   PollableEventQueue( ) { // RuntimeEventBus eventBus) {
      this.eventQueue = new SpscLinkedAtomicQueue<E>();
   }


   @Subscribe
   public void offer(E event)
   {
      this.eventQueue.offer(event);
   }


   void drainOne(Consumer<E> eventSink)
   {
      this.eventQueue.drain(eventSink, 1);
   }
   
   
   void clear()
   {
      this.eventQueue.clear();
   }


   public static <T> Observable<T> create(EventBus bus)
   {
      final SyncOnSubscribe<PollableEventQueue<T>, T> adapter =
         SyncOnSubscribe.<PollableEventQueue<T>, T> createSingleState(
            PollableEventQueue<T>::new,
            (queue, observer) -> { queue.drainOne(observer::onNext); },
            (queue) -> { queue.clear(); }
         );
      
      bus.register(adapter);
      return Observable.create(adapter);
   }
}
