package info.jchein.mesosphere.elevator.common.evaluation;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PassengerExperience
{
   private final double callTime;
   private final double pickupTime;
   private final double dropOffTime;
   private final int callFloor;
   private final int dropFloor;
   private final int carIndex;
   private final double perfectionBaseline;
   private final double actualTripTime;
   private final double weightScore;
   private final int stopsEnRoute;
   private final double maxCarWeight;

   public static PassengerExperience build(Consumer<PassengerExperienceBuilder> director) {
      PassengerExperienceBuilder bldr = PassengerExperience.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public PassengerExperience copy(Consumer<PassengerExperienceBuilder> director) {
      PassengerExperienceBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
