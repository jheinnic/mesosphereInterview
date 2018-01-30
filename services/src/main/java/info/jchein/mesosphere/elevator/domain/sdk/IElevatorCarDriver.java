package info.jchein.mesosphere.elevator.domain.sdk;

import java.util.BitSet;

public interface IElevatorCarDriver
{
   public void initialize(int floorHeight, double weightLoad, BitSet dropRequests);
   public void travelTo(int floorIndex);
   public void openDoors();
}
