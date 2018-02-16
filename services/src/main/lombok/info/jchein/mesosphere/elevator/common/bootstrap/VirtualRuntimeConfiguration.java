package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import javax.validation.constraints.Min;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class VirtualRuntimeConfiguration
{
   @Min(10)
   public long tickDurationMillis;
   
   public static VirtualRuntimeConfiguration build(Consumer<VirtualRuntimeConfigurationBuilder> director)
   {
      final VirtualRuntimeConfigurationBuilder bldr = VirtualRuntimeConfiguration.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public VirtualRuntimeConfiguration copy(Consumer<VirtualRuntimeConfigurationBuilder> director)
   {
      final VirtualRuntimeConfigurationBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
