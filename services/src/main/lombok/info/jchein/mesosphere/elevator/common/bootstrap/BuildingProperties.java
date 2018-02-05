package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.Value;


@Value
@Validated
@Builder(toBuilder=true)
public class BuildingProperties
{
   @Min(3)
   final int numFloors;

   @Min(1)
   final int numElevators;

   @Positive
   final double metersPerFloor;
   
   @NotBlank
   final String carDriverKey;
   
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
