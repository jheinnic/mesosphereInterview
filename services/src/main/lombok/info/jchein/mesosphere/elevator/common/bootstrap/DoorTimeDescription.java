package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

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
