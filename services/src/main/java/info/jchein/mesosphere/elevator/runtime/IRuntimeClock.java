package info.jchein.mesosphere.elevator.runtime;

import java.time.Instant;
import java.time.ZoneId;

public interface IRuntimeClock
{
   long now();

   long tickNow();
   
   long getMillisecondsPerTick();
   
   Instant instant();
   
   ZoneId getZone();
   
   IRuntimeClock withZone(ZoneId zoneId);
}
