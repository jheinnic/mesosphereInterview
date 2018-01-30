package info.jchein.mesosphere.elevator.domain.common;

import java.util.function.Consumer;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder=true)
public class ElevatorMotorProperties
{
   @Positive
   private final double brakingSpeed;
      
   @Positive
   private final double brakingDistance;

   @Positive
   private final double shortTravelSpeed;
      
   @Positive
   private final double longAscentSpeed;

   @Positive
   private final double longDescentSpeed;

   @Positive
   private final double maxAcceleration;
      
   @Positive
   private final double maxJerk;
   
   @Positive
   private final double maxWeightLoad;
   
   public static ElevatorMotorProperties build(Consumer<ElevatorMotorPropertiesBuilder> director) {
      ElevatorMotorPropertiesBuilder bldr = ElevatorMotorProperties.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public ElevatorMotorProperties copy(Consumer<ElevatorMotorPropertiesBuilder> director) {
      ElevatorMotorPropertiesBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
