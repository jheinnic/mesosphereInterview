package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class PickupCallAdded implements LandingEvent {
   final EventType eventType = EventType.PICKUP_CALL_ADDED;
   
	int floorIndex;
	DirectionOfTravel direction;
	
   public static PickupCallAdded build(Consumer<PickupCallAddedBuilder> director)
   {
      final PickupCallAddedBuilder bldr = PickupCallAdded.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public PickupCallAdded copy(Consumer<PickupCallAddedBuilder> director)
   {
      final PickupCallAddedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
