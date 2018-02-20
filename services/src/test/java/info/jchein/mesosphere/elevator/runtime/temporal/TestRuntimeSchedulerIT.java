package info.jchein.mesosphere.elevator.runtime.temporal;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fixtures.elevator.runtime.temporal.QueuePackingIntervalHandler;
import info.jchein.mesosphere.elevator.runtime.temporal.VirtualScheduler;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.IVirtualRuntimeService;
import info.jchein.mesosphere.test.config.runtime.temporal.EnableTestVirtualRuntime;
import info.jchein.mesosphere.test.config.runtime.temporal.VirtualRuntimeTestConfiguration;
import rx.Scheduler;
import rx.schedulers.TestScheduler;


@EnableTestVirtualRuntime
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRuntimeSchedulerIT
{
   public static final long TICK_MILLIS_IN_CFG = 188;

   public static final long TWO_AND_HALF_TICKS = 470;
   
   public static final String LABEL_ONE = "Austin";
   public static final String LABEL_TWO = "Boston";
   public static final String LABEL_THREE = "Charleston";
   public static final String LABEL_FOUR = "Dublin";
   
   public static long INTERVAL_ONE = 250;
   public static long INTERVAL_TWO = 300;
   public static long INTERVAL_THREE = 1500;
   public static long INTERVAL_FOUR = 500;
   
   public static int PRIORITY_ONE = 20;
   public static int PRIORITY_TWO = 10;
   public static int PRIORITY_THREE = 30;
   public static int PRIORITY_FOUR = 20;

   @Autowired
   @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler scheduler;

   @Autowired
   VirtualScheduler sut;

   private LinkedList<String> queue;
   private QueuePackingIntervalHandler handler250;
   private QueuePackingIntervalHandler handler300;
   private QueuePackingIntervalHandler handler1500;
   private QueuePackingIntervalHandler handler500;

   @Before
   public void setup() {
        this.queue = new LinkedList<String>();
        this.handler250 = new QueuePackingIntervalHandler(queue, LABEL_ONE);
        this.handler300 = new QueuePackingIntervalHandler(queue, LABEL_TWO);
        this.handler1500 = new QueuePackingIntervalHandler(queue, LABEL_THREE);
        this.handler500 = new QueuePackingIntervalHandler(queue, LABEL_FOUR);
   }

   @Test
   public void whenInitialNow_thenZero()
   {
      this.sut.scheduleInterrupt(INTERVAL_ONE, TimeUnit.MILLISECONDS, PRIORITY_ONE, handler250);
      this.sut.scheduleInterrupt(INTERVAL_TWO, TimeUnit.MILLISECONDS, PRIORITY_TWO, handler250);
      this.sut.scheduleInterrupt(INTERVAL_THREE, TimeUnit.MILLISECONDS, PRIORITY_THREE, handler250);
      this.sut.scheduleInterrupt(INTERVAL_FOUR, TimeUnit.MILLISECONDS, PRIORITY_FOUR, handler250);
      
      this.scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
      printQueue(100);
      this.scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
      printQueue(200);
      this.scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
      printQueue(300);
      this.scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
      printQueue(400);
      this.scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
      printQueue(500);
      this.scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
      printQueue(1000);
      this.scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
      printQueue(1250);
      this.scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
      printQueue(1500);
   }

   private void printQueue(int ii)
   {
      System.out.println(String.format("Queue at %d\n%s\n", ii, this.queue.stream().collect( Collectors.joining(" -> "))));
   }
}
