package info.jchein.mesosphere.elevator.domain.scheduler;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

public interface ScheduledStop
{
   int getFloorIndex();
   boolean hasDirection();
   DirectionOfTravel getDirection();
}
