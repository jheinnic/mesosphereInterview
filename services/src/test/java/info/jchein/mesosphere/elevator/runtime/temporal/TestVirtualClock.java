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

import fixtures.elevator.runtime.temporal.EnableTestVirtualRuntime;
import info.jchein.mesosphere.elevator.runtime.IRuntime;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.IVirtualRuntimeService;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.RuntimeClock;
import rx.Scheduler;


@EnableTestVirtualRuntime
@RunWith(SpringJUnit4ClassRunner.class)
public class TestVirtualClock
{
   public static final long TICK_MILLIS_IN_CFG = 188;


   @Configuration
   @Import(VirtualClock.class)
   public static class TestVirtualClockConfiguration { }


   @SpyBean
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   Scheduler spyScheduler;

   @Autowired
   VirtualClock sut;


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
