package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class ParkedAtLanding implements ElevatorCarEvent {
	long clockTime;
	long carSequence;
	int carIndex;
	int floorIndex;
   
   public static ParkedAtLanding build(Consumer<ParkedAtLandingBuilder> director)
   {
      final ParkedAtLandingBuilder bldr = ParkedAtLanding.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public ParkedAtLanding copy(Consumer<ParkedAtLandingBuilder> director)
   {
      final ParkedAtLandingBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
