package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class SlowedForArrival implements ElevatorCarEvent {
	long clockTime;
	long carSequence;
	int carIndex;
	int floorIndex;
	
   public static SlowedForArrival build(Consumer<SlowedForArrivalBuilder> director)
   {
      final SlowedForArrivalBuilder bldr = SlowedForArrival.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public SlowedForArrival copy(Consumer<SlowedForArrivalBuilder> director)
   {
      final SlowedForArrivalBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
