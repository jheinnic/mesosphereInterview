package info.jchein.mesosphere.elevator.common;

import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CompletedTrip
{
   @Valid
   @NotNull
   public final PassengerId id;
   
   @Min(0)
   public long callTime;
   
   @Min(0)
   public long pickupTime;

   @Min(0)
   public long dropOffTime;

   @Min(0)
   public int pickupFloor;

   @Min(0)
   public int dropOffFloor;

   @Min(0)
   public int carIndex;


   public static CompletedTrip build(Consumer<CompletedTripBuilder> director)
   {
      final CompletedTripBuilder bldr = CompletedTrip.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public CompletedTrip copy(Consumer<CompletedTripBuilder> director)
   {
      final CompletedTripBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
