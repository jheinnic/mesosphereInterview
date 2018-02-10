package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.common.CompletedTrip;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.common.PendingDropOff;
import info.jchein.mesosphere.elevator.common.PendingPickup;

public interface ISimulatedTraveller {
   public PassengerId getId();

   public double getWeight();

   public int getCurrentFloor();

   public int getDestinationFloor();

   public int getBoardedCarIndex();

   public long getLatestCallTime();

   public long getLatestPickupTime();

   public long getLatestDropOffTime();

   void onQueuedForPickup();
   
   void onSuccessfulPickup(int boardedCarIndex);
   
   void onSuccessfulDropOff();
}
