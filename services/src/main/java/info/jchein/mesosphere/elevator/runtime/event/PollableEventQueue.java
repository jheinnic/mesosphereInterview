package info.jchein.mesosphere.elevator.runtime.event;


import org.jctools.queues.MessagePassingQueue.Consumer;
import org.jctools.queues.atomic.SpscLinkedAtomicQueue;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import rx.observables.SyncOnSubscribe;


/**
 * Backpressure-enabling queue wrapper, useful for creating Observable source sequences with {@link SyncOnSubscribe}.   It is intended
 * to be subclassed to add a {@link Subscribe}-annotated method that takes an argument of type {@link E} that delegates each call to 
 * {@link PollableEventQueue#offer(E)}.  When given a Supplier of such a subtype, the {@link EventBusAdapter} can then support
 * creation of an Observable that is fed by events of type {@code E} that are passed to its {@link EventBus#post} method.
 * 
 * @author jheinnic
 * @param <E> The type of the argument to a @Subscribe annotated method that calls {@link PollableEventQueue#offer(E)} each time it is called.
 *            Subclasses are required to provide such a method, although there is no way to enforce this contract with the compiler unfortunately...
 */
public abstract class PollableEventQueue<E>
{
   private final SpscLinkedAtomicQueue<E> eventQueue;

   protected PollableEventQueue(SpscLinkedAtomicQueue<E> eventQueue) 
   {
      this.eventQueue = eventQueue;
   }
   
   protected final void offer(E event)
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
}
