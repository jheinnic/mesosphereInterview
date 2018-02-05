package info.jchein.mesosphere.elevator.control.model;

import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap;

public interface IElevatorGroupControlCompiler 
{
   IElevatorGroupControl compileBootstrapData( ElevatorGroupBootstrap data );
}
