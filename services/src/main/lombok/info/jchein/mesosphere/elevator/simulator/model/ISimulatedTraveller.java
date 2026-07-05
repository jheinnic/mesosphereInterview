package info.jchein.mesosphere.elevator.simulator.model;

import info.jchein.mesosphere.elevator.common.PassengerId;

public interface ISimulatedTraveller  {
   public PassengerId getId();

   public String getPopulationName();
   
   public double getWeight();

   public int getCurrentFloor();

   public int getDestinationFloor();

   public int getPickupCarIndex();

   /*
   public long getLatestCallTime();

   public long getLatestPickupTime();

   public long getLatestDropOffTime();
   */

   void queueForPickup();
   
   void boardElevator(int boardedCarIndex);
   
   void disembarkElevator();
}
