package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.control.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.runtime.virtual.EventBusInterrupt.EventBusClockEvent;

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
