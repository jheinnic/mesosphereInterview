package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.demographic.AgeGroupWeightSample;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


/**
 * Mutable view of Emulator configuration state, implemented for reuse by Spring's ConfigurationProperties feature set.
 * Not intended for reuse outside of {@link Configuration} annotated classes, where it is intended to be converted to an
 * immutable variant by invocation providing it as argument to
 * {@link EmulatorConfiguration#toImmutable(DemographicConfiguration)}, which returns its immutable equivalent.
 * 
 * @author jheinnic
 */
@Validated
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class DemographicConfiguration
{
   @Size(min = 1)
   @Valid
   @NotNull
   @Singular()
   public final ImmutableList<AgeGroupWeightSample> maleWeightSamples;

   @Size(min = 1)
   @Valid
   @NotNull
   @Singular()
   public final ImmutableList<AgeGroupWeightSample> femaleWeightSamples;


   public static DemographicConfiguration build(Consumer<DemographicConfigurationBuilder> director)
   {
      final DemographicConfigurationBuilder bldr = DemographicConfiguration.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DemographicConfiguration copy(Consumer<DemographicConfigurationBuilder> director)
   {
      final DemographicConfigurationBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
