package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import org.springframework.validation.annotation.Validated;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


@Validated
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
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
