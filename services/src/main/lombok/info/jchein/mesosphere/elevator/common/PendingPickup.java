package info.jchein.mesosphere.elevator.common;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class PendingPickup
{
//   private static final Interner<PendingPickup> INTERN_CACHE = Interners.newWeakInterner();
   @Valid
   @NotNull
   public final PassengerId id;
   
   @Min(0)
   public final long callTime;

   @Min(0)
   public final int pickupFloor;

   @Min(0)
   public final int dropOffFloor;

   @Positive
   public final double weight;


   public static PendingPickup build(Consumer<PendingPickupBuilder> director)
   {
      final PendingPickupBuilder bldr = PendingPickup.builder();
      director.accept(bldr);
      final PendingPickup retVal = bldr.build();

      return retVal;
//      return INTERN_CACHE.intern(retVal);
   }


   public PendingPickup copy(Consumer<PendingPickupBuilder> director)
   {
      final PendingPickupBuilder bldr = this.toBuilder();
      director.accept(bldr);
      final PendingPickup retVal = bldr.build();

      return retVal;
//      return INTERN_CACHE.intern(retVal);
   }


   public PendingDropOff toDropRequest(long pickupTime, int pickupCarIndex)
   {
      return PendingDropOff.build(bldr -> {
         bldr.id(this.id)
            .callTime(this.callTime)
            .pickupTime(pickupTime)
            .pickupFloor(this.pickupFloor)
            .dropOffFloor(this.dropOffFloor)
            .weight(this.weight);
      });
   }
}
