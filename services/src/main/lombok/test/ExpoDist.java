package test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class ExpoDist
{

   public static void main(String[] args) throws InterruptedException
   {
      final TimeUnit unit = TimeUnit.MILLISECONDS;
      final AtomicInteger sequence = new AtomicInteger(0);
      final ExponentialDistribution expoDist = new ExponentialDistribution(1000, 0.0001);
      final ThreadFactory tf = new ThreadFactoryBuilder().setDaemon(false).setNameFormat("Worker %d").build();
      final Executor executor = Executors.newSingleThreadExecutor(tf);
      final Scheduler rxSched = Schedulers.from(executor);
      
      Observable.<Integer>fromCallable(sequence::incrementAndGet)
         .delaySubscription(() -> Observable.timer(Math.round(expoDist.sample()), unit))
         .startWith(0)
         .map(value -> new long[] {System.currentTimeMillis(), value.longValue()} )
         .repeatWhen((v) -> v, rxSched)
         .timeInterval()
         .subscribeOn(rxSched)
         .window(10)
         .concatMap(
            intervals -> intervals.reduce(new long[] {0, 0}, (values, next) -> {
               values[0] += next.getIntervalInMilliseconds();
               values[1] += 1;
               return values;
            }).map(values -> (0.01 * values[0] / values[1]))
         ).subscribe(System.out::println );
      
      // Thread.sleep(5000);
   }
}
