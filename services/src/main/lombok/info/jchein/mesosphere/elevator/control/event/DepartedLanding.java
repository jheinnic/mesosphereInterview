package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class DepartedLanding implements ElevatorCarEvent {
	long clockTime;
	long carSequence;
	int carIndex;
	int origin;
	int destination;
	DirectionOfTravel direction;
	
   
   public static DepartedLanding build(Consumer<DepartedLandingBuilder> director)
   {
      final DepartedLandingBuilder bldr = DepartedLanding.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public DepartedLanding copy(Consumer<DepartedLandingBuilder> director)
   {
      final DepartedLandingBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
