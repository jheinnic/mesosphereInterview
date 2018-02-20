package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.constraints.Min;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class BuildingDescription
{
   @Min(3)
   public int numFloors;
   @Min(1)
   public int numElevators;
   @Positive
   public double metersPerFloor;


   public static BuildingDescription build(Consumer<BuildingDescriptionBuilder> director)
   {
      BuildingDescriptionBuilder bldr = BuildingDescription.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public BuildingDescription copy(Consumer<BuildingDescriptionBuilder> director)
   {
      BuildingDescriptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
