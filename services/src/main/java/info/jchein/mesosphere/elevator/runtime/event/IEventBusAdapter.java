package info.jchein.mesosphere.elevator.runtime.event;

import rx.Observable;

public interface IEventBusAdapter<E>
{
   Observable<E> toObservable();
}
