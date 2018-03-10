package info.jchein.mesosphere.elevator.control.manifest;

public interface TravellingNode
{
   TravelNodeType getNodeType();

   void printMe(double flowValue, TravellingNode edgeTarget);
}
