package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.common.PassengerId;

public interface ISimulatedTraveller  {
   public PassengerId getId();

   public double getWeight();

   public int getCurrentFloor();

   public int getDestinationFloor();

   public int getPickupCarIndex();

   public long getLatestCallTime();

   public long getLatestPickupTime();

   public long getLatestDropOffTime();

   void onQueuedForPickup();
   
   void onSuccessfulPickup(int boardedCarIndex);
   
   void onSuccessfulDropOff();
}
