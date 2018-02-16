package info.jchein.mesosphere.elevator.common;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class PendingDropOff
{
   private static final Interner<PendingDropOff> INTERN_CACHE = Interners.newWeakInterner();

   @Valid
   @NotNull
   public final PassengerId id;
   
   @Min(0)
   public final long callTime;

   @Min(0)
   public final long pickupTime;
   
   @Min(0)
   public final int carIndex;

   @Min(0)
   public final int pickupFloor;

   @Min(0)
   public final int dropOffFloor;

   @Positive
   public final double weight;


   public static PendingDropOff build(Consumer<PendingDropOffBuilder> director)
   {
      PendingDropOffBuilder bldr = PendingDropOff.builder();
      director.accept(bldr);
      final PendingDropOff retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }


   public PendingDropOff copy(Consumer<PendingDropOffBuilder> director)
   {
      PendingDropOffBuilder bldr = this.toBuilder();
      director.accept(bldr);
      final PendingDropOff retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }


   public CompletedTrip completeAt(long dropOffTime) {
       return CompletedTrip.build(bldr -> {
          bldr.id(this.id)
          .callTime(this.callTime)
          .pickupTime(this.pickupTime)
          .dropOffTime(dropOffTime)
          .pickupFloor(this.pickupFloor)
          .dropOffFloor(this.dropOffFloor)
          .carIndex(this.carIndex);
       });
   }
}
