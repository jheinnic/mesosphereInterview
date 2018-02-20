package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.simulator.passengers.IRandomVariables;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class VariablesWithLobbyReturn implements IRandomVariables
{
   int initialFloor = 0;
   double weight;
   int activityFloor;
   double activitySeconds;

   public static VariablesWithLobbyReturn build(Consumer<VariablesWithLobbyReturnBuilder> director)
   {
      final VariablesWithLobbyReturnBuilder bldr = VariablesWithLobbyReturn.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public VariablesWithLobbyReturn copy(Consumer<VariablesWithLobbyReturnBuilder> director)
   {
      final VariablesWithLobbyReturnBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
