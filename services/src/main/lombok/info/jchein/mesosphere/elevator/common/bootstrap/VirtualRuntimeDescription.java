package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


@Validated
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class VirtualRuntimeDescription
{
   @Min(10)
   public long tickDurationMillis;


   public static VirtualRuntimeDescription build(Consumer<VirtualRuntimeDescriptionBuilder> director)
   {
      final VirtualRuntimeDescriptionBuilder bldr = VirtualRuntimeDescription.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public VirtualRuntimeDescription copy(Consumer<VirtualRuntimeDescriptionBuilder> director)
   {
      final VirtualRuntimeDescriptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
