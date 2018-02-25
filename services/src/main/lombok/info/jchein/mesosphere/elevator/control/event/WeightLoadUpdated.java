package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class WeightLoadUpdated implements ElevatorCarEvent {
   final EventType eventType = EventType.UPDATED_WEIGHT_LOAD;
   
	int carIndex;
	double current;
	double delta;
	double previous;
	
   public static WeightLoadUpdated build(Consumer<WeightLoadUpdatedBuilder> director)
   {
      final WeightLoadUpdatedBuilder bldr = WeightLoadUpdated.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public WeightLoadUpdated copy(Consumer<WeightLoadUpdatedBuilder> director)
   {
      final WeightLoadUpdatedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
