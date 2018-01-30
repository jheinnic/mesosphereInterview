package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupSnapshot;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import rx.Observable;
import rx.Scheduler;

public interface IElevatorGroupControl<S extends Scheduler> 
{
   Observable<ElevatorGroupSnapshot> getStates();
   S getScheduler();
   IRuntimeEventBus getEventBus();
   IRuntimeClock getRuntimeClock();
}
