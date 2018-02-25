//package test;
//
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.commons.math3.distribution.ExponentialDistribution;
//
//import com.google.common.eventbus.EventBus;
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//
//import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
//import info.jchein.mesosphere.elevator.runtime.event.RuntimeEventBus;
//import lombok.Getter;
//import lombok.ToString;
//import rx.Observable;
//import rx.Scheduler;
//import rx.Scheduler.Worker;
//import rx.Subscription;
//import rx.functions.Action0;
//import rx.schedulers.Schedulers;
//import rx.schedulers.TestScheduler;
//
//
//public class Throughput3
//{
//   public static class Producer
//   implements Action0
//   {
//      private final String name;
//      private final IRuntimeEventBus bus;
//      private final ExponentialDistribution expoDist;
//      private final Worker worker;
//      private final Snap snapMessage;
//      private final Crackle crackleMessage;
//      private final Pop popMessage;
//
//
//      public Producer( String name, IRuntimeEventBus bus, ExponentialDistribution expoDist,
//         Worker worker )
//      {
//         this.name = name;
//         this.bus = bus;
//         this.expoDist = expoDist;
//         this.worker = worker;
//         this.snapMessage =
//            new Snap(
//               this.name,
//               Thread.currentThread()
//                  .getId(),
//               this.worker.now());
//         this.crackleMessage =
//            new Crackle(
//               this.name,
//               Thread.currentThread()
//                  .getId(),
//               this.worker.now());
//         this.popMessage =
//            new Pop(
//               this.name,
//               Thread.currentThread()
//                  .getId(),
//               this.worker.now());
//      }
//
//
//      @Override
//      public void call()
//      {
//         this.bus.post(this.snapMessage);
//         this.bus.post(this.crackleMessage);
//         this.bus.post(this.popMessage);
//         this.schedule();
//      }
//
//
//      public void schedule()
//      {
//         this.worker.schedule(this, 250, TimeUnit.MILLISECONDS);
//         // this.worker.schedule(this);
//
//      }
//   }
//
//   public interface Message { }
//
//   @ToString
//   public static class Snap implements Message
//   {
//      @Getter
//      private final String name;
//      @Getter
//      private final long threadId;
//      @Getter
//      private final long now;
//
//
//      public Snap( String name, long threadId, long now )
//      {
//         this.name = String.format("Snap<%s>", name);
//         this.threadId = threadId;
//         this.now = now;
//      }
//   }
//
//
//   @ToString
//   public static class Crackle implements Message
//   {
//      @Getter
//      private final String name;
//      @Getter
//      private final long threadId;
//      @Getter
//      private final long now;
//
//
//      public Crackle( String name, long threadId, long now )
//      {
//         this.name = String.format("Crackle<%s>", name);
//         this.threadId = threadId;
//         this.now = now;
//      }
//   }
//
//
//   @ToString
//   public static class Pop implements Message
//   {
//      @Getter
//      private final String name;
//      @Getter
//      private final long threadId;
//      @Getter
//      private final long now;
//
//
//      public Pop( String name, long threadId, long now )
//      {
//         this.name = String.format("Pop<%s>", name);
//         this.threadId = threadId;
//         this.now = now;
//      }
//   }
//
//
//   public static void main(String[] args)
//   {
//      final TimeUnit unit = TimeUnit.MILLISECONDS;
//      final EventBus guavaBus = new EventBus();
//      final RuntimeEventBus bus = new RuntimeEventBus(guavaBus);
//      final ExponentialDistribution expoDist = new ExponentialDistribution(1000, 0.0001);
//      final ThreadFactory tf =
//         new ThreadFactoryBuilder().setDaemon(false)
//            .setNameFormat("Worker %d")
//            .build();
//      final Executor executor = Executors.newSingleThreadExecutor(tf);
//      final Scheduler rxSched = Schedulers.from(executor);
//      final TestScheduler testSched = Schedulers.test();
//      final Worker testWorker = testSched.createWorker();
//      final Worker rxWorker = rxSched.createWorker();
//
//      final Producer[] producers =
//         new Producer[]
//         {
//            new Producer("Fred", bus, expoDist, rxWorker),
//            new Producer("Willma", bus, expoDist, rxWorker),
//            new Producer("Barney", bus, expoDist, rxWorker),
//            new Producer("Betty", bus, expoDist, rxWorker)
//         };
//
//      System.out.println("Bootstrap completed!");
//
//      rxWorker.schedule(() -> {
//         // testSched.advanceTimeTo(0, TimeUnit.SECONDS);
//         producers[0].call();
//         producers[1].call();
//         producers[2].call();
//         producers[3].call();
//         // testSched.advanceTimeTo(2200, TimeUnit.MILLISECONDS);
//         // source.close();
//         // testSched.advanceTimeTo(4, TimeUnit.SECONDS);
//      }, 2, TimeUnit.SECONDS);
//
//      // System.out.println("Advance scheduled!");
//
//      // EventBusEmitter.<Pop> create(bus)
//      final Observable<Message> msgSource = bus.toObservable();
//      final Observable<Snap> snapSource = bus.toObservable();
//      final Observable<Crackle> crackleSource = bus.toObservable();
//      final Observable<Pop> popSource = bus.toObservable();
//      final Subscription subOne =
//         msgSource.window(500, TimeUnit.MILLISECONDS, rxSched)
//            .concatMap(window -> window.count())
//            // .toBlocking()
//            .subscribe(System.out::println);
//
//      rxWorker.schedule(() -> {
//         subOne.unsubscribe();
//      }, 4, TimeUnit.SECONDS);
//
//      final Subscription subTwo =
//         Observable.merge(crackleSource, popSource, snapSource).window(400, TimeUnit.MILLISECONDS, rxSched)
//            .concatMap(window -> window.count())
//            .subscribe(System.out::println);
//
//      rxWorker.schedule(() -> {
//         subTwo.unsubscribe();
//         synchronized (subTwo) {
//            subTwo.notify();
//         }
//      }, 3, TimeUnit.SECONDS);
//
//      synchronized (subTwo) {
//         try {
//            subTwo.wait();
//         }
//         catch (InterruptedException e) {
//            e.printStackTrace();
//         }
//      }
//
//      System.out.println("Task completed!");
//   }
//}
