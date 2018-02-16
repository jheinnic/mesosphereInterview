package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.simulator.passengers.IRandomVariables;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class WithLobbyReturnRandomVariables implements IRandomVariables
{
   int initialFloor = 0;
   double weight;
   int activityFloor;
   long activityDuration;

   public static WithLobbyReturnRandomVariables build(Consumer<WithLobbyReturnRandomVariablesBuilder> director)
   {
      final WithLobbyReturnRandomVariablesBuilder bldr = WithLobbyReturnRandomVariables.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public WithLobbyReturnRandomVariables copy(Consumer<WithLobbyReturnRandomVariablesBuilder> director)
   {
      final WithLobbyReturnRandomVariablesBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
