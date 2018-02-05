package info.jchein.mesosphere.elevator.control.model;

import info.jchein.mesosphere.elevator.common.Event;
import rx.Observable;

public interface IElevatorGroupControl
{
   Observable<Event> getChangeStream();
//   S getScheduler();
//   IRuntimeEventBus getEventBus();
//   IRuntimeClock getRuntimeClock();

}
