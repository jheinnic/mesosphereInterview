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
public class WeightDescription
{
   @Positive
   public double maxForTravel;
   @Positive
   public double pctMaxForPickup;
   @Positive
   public double pctMaxForIdeal;
   @Positive
   public double avgPassenger;


   public static WeightDescription build(Consumer<WeightDescriptionBuilder> director)
   {
      WeightDescriptionBuilder bldr = WeightDescription.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public WeightDescription copy(Consumer<WeightDescriptionBuilder> director)
   {
      WeightDescriptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
