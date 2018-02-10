package info.jchein.mesosphere.elevator.common;


import java.util.function.Consumer;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class FutureCall
{
   @NotNull
   PassengerId id;
   @Min(0)
   public long callTime;
   @Min(0)
   public int pickupFloor;
   @Min(0)
   public int dropOffFloor;
   @Positive
   public double weight;


   public static FutureCall build(Consumer<FutureCallBuilder> director)
   {
      final FutureCallBuilder bldr = FutureCall.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public FutureCall copy(Consumer<FutureCallBuilder> director)
   {
      final FutureCallBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }


   public PendingPickup arrive() {
       return PendingPickup.build(bldr -> {
          bldr.id(this.id)
          .callTime(this.callTime)
          .pickupFloor(this.pickupFloor)
          .dropOffFloor(this.dropOffFloor)
          .weight(weight);
       });
   }
}
