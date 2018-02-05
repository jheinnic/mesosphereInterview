package info.jchein.mesosphere.elevator.runtime.virtual;

import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

//@Builder(toBuilder=true)
@Data
@Validated
@ConfigurationProperties("mesosphere.elevator.runtime")
public class VirtualRuntimeProperties
{
   @Min(10)
   final long tickDurationMillis;
   
   /*
   public static SystemRuntimeProperties build(Consumer<SystemRuntimePropertiesBuilder> director) {
      SystemRuntimePropertiesBuilder bldr = SystemRuntimeProperties.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public SystemRuntimeProperties copy(Consumer<SystemRuntimePropertiesBuilder> director) {
      SystemRuntimePropertiesBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
   */
}
