package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TravellerDeparted implements TravellerEvent
{
   final SimulationEventType eventType = SimulationEventType.TRAVELLER_DEPARTED;

   long clockTime;
   ISimulatedTraveller traveller;
   
   public static TravellerDeparted build(Consumer<TravellerDepartedBuilder> director)
   {
      final TravellerDepartedBuilder bldr = TravellerDeparted.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public TravellerDeparted copy(Consumer<TravellerDepartedBuilder> director)
   {
      final TravellerDepartedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
