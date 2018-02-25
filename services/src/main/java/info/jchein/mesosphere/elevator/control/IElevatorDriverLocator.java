package info.jchein.mesosphere.elevator.control;

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
}
