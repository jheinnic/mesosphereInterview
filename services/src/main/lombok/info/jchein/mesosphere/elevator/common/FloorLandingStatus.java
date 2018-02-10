package info.jchein.mesosphere.elevator.common;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class FloorLandingStatus
{
   @NotNull
   @Size(min=0)
   @Singular
   private ImmutableList<PendingPickup> upwardTravellers;

   @NotNull
   @Size(min=0)
   @Singular
   private ImmutableList<PendingPickup> downwardTravellers;
   
}
