package info.jchein.mesosphere.elevator.domain.common;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder(toBuilder=true)
public class InitialElevatorCarState
{
   final int initialFloor;
   
   final double weightLoaded;
   
   @Singular
   final ImmutableList<Integer> requestFloors;
   
   @Singular
   final ImmutableList<PassengerBootstrap> passengers;
   
   public static InitialElevatorCarState build(Consumer<InitialElevatorCarStateBuilder> director) {
      InitialElevatorCarStateBuilder bldr = InitialElevatorCarState.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public InitialElevatorCarState copy(Consumer<InitialElevatorCarStateBuilder> director) {
      InitialElevatorCarStateBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
