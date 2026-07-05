package info.jchein.mesosphere.elevator.control;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import rx.Observable;

public interface IElevatorGroupControl
{
   int getNumElevators();
   int getNumFloors();
   Observable<ElevatorCarEvent> getChangeStream();
//   S getScheduler();
//   IRuntimeEventBus getEventBus();
//   IRuntimeClock getRuntimeClock();

}
