package test;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Lombok;


public class TryForkJoinPool
{

   public static void main(String[] args)
   {
      @SuppressWarnings("serial")
      List<SomeClass> list = new ArrayList<SomeClass>() {
         {
            for (int ii = 0; ii < 50; ii++) {
               add(new SomeClass());
            }
         }
      };
      Stream<Long> stream1 =
         list.parallelStream()
            .map(TryForkJoinPool::veryLongProcessing);
      Stream<Long> stream2 =
         list.parallelStream()
            .map(TryForkJoinPool::veryLongProcessing);
      Stream<Long> stream3 =
         list.parallelStream()
            .map(TryForkJoinPool::veryLongProcessing);
      Callable<List<Long>> task1 = () -> stream1.map(item -> {
         System.out.println(
            Thread.currentThread()
               .getId());
         System.out.println(item);
         return item;
      })
         .collect(Collectors.toList());
      Callable<List<Long>> task3 = () -> stream3.map(item -> {
         System.out.println(
            Thread.currentThread()
               .getId());
         System.out.println(item);
         return item;
      })
         .collect(Collectors.toList());
      ForkJoinPool forkJoinPool = new ForkJoinPool(2);
      try {
         final ForkJoinTask<List<Long>> newList1 = forkJoinPool.submit(task1);
         System.out.println(stream2.map(item -> {
            System.out.println(
               Thread.currentThread().getId());
            System.out.println(item);
            return item;
         })
            .collect(Collectors.toList())
            .toString());
         final ForkJoinTask<List<Long>> newList3 = forkJoinPool.submit(task3);
         System.out.println(
            newList3.get().toString());
         System.out.println(
            newList1.get().toString());
      }
      catch (InterruptedException | ExecutionException e) {
         Lombok.sneakyThrow(e);
      }
   }


   static Long veryLongProcessing(SomeClass param)
   {
      return param.toValue();
   }


   static class SomeClass
   {
      static ThreadLocal<AtomicLong> tl = ThreadLocal.withInitial(() -> new AtomicLong());


      Long toValue()
      {
         // return tl.get()
         // .getAndIncrement();
         return Thread.currentThread()
            .getId() *
            100000 +
            tl.get()
               .getAndIncrement();
      }
   }
}
