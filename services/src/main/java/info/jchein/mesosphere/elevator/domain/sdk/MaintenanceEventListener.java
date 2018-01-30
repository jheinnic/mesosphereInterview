package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.runtime.EventBusInterrupt.EventBusClockEvent;

public class MaintenanceEventListener
{
   public void assignPickupCall(PickupCallAdded callAdded)
   {
//      int carIndex = ElevatorGroup.this.scheduler.assignPickupCall(callAdded);
//      final IElevatorCar assignedCar = ElevatorGroup.this.carList.get(carIndex);
//      assignedCar.enqueuePickupRequest(callAdded.getFloorIndex(), callAdded.getDirection());
   }


   public void handleClockEvent(EventBusClockEvent clock)
   {
      System.out.println("Handled a clock!");
   }
}
