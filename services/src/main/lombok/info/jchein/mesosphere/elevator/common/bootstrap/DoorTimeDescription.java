package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class DoorTimeDescription
{
   @Positive
   public double minHold;
   @Positive
   public double personHold;
   @Positive
   public double openCloseTime;
   
   public static DoorTimeDescription build(Consumer<DoorTimeDescriptionBuilder> director)
   {
      DoorTimeDescriptionBuilder bldr = DoorTimeDescription.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DoorTimeDescription copy(Consumer<DoorTimeDescriptionBuilder> director)
   {
      DoorTimeDescriptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
