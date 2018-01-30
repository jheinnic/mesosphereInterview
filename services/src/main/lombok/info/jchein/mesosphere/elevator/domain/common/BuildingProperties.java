package info.jchein.mesosphere.elevator.domain.common;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;


@Data
@Builder(toBuilder=true)
public class BuildingProperties
{
   @Min(3)
   final int numFloors;

   @Min(1)
   final int numElevators;

   @Positive
   final double metersPerFloor;
   
   public static BuildingProperties build(Consumer<BuildingPropertiesBuilder> director) {
      BuildingPropertiesBuilder bldr = BuildingProperties.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public BuildingProperties copy(Consumer<BuildingPropertiesBuilder> director) {
      BuildingPropertiesBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
