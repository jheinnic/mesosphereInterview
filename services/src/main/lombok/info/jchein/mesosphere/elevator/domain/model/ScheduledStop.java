package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

public interface ScheduledStop
{
   int getFloorIndex();
   boolean hasDirection();
   DirectionOfTravel getDirection();
}