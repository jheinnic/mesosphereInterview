package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class WeightLoadUpdated implements ElevatorCarEvent {
	long clockTime;
	long carSequence;
	int carIndex;
	double weightLoad;
	
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
