package info.jchein.mesosphere.elevator.runtime.temporal;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeDescription;
import info.jchein.mesosphere.elevator.runtime.IRuntime;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import lombok.SneakyThrows;
import rx.Scheduler;
import rx.schedulers.TestScheduler;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class VirtualClock
extends Clock
implements IRuntimeClock
{
   @NotNull
   private final ZoneId zoneId;

   @NotNull
   private final Scheduler authority;

   @NotNull
   private final long millisPerTick;


   VirtualClock( @NotNull ZoneId zoneId, @NotNull Scheduler authority, @Min(10) long millisPerTick )
   {
      this.zoneId = zoneId;
      this.authority = authority;
      this.millisPerTick = millisPerTick;
   }


   @Autowired
   public VirtualClock( @NotNull @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER) Scheduler authority,
      @NotNull VirtualRuntimeDescription runtimeProps )
   {
      this(ZoneId.systemDefault(), authority, runtimeProps.getTickDurationMillis());
   }


   @Override
   public ZoneId getZone()
   {
      return this.zoneId;
   }


   @Override
   public VirtualClock withZone(ZoneId zone)
   {
      return new VirtualClock(zone, this.authority, this.millisPerTick);
   }


   @Override
   public Instant instant()
   {
      return Instant.ofEpochMilli(this.millis());
   }


   @Override
   public long millis()
   {
      final long millis = this.authority.now();
      return millis - Math.floorMod(millis, this.millisPerTick);
   }


   @Override
   public long now()
   {
      return this.authority.now();
   }


   @Override
   public long ticks()
   {
      return Math.floorDiv(this.authority.now(), this.millisPerTick);
   }


   @Override
   public long getMillisecondsPerTick()
   {
      return this.millisPerTick;
   }


   @Override
   @SneakyThrows
   public void advanceBy(long delta, TimeUnit timeUnit)
   {
      if (this.authority instanceof TestScheduler) {
          ((TestScheduler) this.authority).advanceTimeBy(delta, timeUnit);
      } else {
         Thread.sleep(timeUnit.toMillis(delta));
      }
   }
}
