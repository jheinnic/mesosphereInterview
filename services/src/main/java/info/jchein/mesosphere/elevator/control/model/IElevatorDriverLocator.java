package info.jchein.mesosphere.elevator.control.model;

import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorGroupDriver;

public interface IElevatorDriverLocator
{
   IElevatorGroupDriver locateGroupDriver();

   /**
    * Retrieves car driver instance wired to its IElevatorCarPort.  Must be called from within an {@link IElevatorCarScope} context
    * 
    * @return
    */
   IElevatorCarDriver locateCarDriver();

   /**
    * Retrieves the core FSM for an elevator car using its {@link ElevatorGroupControl}-facing interface.  Must be called from within 
    * an {@link IElevatorCarScope} context.
    * 
    * @return
    */
   IElevatorCar locateCarPort();
}
