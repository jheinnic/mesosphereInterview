package info.jchein.mesosphere.domain.clock;


import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.configuration.properties.SystemRuntimeProperties;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscription;
import rx.functions.Action0;


public class SystemClock
implements IClock
{
   private static final Logger LOG = LoggerFactory.getLogger(SystemClock.class);

   private final long tickMillis;
   private final Worker worker;
   private final FibonacciHeap<ScheduledCallback<?>> interruptHeap;
   // private final ArrayList<FibonacciHeapNode<ScheduledCallback<?>>> allInterrupts;

   private Subscription interruptsSubscription;


   // private Subscription clockEventSubscription;

   enum RegistrationType
   {
      PERIODIC_FIXED,
      PERIODIC_VARIABLE,
      ONE_TIME
   }


   public static final int HI_PRIORITY = Integer.MIN_VALUE;
   public static final int LOW_PRIORITY = Integer.MAX_VALUE;
   public static final int DEFAULT_PRIORITY = 0;


   abstract class ScheduledCallback<T>
   implements Comparable<ScheduledCallback<?>>
   {
      protected final T callback;
      private final int priority;

      // Each iteration we mutate this interval field to compensate for clock skew
      // based on whether the clock tick where the interrupt actually fires happens
      // earlier or later than its ideal target time that would have been a perfect
      // multiple of the tick interval. Recurring schedules also have a next expected
      // interval value that would be unnecessary for one-time scheduling.
      protected long previousActualInterval;

      protected ScheduledCallback( T callback, int priority )
      {
         this.callback = callback;
         this.priority = priority;
         this.previousActualInterval = 0;
      }


      protected ScheduledCallback( T callback )
      {
         this(callback, DEFAULT_PRIORITY);
      }

      abstract RegistrationType getType();

      abstract void adjustForClockSkew(long clockSkew);
      
      abstract void fireCallback(long timeNow);

      @Override
      public int compareTo(ScheduledCallback<?> o)
      {
         return this.priority - o.priority;
      }
   }


   abstract class PeriodicInterrupt<T>
   extends ScheduledCallback<T>
   {
      protected long nextExpectedInterval;
      private FibonacciHeapNode<ScheduledCallback<?>> node;

      PeriodicInterrupt( T callback, long nextExpectedInterval, int priority )
      {
         super(callback, priority);
         this.nextExpectedInterval = nextExpectedInterval;
      }

      long getNextExpectedInterval() {
         return this.nextExpectedInterval;
      }

      FibonacciHeapNode<ScheduledCallback<?>> getNode() {
         return this.node;
      }
      
      void setNode(FibonacciHeapNode<ScheduledCallback<?>> node) {
         this.node = node;
      }

      protected void reschedule(long timeNow)
      {
         SystemClock.this.interruptHeap.insert(this.node, timeNow + this.nextExpectedInterval);
      }
   }


   class PeriodicFixedInterrupt
   extends PeriodicInterrupt<IInterruptHandler>
   {
      private final long fixedTargetInterval;

      PeriodicFixedInterrupt( IInterruptHandler callback, long fixedTargetInterval, int priority )
      {
         super(callback, fixedTargetInterval, priority);
         this.fixedTargetInterval = fixedTargetInterval;
      }

      @Override
      RegistrationType getType()
      {
         return RegistrationType.PERIODIC_FIXED;
      }

      @Override
      void adjustForClockSkew(long clockSkew)
      {
         this.previousActualInterval = this.nextExpectedInterval - clockSkew;
         this.nextExpectedInterval = this.fixedTargetInterval + clockSkew;
      }

      long getFixedTargetInterval()
      {
         return this.fixedTargetInterval;
      }
      
      void fireCallback(long timeNow) {
         this.callback.call(this.previousActualInterval);
         this.reschedule(timeNow);
      }
   }


   class PeriodicVariableInterrupt extends PeriodicInterrupt<IRepeatIntervalFunction>
   {
      private long nextExpectedInterval;

      PeriodicVariableInterrupt( IRepeatIntervalFunction callback, long firstInterval, int priority )
      {
         super(callback, firstInterval, priority);
         this.nextExpectedInterval = firstInterval;
      }

      @Override
      RegistrationType getType()
      {
         return RegistrationType.PERIODIC_VARIABLE;
      }

      @Override
      void adjustForClockSkew(long clockSkew)
      {
         this.previousActualInterval = this.nextExpectedInterval - clockSkew;
         this.nextExpectedInterval = clockSkew;
      }

      void fireCallback(long timeNow)
      {
         this.nextExpectedInterval += this.callback.apply(this.previousActualInterval);
         this.reschedule(timeNow);
      }
   }


   class OneTimeCallback extends ScheduledCallback<IInterruptHandler>
   {
      private final long fixedTargetInterval;

      OneTimeCallback( IInterruptHandler callback, long fixedTargetInterval, int priority )
      {
         super(callback, priority);
         this.fixedTargetInterval = fixedTargetInterval;
      }

      @Override
      RegistrationType getType()
      {
         return RegistrationType.ONE_TIME;
      }

      @Override
      void adjustForClockSkew(long clockSkew)
      {
         this.previousActualInterval = this.fixedTargetInterval + clockSkew;
      }

      void fireCallback(long timeNow)
      {
         this.callback.call(this.previousActualInterval);
      }
   }


   class ProcessRecurringInterrupts
   implements Action0
   {
      private long timeZero;
      private long timeNext;

      ProcessRecurringInterrupts()
      {
         this.timeZero = SystemClock.this.worker.now();
         this.timeNext = this.timeZero + SystemClock.this.tickMillis;
      }

      @Override
      public void call()
      {
         final long timeNow = SystemClock.this.worker.now();
         // final long deltaNow = timeNow - this.timeZero;

         final long nextTimeNext = this.timeNext + SystemClock.this.tickMillis;
         final long deltaNext = nextTimeNext - timeNow;
         final long clockSkew = deltaNext - SystemClock.this.tickMillis;

         // Proofcheck this alternate path yields same calculated value
         // final long clockSkew = this.timeNext - timeNow;
         Assert.isTrue(
            clockSkew == (this.timeNext - timeNow),
            "iterationError computed unexpected result");

         final ArrayList<ScheduledCallback<?>> callTargets = new ArrayList<>();
         FibonacciHeapNode<ScheduledCallback<?>> closestEvent = SystemClock.this.interruptHeap.min();
         while (closestEvent.getKey() < this.timeNext) {
            // Remove the interruptHeap entry for min registration since we now know it is in range.
            closestEvent = SystemClock.this.interruptHeap.removeMin();

            // Build a collection of registrations whose interrupts have just fired. Adjust previous expected interval
            // based on the adjustment just calculated, and also use it to set an expected value for next iteration.
            final ScheduledCallback<?> closestData = closestEvent.getData();
            callTargets.add(closestData);

            // Compensate for difference between when this latest clock tick was expected to fire and the time it
            // actually did fire. It is sufficient to examine skew for present clock tick only since we deducted
            // actual time spent from a calculated expected value in all each tick since last scheduling occurred.
            //
            // ** deltaNext > tickMillis implies we are in this iteration sooner than expected and therefore wish to
            // deduct that shortfall from the previous expectation and added it onto next expected interval.
            // ** deltaNext < tickMillis implies we are late in this iteration and therefore wish to deduct that lag
            // time from next interval's expected duration and transfer it by addition to previous expected value.
            closestData.adjustForClockSkew(clockSkew);
            closestEvent = SystemClock.this.interruptHeap.min();
         }

         // Call interrupt handler method for each ScheduledCallback<?> that exhausted its time span. Sort list by
         // priority order first.
         Collections.sort(callTargets);
         for (final ScheduledCallback<?> firedInterrupt : callTargets) {
            try {
               firedInterrupt.fireCallback(timeNow);
            }
            catch (Exception exp) {
               LOG.error("Unexpected exception thrown by registered interrupt handler discarded: %s", exp);
            }
         }

         // Reschedule this handler for interrupt handling's next clock tick.
         this.timeZero = timeNow;
         this.timeNext = nextTimeNext;
      }
   }


   @Autowired
   public SystemClock( SystemRuntimeProperties systemProps, @NotNull Scheduler scheduler )
   {
      this.tickMillis = Math.round(systemProps.getClockTickDuration() * 1000);
      this.worker = scheduler.createWorker();
      this.interruptHeap = new FibonacciHeap<ScheduledCallback<?>>();
      this.interruptsSubscription = null;
   }

   @Override
   public void scheduleInterrupt(@Min(1) long cycleInterval, @NotNull TimeUnit intervalUnit,
      int priority, @NotNull IInterruptHandler handler)
   {
      final long cycleMillis = checkInterval(cycleInterval, intervalUnit);
      
      this.doScheduleInterrupt(
         new PeriodicFixedInterrupt(handler, cycleMillis, priority));
   }

   @Override
   public void scheduleVariable(@Min(1) long firstInterval, @NotNull TimeUnit intervalUnit,
      int priority, @NotNull IRepeatIntervalFunction handler)
   {
      final long cycleMillis = checkInterval(firstInterval, intervalUnit);
      
      this.doScheduleInterrupt(
         new PeriodicVariableInterrupt(handler, cycleMillis, priority));
   }

   @Override
   public void scheduleOnce(@Min(1) long interval, @NotNull TimeUnit timeUnit,
      int priority, @NotNull IInterruptHandler handler)
   {
      final long millis = checkInterval(interval, timeUnit);
      
      this.doScheduleOnce(
         new OneTimeCallback(handler, millis, priority), millis);
   }

   @Override
   public void scheduleOnce(Action0 action, long interval, TimeUnit timeUnit)
   {
//      return this.worker.schedule(action, interval, timeUnit);
      this.scheduleOnce(interval, timeUnit, DEFAULT_PRIORITY, (delta) ->{ action.call(); });
   }
   
   private long checkInterval(long interval, TimeUnit intervalUnit) {
      final long millis = intervalUnit.toMillis(interval);

      Preconditions.checkArgument(millis >= this.tickMillis, "Cannot repeat on an interval shorter than tick duration");
      if ((millis % this.tickMillis) > 0) {
         LOG.warn("Interrupt cycle of %s is not an even multiple of the tick duration, %s", millis, this.tickMillis);
      }
      
      return millis;
   }

   private void doScheduleInterrupt(PeriodicInterrupt<?> registration)
   {
      final FibonacciHeapNode<ScheduledCallback<?>> node =
         new FibonacciHeapNode<ScheduledCallback<?>>(registration);
      registration.setNode(node);
      this.interruptHeap.insert(
         node, registration.getNextExpectedInterval());
   }

   private void doScheduleOnce(OneTimeCallback registration, long millis)
   {
      this.interruptHeap.insert(
         new FibonacciHeapNode<ScheduledCallback<?>>(registration), millis);
   }

   @Override
   public void begin()
   {
      Preconditions.checkState(
         this.interruptsSubscription == null,
         "SystemClock.begin() may only be called once");
      final ProcessRecurringInterrupts interruptAction = new ProcessRecurringInterrupts();

      this.interruptsSubscription =
         this.worker.schedulePeriodically(
            interruptAction,
            this.tickMillis,
            this.tickMillis,
            TimeUnit.MILLISECONDS);

//      this.clockEventSubscription = this.worker.schedulePeriodically(() -> {
//         this.eventBus.post(new SystemClockTick(this.worker.now()));
//      }, this.tickMillis, this.tickMillis, TimeUnit.MILLISECONDS);
   }


   @Override
   public long now()
   {
      return this.worker.now();
   }


   @Override
   public long getMillisecondsPerTick()
   {
      return this.tickMillis;
   }
}
