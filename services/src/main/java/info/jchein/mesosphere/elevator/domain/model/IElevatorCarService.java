package info.jchein.mesosphere.elevator.domain.model;

import java.util.BitSet;
import java.util.function.Function;

import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.dispatch.event.StopItineraryUpdated;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;
import rx.Observable;
import rx.Observer;

public interface IElevatorCarService
{
   Observable<ElevatorCarSnapshot> bootstrapElevatorCar(double floorHeight, double weightLoad, BitSet floorStops, Observer<StopItineraryUpdated> portAdapter );
   
   // StatusSnapshot getStatusUpdate();
   
   // Subscription subscribe(Observer<StatusSnapshot>)
}
