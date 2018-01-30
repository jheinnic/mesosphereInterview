package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap;
import rx.Scheduler;
//import rx.Single.Transformer;
import rx.Single;

public interface IElevatorGroupControlCompiler 
{
   <T extends Scheduler> Single<IElevatorGroupControl<T>> compileBootstrapData( ElevatorGroupBootstrap data, T scheduler );
}
