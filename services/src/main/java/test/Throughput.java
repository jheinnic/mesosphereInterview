package test;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.event.RuntimeEventBus;
import lombok.Getter;
import lombok.ToString;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscription;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;


public class Throughput
{
   public static class Producer
   implements Action0
   {
      private final String name;
      private final IRuntimeEventBus bus;
      private final ExponentialDistribution expoDist;
      private final Worker worker;
      private final Message message;


      public Producer( String name, IRuntimeEventBus bus, ExponentialDistribution expoDist,
         Worker worker )
      {
         this.name = name;
         this.bus = bus;
         this.expoDist = expoDist;
         this.worker = worker;
         this.message =
            new Message(
               this.name,
               Thread.currentThread()
                  .getId(),
               this.worker.now());
      }


      @Override
      public void call()
      {
         this.bus.post(this.message);
         this.schedule();
      }


      public void schedule()
      {
         this.worker.schedule(this, 250, TimeUnit.MILLISECONDS);
         // this.worker.schedule(this);

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


   public static void main(String[] args)
   {
      final TimeUnit unit = TimeUnit.MILLISECONDS;
      final EventBus guavaBus = new EventBus();
      final RuntimeEventBus bus = new RuntimeEventBus(guavaBus);
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
            new Producer("Fred", bus, expoDist, rxWorker),
            new Producer("Willma", bus, expoDist, rxWorker),
            new Producer("Barney", bus, expoDist, rxWorker),
            new Producer("Betty", bus, expoDist, rxWorker)
         };

      System.out.println("Bootstrap completed!");

      rxWorker.schedule(() -> {
         // testSched.advanceTimeTo(0, TimeUnit.SECONDS);
         producers[0].call();
         producers[1].call();
         producers[2].call();
         producers[3].call();
         // testSched.advanceTimeTo(2200, TimeUnit.MILLISECONDS);
         // source.close();
         // testSched.advanceTimeTo(4, TimeUnit.SECONDS);
      }, 2, TimeUnit.SECONDS);

      // System.out.println("Advance scheduled!");

      // EventBusEmitter.<Message> create(bus)
      final Observable<Message> source = bus.toObservable();
      final Subscription subOne =
         source.window(500, TimeUnit.MILLISECONDS, rxSched)
            .concatMap(window -> window.count())
            // .toBlocking()
            .subscribe(System.out::println);

      rxWorker.schedule(() -> {
         subOne.unsubscribe();
      }, 4, TimeUnit.SECONDS);

      final Subscription subTwo =
         source.window(400, TimeUnit.MILLISECONDS, rxSched)
            .concatMap(window -> window.count())
            .subscribe(System.out::println);

      rxWorker.schedule(() -> {
         subTwo.unsubscribe();
         synchronized (subTwo) {
            subTwo.notify();
         }
      }, 3, TimeUnit.SECONDS);

      synchronized (subTwo) {
         try {
            subTwo.wait();
         }
         catch (InterruptedException e) {
            e.printStackTrace();
         }
      }

      System.out.println("Task completed!");
   }
}
