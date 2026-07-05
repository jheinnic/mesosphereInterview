package info.jchein.mesosphere.elevator.runtime.event;


import rx.Emitter;


/**
 * Wrapper around a Guava EventBus and RxJava Emitter with the overlap of a single event subscriber method that shares its argument type
 * with the Emitter's generic type and emits events through that emitter when called.
 * 
 * This implementation is not designed with large scale concurrency in mind.  Its intended use case is unidirectional wait-free message passing
 * between a pair of threads interacting as producer and consumer with backpressure concerns handled in a black-box fashion.
 * 
 * This implementation does not currently allow the backpressure strategy to be chosen.  It has a fixed preference specified for buffering, which
 * RxJava does with an unbounded Single Producer Single Consumer (Spsc) queue.  Should use cases arise for exposing more than just the buffering
 * backpressure strategy, the only reason this was not done proactively was the lack of a use case.
 * 
 * If using EventBus with multiple producer threads, only consider this class if you have a way of routing all of its incoming messages through a 
 * single thread.  The simplest and most robust way to accomplish this is to use Guava's AsyncEventBus and to create the AsyncEventBus with a 
 * single threaded Executor.  A single threaded Executor is more appropriate than a fixed thread pool of size one, because the latter case will
 * not necessarily use the same thread at all times over its life span.
 * 
 * If you can select the Message class forwarded by an instance of this class such that only one thread provides instances of that message type,
 * even if other threads are generating different classes of messages, this satisfy the constraint, but it does involve relying on an unenforcable
 * constraint.  
 * 
 * The RxJava library's Emitter callback is what gives rise to the single producer restriction.  Its backpressure strategy for buffering uses a single
 * producer queue and its documentation prohibits concurrent push though an Emitter.  Should a multi-producer use case arise, the proposed next step 
 * would be to introduce a ThreadLocal around multiple Emitters with a merge operator interleaving the distinct per-thread streams produced by each
 * Thread's Emitter's Observable.
 * 
 * @author jheinnic
 *
 * @param <E>
 */
public abstract class EventBusEmitter<E>
{
   private Emitter<E> emitter;
   private IRuntimeEventBus eventBus;


   protected EventBusEmitter(Emitter<E> emitter, IRuntimeEventBus eventBus) { 
      this.emitter = emitter;
      this.eventBus = eventBus;
      System.out.println("Open");
   }

   protected void emit(E event)
   {
      this.emitter.onNext(event);
   }

   protected void done()
   {
      this.close();
      this.emitter.onCompleted();
      this.emitter = null;
   }
   
   protected void fail(Throwable err)
   {
      this.close();
      this.emitter.onError(err);
      this.emitter = null;
   }
   
   void close() {
      if (this.eventBus != null) {
         this.eventBus.unregisterListener(this);
         this.eventBus = null;
      }
   }
}
