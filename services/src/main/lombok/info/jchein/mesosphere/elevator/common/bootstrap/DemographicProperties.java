package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.LinkedList;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Data;


/**
 * Mutable view of Emulator configuration state, implemented for reuse by Spring's ConfigurationProperties feature set.  Not intended for reuse outside of
 * {@link Configuration} annotated classes, where it is intended to be converted to an immutable variant by invocation providing it as argument to
 * {@link EmulatorConfiguration#toImmutable(DemographicProperties)}, which returns its immutable equivalent.
 * 
 * @author jheinnic
 */
@Data
@Validated
@ConfigurationProperties("mesosphere.demographic")
public class DemographicProperties
{
   @Size(min=1)
   @Valid
   @NotNull
   public final LinkedList<AgeGroupWeightSample> maleWeightSamples = new LinkedList<>();

   @Size(min=1)
   @Valid
   @NotNull
   public final LinkedList<AgeGroupWeightSample> femaleWeightSamples = new LinkedList<>();

   
   @Data
   public static class AgeRange {
      @Min(18)
      public int min;
      
      @Max(100)
      public int max;
   }
   
   @Data
   public static class NormalDistribution {
      @Positive
      public double mean;
      
      @Positive
      public double stdDev;
   }

   @Data
   public static class AgeGroupWeightSample
   {
      @Valid
      public AgeRange age = new AgeRange();
      
      @Valid
      public NormalDistribution weight = new NormalDistribution();  

      @Min(1)
      public int count;
   }
}
