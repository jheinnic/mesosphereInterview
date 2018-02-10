package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TravelSpeedDescription
{
   @Positive
   public double shortHop;
   @Positive
   public double longAscent;
   @Positive
   public double longDescent;

   public static TravelSpeedDescription build(Consumer<TravelSpeedDescriptionBuilder> director)
   {
      TravelSpeedDescriptionBuilder bldr = TravelSpeedDescription.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public TravelSpeedDescription copy(Consumer<TravelSpeedDescriptionBuilder> director)
   {
      TravelSpeedDescriptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
