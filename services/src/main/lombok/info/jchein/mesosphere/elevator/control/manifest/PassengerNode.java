package info.jchein.mesosphere.elevator.control.manifest;

public interface PassengerNode
{
   PassengerNodeType getNodeType();
   
   int getFloorIndex();

   void printMe(double flowValue, PassengerNode edgeTarget);
}
