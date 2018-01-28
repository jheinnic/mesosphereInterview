package info.jchein.mesosphere.elevator.domain.sdk;

import java.util.BitSet;

import info.jchein.mesosphere.elevator.domain.dispatch.event.StopItineraryUpdated;
import rx.Observer;

public interface IElevatorCarDriver
{
   public void travelTo(int floorIndex);
   public void openDoors();
   
   public void bootstrap(Bootstrap callback);
   
   public interface Bootstrap {
      void initialize(int floorHeight, double weightLoad, BitSet dropRequests);
   }
}
