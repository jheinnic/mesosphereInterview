package info.jchein.mesosphere.elevator.common.demographic;

import java.util.function.Consumer;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class AgeGroupWeightSample
{
   @Min(18)
   public int minAge;
   
   @Max(100)
   public int maxAge;

   @Positive
   public double weightMean;
   
   @Positive
   public double weightStdDev;

   @Min(1)
   public int count;
   
   public static AgeGroupWeightSample build(Consumer<AgeGroupWeightSampleBuilder> director)
   {
      final AgeGroupWeightSampleBuilder bldr = AgeGroupWeightSample.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public AgeGroupWeightSample copy(Consumer<AgeGroupWeightSampleBuilder> director)
   {
      final AgeGroupWeightSampleBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
