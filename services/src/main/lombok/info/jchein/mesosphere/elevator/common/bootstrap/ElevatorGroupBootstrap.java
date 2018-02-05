package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.InitialElevatorCarState;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;


@Data
@Builder(toBuilder=true)
@ConfigurationProperties("mesosphere.elevator")
public class ElevatorGroupBootstrap
{
   @Valid
   private final BuildingProperties building;
   
   @Valid
   private final ElevatorMotorProperties motor;

   @Valid
   private final PhysicalDispatchContext dispatch;
   
   @Valid
   @Singular
   ImmutableList<InitialElevatorCarState> cars;
   
   public static ElevatorGroupBootstrap build(Consumer<ElevatorGroupBootstrapBuilder> director) {
      ElevatorGroupBootstrapBuilder bldr = ElevatorGroupBootstrap.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public ElevatorGroupBootstrap copy(Consumer<ElevatorGroupBootstrapBuilder> director) {
      ElevatorGroupBootstrapBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
