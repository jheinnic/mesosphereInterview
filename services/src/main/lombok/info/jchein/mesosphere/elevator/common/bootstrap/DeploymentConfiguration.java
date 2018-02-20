package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


/**
 * Mutable view of elevator deployment definition state, implemented for reuse by Spring's ConfigurationProperties
 * feature set. Not intended for reuse outside of {@link Configuration} annotated classes, where it is intended to be
 * converted to an immutable variant by invocation providing it as argument to
 * {@link EmulatorConfiguration#toImmutable(EmulatorProperties)}, which returns its immutable equivalent.
 * 
 * @author jheinnic
 *
 */
@Validated
@Getter @FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE) @AllArgsConstructor @ToString @EqualsAndHashCode
@Builder(toBuilder = true)
public class DeploymentConfiguration
{
   @Valid
   final BuildingDescription building;

   @Valid
   final TravelSpeedDescription topSpeed;

   @Valid
   final StartStopDescription motor;

   @Valid
   final WeightDescription weight;

   @Valid
   final DoorTimeDescription doors;
   
   @NotBlank
   @NotNull
   String carDriverKey;


   public static DeploymentConfiguration build(Consumer<DeploymentConfigurationBuilder> director)
   {
      DeploymentConfigurationBuilder bldr = DeploymentConfiguration.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DeploymentConfiguration copy(Consumer<DeploymentConfigurationBuilder> director)
   {
      DeploymentConfigurationBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
