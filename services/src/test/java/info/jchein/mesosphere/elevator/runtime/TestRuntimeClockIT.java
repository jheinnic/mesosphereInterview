package info.jchein.mesosphere.elevator.runtime;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.jchein.mesosphere.configuartion.tests.EnableTestVirtualRuntime;
import info.jchein.mesosphere.elevator.runtime.virtual.IVirtualRuntimeService;
import info.jchein.mesosphere.elevator.runtime.virtual.RuntimeClock;
import rx.schedulers.TestScheduler;


@EnableTestVirtualRuntime
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRuntimeClockIT
{
   public static final long TICK_MILLIS_IN_CFG = 188;

   public static final long TWO_AND_HALF_TICKS = 470;

   @Autowired
   @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler scheduler;

   @Autowired
   RuntimeClock sut;


   @Test
   public void whenInitialNow_thenZero()
   {
      assertThat(
         sut.now()
      ).as("now")
         .isZero();
   }

   @Test
   public void whenTickNow_thenModuloTickZero()
   {
      scheduler.advanceTimeTo(5, TimeUnit.MILLISECONDS);
      assertThat(
         sut.now() % TICK_MILLIS_IN_CFG
      ).as("Milliseconds to nearest tick modulo tick")
         .isZero();
      assertThat(
         sut.tickNow() % TICK_MILLIS_IN_CFG
      ).as("Milliseconds to nearest tick modulo tick")
         .isZero();
   }

   @Test
   public void whenConfigMillisPerTick_thenGetMillisPerTick()
   {
      assertThat(
         sut.getMillisecondsPerTick()
      ).as("Milliseconds per tick")
         .isEqualTo(TICK_MILLIS_IN_CFG);
   }
   
   @Test
   public void whenSchedulerAdvanced_thenClockTickAdvanced()
   {
      scheduler.advanceTimeTo(TICK_MILLIS_IN_CFG + 2, TimeUnit.MILLISECONDS);
      assertThat(
         sut.tickNow()
      ).as("Just over one tick")
         .isEqualTo(1);

      scheduler.advanceTimeTo(TWO_AND_HALF_TICKS, TimeUnit.MILLISECONDS);
      assertThat(
         sut.tickNow()
      ).as("2.5 ticks")
         .isEqualTo(2);
   }
}
