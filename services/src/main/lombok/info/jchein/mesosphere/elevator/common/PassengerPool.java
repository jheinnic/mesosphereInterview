package info.jchein.mesosphere.elevator.common;

import java.util.function.Consumer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PassengerPool
{
   private static final Interner<PassengerPool> INTERN_CACHE = Interners.newWeakInterner();
   
   final int destinationFloor;
   
   @NotNull
   @Size(min=0)
   @Singular
   final ImmutableList<PendingDropOff> passengers;

   public static PassengerPool build(Consumer<PassengerPoolBuilder> director)
   {
      final PassengerPoolBuilder bldr = PassengerPool.builder();
      director.accept(bldr);
      final PassengerPool retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }

   public PassengerPool copy(Consumer<PassengerPoolBuilder> director)
   {
      final PassengerPoolBuilder bldr = this.toBuilder();
      director.accept(bldr);
      final PassengerPool retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }

}
