package info.jchein.mesosphere.elevator.runtime;

import java.util.function.Consumer;

import javax.validation.constraints.Min;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class SystemRuntimeProperties
{
   @Min(10)
   long tickDurationMillis;
   
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

}
