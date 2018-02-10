package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class PickupCallRemoved implements LandingEvent {
	long clockTime;
	int floorIndex;
	long floorSequence;
	DirectionOfTravel direction;
	
   public static PickupCallRemoved build(Consumer<PickupCallRemovedBuilder> director)
   {
      final PickupCallRemovedBuilder bldr = PickupCallRemoved.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public PickupCallRemoved copy(Consumer<PickupCallRemovedBuilder> director)
   {
      final PickupCallRemovedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
