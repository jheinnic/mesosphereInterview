package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.PassengerId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TravellerArrived implements TravellerEvent
{
   final SimulationEventType eventType = SimulationEventType.TRAVELLER_ARRIVED;

   long clockTime;
   PassengerId travellerId;
   String populationName;
   
   public static TravellerArrived build(Consumer<TravellerArrivedBuilder> director)
   {
      final TravellerArrivedBuilder bldr = TravellerArrived.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public TravellerArrived copy(Consumer<TravellerArrivedBuilder> director)
   {
      final TravellerArrivedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
