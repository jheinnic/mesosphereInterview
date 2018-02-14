package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.PassengerId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PickupRequested implements TravellerEvent
{
   final SimulationEventType eventType = SimulationEventType.PICKUP_REQUESTED;

   long clockTime;
   PassengerId travellerId;
   String populationName;
   int floorIndex;
   DirectionOfTravel direction;
   
   public static PickupRequested build(Consumer<PickupRequestedBuilder> director)
   {
      final PickupRequestedBuilder bldr = PickupRequested.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public PickupRequested copy(Consumer<PickupRequestedBuilder> director)
   {
      final PickupRequestedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
