package info.jchein.mesosphere.elevator.control.model;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import rx.Observable;

public interface IElevatorGroupControl
{
   Observable<ElevatorCarEvent> getChangeStream();
//   S getScheduler();
//   IRuntimeEventBus getEventBus();
//   IRuntimeClock getRuntimeClock();

}
