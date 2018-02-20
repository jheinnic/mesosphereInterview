package info.jchein.mesosphere.elevator.runtime;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public interface IRuntimeClock
{
   long now();

   long ticks();
   
   long getMillisecondsPerTick();
   
   Instant instant();
   
   ZoneId getZone();
   
   IRuntimeClock withZone(ZoneId zoneId);
   
   void advanceBy(long delta, TimeUnit timeUnit);
}
