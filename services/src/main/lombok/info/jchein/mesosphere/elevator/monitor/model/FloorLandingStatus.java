package info.jchein.mesosphere.elevator.monitor.model;

import javax.validation.constraints.Min;
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
   @Min(0)
   private int floorIndex;
   
   @NotNull
   @Size(min=0)
   @Singular
   private ImmutableList<PendingPickup> upwardTravellers;

   @NotNull
   @Size(min=0)
   @Singular
   private ImmutableList<PendingPickup> downwardTravellers;
   
}
