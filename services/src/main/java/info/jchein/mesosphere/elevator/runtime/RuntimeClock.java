package info.jchein.mesosphere.elevator.runtime;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import rx.Scheduler;
import rx.Scheduler.Worker;

public class RuntimeClock
extends Clock
implements IRuntimeClock
{
   private final ZoneId zoneId;
   @NotNull private final Scheduler authority;
   @NotNull private final long millisPerTick;

   RuntimeClock(ZoneId zoneId, @NotNull Scheduler authority, @Min(10) long millisPerTick) {
      this.zoneId = zoneId;
      this.authority = authority;
      this.millisPerTick = millisPerTick;
   }

   public RuntimeClock(ZoneId zoneId, @NotNull Scheduler authority, @NotNull SystemRuntimeProperties runtimeProps) {
      this.zoneId = zoneId;
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
