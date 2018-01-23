package info.jchein.mesosphere.elevator.domain.car.event;

import info.jchein.mesosphere.domain.event.IEvent;
import info.jchein.mesosphere.elevator.domain.car.event.CarEventType;

public interface CarEvent extends IEvent<CarEventType> {
    int getCarIndex();
    long getTimeIndex();
}
