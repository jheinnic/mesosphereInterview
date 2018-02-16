package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PopulationDefined implements PopulationEvent
{
   SimulationEventType eventType = SimulationEventType.POPULATION_DEFINED;
   long clockTime;
   String populationName;
   
   public static PopulationDefined build(Consumer<PopulationDefinedBuilder> director)
   {
      final PopulationDefinedBuilder bldr = PopulationDefined.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public PopulationDefined copy(Consumer<PopulationDefinedBuilder> director)
   {
      final PopulationDefinedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
