package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class FloorSensorTriggered implements LandingEvent {
	long clockTime;
	long floorSequence;
	int floorIndex;
	int carIndex;
	DirectionOfTravel direction;
   
   public static FloorSensorTriggered build(Consumer<FloorSensorTriggeredBuilder> director)
   {
      final FloorSensorTriggeredBuilder bldr = FloorSensorTriggered.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public FloorSensorTriggered copy(Consumer<FloorSensorTriggeredBuilder> director)
   {
      final FloorSensorTriggeredBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
