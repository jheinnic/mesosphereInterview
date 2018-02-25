package info.jchein.mesosphere.elevator.control.manifest.bad;

public interface TravellingNode
{
   TravelNodeType getNodeType();
   int getFromFloorIndex();
   int getToFloorIndex();
}
