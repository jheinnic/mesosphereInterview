package info.jchein.mesosphere.elevator.control;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;

public interface IPathNavigator
{
   void trackDropRequest(DropOffRequested dropOffRequested);
   void trackAssignedPickup(int floorIndex, DirectionOfTravel direction);
   void trackCanceledPickup(int floorIndex, DirectionOfTravel direction);
   
}
