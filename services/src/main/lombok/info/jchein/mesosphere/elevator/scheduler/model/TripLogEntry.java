package info.jchein.mesosphere.elevator.scheduler.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TripLogEntry {
   long callTime;
   long boardingTime;
   int boardingFloorIndex;
   long dismebarkTime;
   int disembarkFloorIndex;
}
