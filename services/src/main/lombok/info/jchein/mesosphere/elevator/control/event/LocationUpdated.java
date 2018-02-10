package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder=true)
public class LocationUpdated implements ElevatorCarEvent {
	long clockTime;
	long carSequence;
	int carIndex;
	int floorIndex;
   
   public static LocationUpdated build(Consumer<LocationUpdatedBuilder> director)
   {
      final LocationUpdatedBuilder bldr = LocationUpdated.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public LocationUpdated copy(Consumer<LocationUpdatedBuilder> director)
   {
      final LocationUpdatedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
