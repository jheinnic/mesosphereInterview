package test;


import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jctools.queues.SpscLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.SneakyThrows;
import lombok.ToString;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;


public class Observabubble
{
   static final Logger log = LoggerFactory.getLogger(Observabubble.class);


   public static class Message
   {
      int orderId;
      Logger log;


      public Message( int orderId, Logger log )
      {
         this.orderId = orderId;
         this.log = log;
      }


      public void fire()
      {
         this.log.error(
            String.format(
               "Thread %d error on %d: %s",
               this.orderId,
               Thread.currentThread()
                  .getId(),
               Thread.currentThread()
                  .getName()));
      }
   }

   public static class RealDeal {
      @Subscribe
      void myDef(Message2 evt) {
         System.out.println("EVT EVT");
      }
   }

   public static Worker txWorker;
   
   public static void main(String[] args)
   {
      final ThreadFactory threadFactory =
         new ThreadFactoryBuilder().setNameFormat("RU %d")
            .build();
      ExecutorService t1 = Executors.newSingleThreadExecutor(threadFactory);
      ExecutorService t2 = Executors.newSingleThreadExecutor(threadFactory);
      ExecutorService t3 = Executors.newSingleThreadExecutor(threadFactory);
      ExecutorService t4 = Executors.newSingleThreadExecutor(threadFactory);

      Scheduler rxT2 = Schedulers.from(t2);
      Worker rxW2 = rxT2.createWorker();
      
      txWorker = Schedulers.from(t3).createWorker();
      
      Observabubble[] INSTANCES = new Observabubble[] {
         new Observabubble( txWorker, eventBus, new RealDeal() ), new Observabubble( txWorker, eventBus, new RealDeal() ),
         new Observabubble( txWorker, eventBus, new RealDeal() ), new Observabubble( txWorker, eventBus, new RealDeal() ),
         new Observabubble( txWorker, eventBus, new RealDeal() ), new Observabubble( txWorker, eventBus, new RealDeal() ),
         new Observabubble( txWorker, eventBus, new RealDeal() ), new Observabubble( txWorker, eventBus, new RealDeal() )
      };

      eventBus.register(INSTANCES[0]);
      eventBus.register(INSTANCES[4]);
      eventBus.register(INSTANCES[2]);
      eventBus.register(INSTANCES[6]);
      
      eventBus.post(new Message2("Baseline", -1));

      Observable<Message> src = Observable.<Message> create((emit) -> {
         emit.onNext(new Message(1, Observabubble.log));
         emit.onNext(new Message(77, Observabubble.log));
      }, BackpressureMode.BUFFER);
      
      src.<Integer, String> zipWith(
         Observable.from(new Integer[]
         {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
         }),
         (msg, val) -> {
            return String.format("%s::%d", msg, val);
         })
         .subscribeOn(rxT2)
         .subscribe(inout -> { System.out.println(inout); });
   }
   
   public static void ddd() {
      final ThreadFactory threadFactory =
         new ThreadFactoryBuilder().setNameFormat("RU %d")
            .build();
      ExecutorService t2 = Executors.newSingleThreadExecutor(threadFactory);
      ExecutorService t3 = Executors.newSingleThreadExecutor(threadFactory);
      ExecutorService t4 = Executors.newSingleThreadExecutor(threadFactory);

      Scheduler rxT2 = Schedulers.from(t2);
      Worker rxW2 = rxT2.createWorker();
      
      AsyncEventBus abus = new AsyncEventBus("Short Bus", t4);
      SpscLinkedQueue<Message> queueOut = new SpscLinkedQueue<Message>();

      Observable<Message> src = Observable.<Message> create((emit) -> {
         emit.onNext(new Message(1, Observabubble.log));
         emit.onNext(new Message(77, Observabubble.log));
      }, BackpressureMode.BUFFER);
      
      src.<Integer, String> zipWith(
         Observable.from(new Integer[]
         {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
         }),
         (msg, val) -> {
            return String.format("%s::%d", msg, val);
         })
         .subscribeOn(rxT2)
         .subscribe(inout -> { System.out.println(inout); });
//         .repeatWhen(strm -> {
//            strm.subscribe(foo -> {
//               System.out.println(String.format("Foo, %s", foo));
//            });
//         });
   }
   
   public static final EventBus eventBus = new EventBus();

   private SpscLinkedQueue<Message2> queue;
   private SecureRandom random;
   private Worker rxWorker;
   private Object realDeal;
   private EventBus myEventBus;
   private long myId;
   private boolean scheduled;
   private Message2 nextMessage;
   private final EventBus sharedBus;
   
   @SneakyThrows
   public Observabubble(Worker rxWorker, EventBus sharedBus, Object realDeal) {
      this.sharedBus = sharedBus;
      this.queue = new SpscLinkedQueue<Message2>();
      this.random = SecureRandom.getInstanceStrong();
      this.myId = this.random.nextLong();
      this.rxWorker = rxWorker;
      this.realDeal = realDeal;
      this.myEventBus = new EventBus();
      this.myEventBus.register(realDeal);
      this.scheduled = false;
   }
   
   @ToString
   public static class Message2 {
      private final ArrayList<String> messageList;
      private final String baseline;
      private final long id;

      public Message2(String baseline, long id) {
         this.baseline = baseline;
         this.id = id;
         this.messageList = new ArrayList<>(4);
         this.messageList.add(
            String.format("Text: %s from %d on %d", baseline, id, Thread.currentThread().getId()));
      }
      
      public Message2(Message2 src) {
         this.messageList = new ArrayList<String>(src.messageList);
         this.baseline = src.baseline;
         this.id = src.id;
         this.messageList.add(
            String.format("Text: %s from %d on %d", baseline, id, Thread.currentThread().getId()));
      }
      
      public boolean limit() {
         return this.messageList.size() < 4;
      }
   }
   
   @Subscribe
   void onMessage(Message2 msg) {
      // Observabubble target = INSTANCES[this.random.nextInt() % INSTANCES.length];
      if (! this.scheduled) {
         this.scheduled = true;
         this.queue.offer(msg);
         this.rxWorker.schedule(() -> {
            this.proceed();
         }, 1, TimeUnit.SECONDS);
      }
   }
   
   void proceed() {
      this.queue.drain(msg -> {
         this.nextMessage = msg;
         this.myEventBus.post(msg);
      });
      this.onMessage(this.nextMessage);
      this.scheduled = false;
      
      if (! this.nextMessage.limit()) {
         this.sharedBus.post(new Message2(this.nextMessage));
      } else {
         System.out.println(
            this.nextMessage.toString()
         );
      }
   }
}
