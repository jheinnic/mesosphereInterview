package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import org.hibernate.validator.constraints.ScriptAssert;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
@ScriptAssert(lang="javascript", alias="_this", script="if (_this.destinationFloorIndex > _this.originFloorIndex) { return _this.direction == DirectionOfTravel.GOING_UP } else if(_this.originFloorIndex > _this.destinationFloorIndex) { return _this.direction == DirectionOfTravel.GOING_DOWN; } else { return false; }")
public class PickupRequested implements TravellerEvent
{
   final SimulationEventType eventType = SimulationEventType.PICKUP_REQUESTED;

   long clockTime;
   ISimulatedTraveller traveller;
   int originIndex;
   DirectionOfTravel direction;
   int destinationIndex;
   
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
