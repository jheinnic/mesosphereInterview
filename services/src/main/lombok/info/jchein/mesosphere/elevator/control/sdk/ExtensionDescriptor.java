package info.jchein.mesosphere.elevator.control.sdk;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class ExtensionDescriptor
{
   public final String beanName;
   public final String lookupKey;
   public final ExtensionType extensionType;
   
   public static ExtensionDescriptor build(Consumer<ExtensionDescriptorBuilder> director) {
      ExtensionDescriptorBuilder bldr = ExtensionDescriptor.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public ExtensionDescriptor copy(Consumer<ExtensionDescriptorBuilder> director) {
      ExtensionDescriptorBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
