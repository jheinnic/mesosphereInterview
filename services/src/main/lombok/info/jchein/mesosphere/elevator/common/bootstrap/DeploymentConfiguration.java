package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Builder;
import lombok.Value;


/**
 * Mutable view of elevator deployment definition state, implemented for reuse by Spring's ConfigurationProperties
 * feature set. Not intended for reuse outside of {@link Configuration} annotated classes, where it is intended to be
 * converted to an immutable variant by invocation providing it as argument to
 * {@link EmulatorConfiguration#toImmutable(EmulatorProperties)}, which returns its immutable equivalent.
 * 
 * @author jheinnic
 *
 */
@Value
@Builder(toBuilder = true)
@Validated
public class DeploymentConfiguration
{
   @Valid
   public final BuildingDescription building;

   @Valid
   public final TravelSpeedDescription topSpeed;

   @Valid
   public final StartStopDescription motor;

   @Valid
   public final WeightDescription weight;

   @Valid
   public final DoorTimeDescription doors;
   
   @NotBlank
   @NotNull
   public String carDriverKey;


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
