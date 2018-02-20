package info.jchein.mesosphere.elevator.runtime.temporal;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.jchein.mesosphere.elevator.runtime.temporal.temporal.IVirtualRuntimeService;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.RuntimeClock;
import info.jchein.mesosphere.test.config.runtime.temporal.EnableTestVirtualRuntime;
import rx.Scheduler;


@EnableTestVirtualRuntime
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRuntimeClock
{
   public static final long TICK_MILLIS_IN_CFG = 188;


   @Configuration
   @Import(RuntimeClock.class)
   public static class TestRuntimeClockConfiguration
   {

   }


   @SpyBean
   @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER)
   Scheduler spyScheduler;

   @Autowired
   RuntimeClock sut;


   @Test
   public void whenTickNow_thenModuloTickZero()
   {
      assertThat(
         sut.ticks() % TICK_MILLIS_IN_CFG
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
}
