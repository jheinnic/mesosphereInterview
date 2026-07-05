package info.jchein.mesosphere.elevator.emulator.model;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;

public interface IEmulatorRoot
{
   public void onPathUpdated(JourneyArc travelArc, DirectionOfTravel nextDirection);
   
   public void onArrival(int floorIndex, DirectionOfTravel nextDirection);
}
