package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import javax.validation.constraints.Min;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
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
