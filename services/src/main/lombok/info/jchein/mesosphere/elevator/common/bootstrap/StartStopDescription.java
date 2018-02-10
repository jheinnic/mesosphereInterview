package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class StartStopDescription
{
   @Positive
   public double maxJerk;
   @Positive
   public double maxAcceleration;
   @Positive
   public double brakeSpeed;
   @Positive
   public double brakeDistance;

   public static StartStopDescription build(Consumer<StartStopDescriptionBuilder> director)
   {
      StartStopDescriptionBuilder bldr = StartStopDescription.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public StartStopDescription copy(Consumer<StartStopDescriptionBuilder> director)
   {
      StartStopDescriptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
