package info.jchein.mesosphere.elevator.common.bootstrap;


import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Data;

/**
 * Mutable view of elevator deployment definition state, implemented for reuse by Spring's ConfigurationProperties feature set.  Not intended for reuse 
 * outside of {@link Configuration} annotated classes, where it is intended to be converted to an immutable variant by invocation providing it as argument 
 * to {@link IConfigurationFactory#toImmutable(DeploymentProperties)}, which returns its immutable equivalent, {@link DeploymentConfiguration}
 * 
 * @author jheinnic
 *
 */
@Data
@Validated
@ConfigurationProperties("mesosphere.elevator")
public class DeploymentProperties
{
   @Valid
   public final BuildingProperties building = new BuildingProperties(); 

   @Valid
   public final TravelSpeedProperties topSpeed = new TravelSpeedProperties();

   @Valid
   public final StartStopProperties motor = new StartStopProperties();

   @Valid
   public final WeightProperties weight = new WeightProperties();

   @Valid
   public final DoorTimeProperties doors = new DoorTimeProperties();
   
   @NotBlank
   @NotNull
   public String carDriverKey = "emulator";

   @Data
   public static class StartStopProperties
   {
      @Positive
      public double maxJerk;
      @Positive
      public double maxAcceleration;
      @Positive
      public double brakeSpeed;
      @Positive
      public double brakeDistance;
   }


   @Data
   public static class TravelSpeedProperties
   {
      @Positive
      public double shortHop;
      @Positive
      public double longAscent;
      @Positive
      public double longDescent;
   }


   @Data
   public static class WeightProperties
   {
      @Positive
      public double maxForTravel;
      @Positive
      public double pctMaxForPickup;
      @Positive
      public double pctMaxForIdeal;
      @Positive
      public double avgPassenger;
   }


   @Data
   public static class BuildingProperties
   {
      @Min(3)
      public int numFloors;
      @Min(1)
      public int numElevators;
      @Positive
      public double metersPerFloor;
   }

   @Data
   public static class DoorTimeProperties {
      @Positive
      public double minHold;
      @Positive
      public double personHold;
      @Positive
      public double openCloseTime;
   }
}
