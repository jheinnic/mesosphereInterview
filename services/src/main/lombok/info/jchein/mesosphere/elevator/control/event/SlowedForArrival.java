package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class SlowedForArrival implements ElevatorCarEvent {
   final EventType eventType = EventType.SLOWED_FOR_ARRIVAL;
   
	int carIndex;
	int floorIndex;
	DirectionOfTravel direction;
	
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
