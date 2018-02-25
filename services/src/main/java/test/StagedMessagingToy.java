package test;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import info.jchein.mesosphere.elevator.runtime.event.EventBusAdapter;
import info.jchein.mesosphere.elevator.runtime.event.EventBusEmitter;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.event.RuntimeEventBus;
import lombok.Getter;
import lombok.ToString;
import rx.Emitter;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;


public class StagedMessagingToy
{
   public static class Producer
   implements Action0
   {
      private final String name;
      private final RuntimeEventBus bus;
      private final ExponentialDistribution expoDist;
      private final Worker worker;


      public Producer( String name, RuntimeEventBus bus, ExponentialDistribution expoDist, Worker worker )
      {
         this.name = name;
         this.bus = bus;
         this.expoDist = expoDist;
         this.worker = worker;
      }


      @Override
      public void call()
      {
         this.bus.post(
            new Message(
               this.name,
               Thread.currentThread()
                  .getId(),
               this.worker.now()));

         this.schedule();
      }


      public void schedule()
      {
         this.worker.schedule(this, Math.round(this.expoDist.sample()), TimeUnit.MILLISECONDS);
      }
   }


   @ToString
   public static class Message
   {
      @Getter
      private final String name;
      @Getter
      private final long threadId;
      @Getter
      private final long now;


      public Message( String name, long threadId, long now )
      {
         this.name = name;
         this.threadId = threadId;
         this.now = now;
      }
   }
   
   public static final class SnapCracklePopListener extends EventBusEmitter<Message> 
   {
      public SnapCracklePopListener( Emitter<Message> emitter, IRuntimeEventBus eventBus )
      {
         super(emitter, eventBus);
      }
      
      @Subscribe
      public void riceKrispies(Message event) {
         this.emit(event);
      }
   }



   public static void main(String[] args)
   {
      final TimeUnit unit = TimeUnit.MILLISECONDS;
      final EventBus eventBus = new EventBus();
      final RuntimeEventBus bus = new RuntimeEventBus(eventBus);
      final ExponentialDistribution expoDist = new ExponentialDistribution(1000, 0.0001);

      final ThreadFactory tf =
         new ThreadFactoryBuilder().setDaemon(false)
            .setNameFormat("Worker %d")
            .build();
      final Executor executor = Executors.newSingleThreadExecutor(tf);
      final Scheduler rxSched = Schedulers.from(executor);
      final TestScheduler testSched = Schedulers.test();
      final Worker testWorker = testSched.createWorker();
      final Worker rxWorker = rxSched.createWorker();

      final Producer[] producers =
         new Producer[]
         {
            new Producer("Fred", bus, expoDist, testWorker),
            new Producer("Willma", bus, expoDist, testWorker),
            new Producer("Barney", bus, expoDist, testWorker),
            new Producer("Betty", bus, expoDist, testWorker)
         };
      
      producers[0].schedule();
      producers[1].schedule();
      producers[2].schedule();
      producers[3].schedule();
      
      System.out.println("Bootstrap completed!");

      rxWorker.schedule(() -> {
         testSched.advanceTimeTo(3, TimeUnit.MINUTES);
      }, 5, TimeUnit.SECONDS);

      System.out.println("Advance scheduled!");

      EventBusAdapter<Message, SnapCracklePopListener> adapter =
         new EventBusAdapter<Message, SnapCracklePopListener>(bus, SnapCracklePopListener::new);
      adapter.toObservable()
         .observeOn(testSched)
         .rebatchRequests(10)
         .buffer(750, TimeUnit.MILLISECONDS, testSched)
         .toBlocking()
         .subscribe(System.out::println);

      System.out.println("Task completed!");
   }
}
