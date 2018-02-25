package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class ChangedDirection implements ElevatorCarEvent {
   final EventType eventType = EventType.CHANGED_DIRECTION;
   
	int carIndex;
	int floorIndex;
	DirectionOfTravel newDirection;
	
   
   public static ChangedDirection build(Consumer<ChangedDirectionBuilder> director)
   {
      final ChangedDirectionBuilder bldr = ChangedDirection.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public ChangedDirection copy(Consumer<ChangedDirectionBuilder> director)
   {
      final ChangedDirectionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
