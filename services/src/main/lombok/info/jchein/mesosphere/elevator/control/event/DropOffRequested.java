package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class DropOffRequested implements ElevatorCarEvent {
	long clockTime;
	long carSequence;
	int carIndex;
	int dropOffFloorIndex;
   
   public static DropOffRequested build(Consumer<DropOffRequestedBuilder> director)
   {
      final DropOffRequestedBuilder bldr = DropOffRequested.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DropOffRequested copy(Consumer<DropOffRequestedBuilder> director)
   {
      final DropOffRequestedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
