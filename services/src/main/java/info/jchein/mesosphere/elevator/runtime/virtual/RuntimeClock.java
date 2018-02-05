package info.jchein.mesosphere.elevator.runtime.virtual;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.virtual.VirtualRuntimeProperties;
import rx.Scheduler;

@Primary
@Component
public class RuntimeClock
extends Clock
implements IRuntimeClock
{
   private final ZoneId zoneId;
   @NotNull
   private final Scheduler authority;
   @NotNull
   private final long millisPerTick;


   RuntimeClock( ZoneId zoneId, @NotNull Scheduler authority, @Min(10) long millisPerTick )
   {
      this.zoneId = zoneId;
      this.authority = authority;
      this.millisPerTick = millisPerTick;
   }


   public RuntimeClock( ZoneId zoneId, @NotNull Scheduler authority,
      @NotNull VirtualRuntimeProperties runtimeProps )
   {
      this.zoneId = zoneId;
      this.authority = authority;
      this.millisPerTick = runtimeProps.getTickDurationMillis();
   }


   @Autowired
   public RuntimeClock(
      @NotNull @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER) Scheduler authority,
      @NotNull VirtualRuntimeProperties runtimeProps )
   {
      this.zoneId = ZoneId.systemDefault();
      this.authority = authority;
      this.millisPerTick = runtimeProps.getTickDurationMillis();
   }


   @Override
   public ZoneId getZone()
   {
      return this.zoneId;
   }


   @Override
   public RuntimeClock withZone(ZoneId zone)
   {
      return new RuntimeClock(zone, this.authority, this.millisPerTick);
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
      return this.millis();
   }


   @Override
   public long tickNow()
   {
      return Math.floorDiv(this.authority.now(), this.millisPerTick);
   }


   @Override
   public long getMillisecondsPerTick()
   {
      return this.millisPerTick;
   }

}
