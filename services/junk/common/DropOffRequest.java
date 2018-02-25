package info.jchein.mesosphere.elevator.common;


import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
class DropOffRequest
{
   private final long timeOfRequest;
   private final int floorIndex;


   public static DropOffRequest build(Consumer<DropOffRequestBuilder> director)
   {
      DropOffRequestBuilder bldr = DropOffRequest.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DropOffRequest copy(Consumer<DropOffRequestBuilder> director)
   {
      DropOffRequestBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
