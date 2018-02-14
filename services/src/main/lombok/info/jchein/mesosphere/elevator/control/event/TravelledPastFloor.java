package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class TravelledPastFloor implements ElevatorCarEvent {
   final EventType eventType = EventType.TRAVELLED_PAST_FLOOR;
   
	int carIndex;
	int floorIndex;
	DirectionOfTravel direction;
	
   public static TravelledPastFloor build(Consumer<TravelledPastFloorBuilder> director)
   {
      final TravelledPastFloorBuilder bldr = TravelledPastFloor.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public TravelledPastFloor copy(Consumer<TravelledPastFloorBuilder> director)
   {
      final TravelledPastFloorBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
