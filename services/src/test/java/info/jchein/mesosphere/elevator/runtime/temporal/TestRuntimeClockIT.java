package info.jchein.mesosphere.elevator.runtime.temporal;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import info.jchein.mesosphere.elevator.runtime.IRuntime;
import info.jchein.mesosphere.test.config.runtime.temporal.EnableTestVirtualRuntime;
import rx.schedulers.TestScheduler;


@EnableTestVirtualRuntime
//@RunWith(Parameterized.class)
public class TestRuntimeClockIT
{
   @ClassRule
   public static final SpringClassRule springClassRule = new SpringClassRule();

   @Rule
   public final SpringMethodRule springMethodRule = new SpringMethodRule();

   public static final long TICK_MILLIS_IN_CFG = 188;

   public static final long FIVE_SECOND_NOW = 5000;
   public static final long FIVE_SECOND_MILLIS = 4888;
   public static final long FIVE_SECOND_TICKS = 26;

   public static final long TWO_AND_HALF_TICKS = 470;

   @Autowired
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler scheduler;

   @Autowired
   VirtualClock sut;


   @Test
   public void whenInitialNow_thenZero()
   {
      assertThat(
         sut.now()
      ).as("now")
         .isZero();
   }

   @Test
   public void whenAdvancePartialTick_thenNowAdvancePartialTicks()
   {
      scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
      assertThat(
         sut.now()
      ).as("Milliseconds to nearest tick modulo tick")
         .isEqualTo(FIVE_SECOND_NOW);
   }

   @Test
   public void whenAdvancePartialTick_thenMillisAdvanceNearestMultiple()
   {
      scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
      assertThat(
         sut.millis()
      ).as("Milliseconds to nearest tick modulo tick")
         .isEqualTo(FIVE_SECOND_MILLIS);
   }

   @Test
   public void whenAdvancePartialTick_thenTicksAdvanceNearestWhole()
   {
      scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
      assertThat(
         sut.ticks()
      ).as("Milliseconds to nearest tick modulo tick")
         .isEqualTo(FIVE_SECOND_TICKS);
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
   public void whenAdvancedSmallOver_thenClockTickAdvancedOne()
   {
      scheduler.advanceTimeTo(TICK_MILLIS_IN_CFG + 2, TimeUnit.MILLISECONDS);
      assertThat(
         sut.ticks()
      ).as("Just over one tick")
         .isEqualTo(1);
   }

   @Test
   public void whenAdvancedHalfOver_thenClockTickAdvancedOne()
   {
      scheduler.advanceTimeTo(TWO_AND_HALF_TICKS, TimeUnit.MILLISECONDS);
      assertThat(
         sut.ticks()
      ).as("2.5 ticks")
         .isEqualTo(2);
   }
}
