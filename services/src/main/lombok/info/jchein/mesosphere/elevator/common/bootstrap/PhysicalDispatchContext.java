package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class PhysicalDispatchContext
{
   @Positive
   private final double idealWeightLoad;

   @Positive
   private final double passengerWeight;
   
   @Positive
   private final double pctMaxBoardingWeight;

   @Positive
   private final double minDoorHoldTimePerOpen;

   @Positive
   private final double doorHoldTimePerPerson;

   @Positive
   private final double doorOpenCloseSlideTime;
   
   public static PhysicalDispatchContext build(Consumer<PhysicalDispatchContextBuilder> director) {
      PhysicalDispatchContextBuilder bldr = PhysicalDispatchContext.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public PhysicalDispatchContext copy(Consumer<PhysicalDispatchContextBuilder> director) {
      PhysicalDispatchContextBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
