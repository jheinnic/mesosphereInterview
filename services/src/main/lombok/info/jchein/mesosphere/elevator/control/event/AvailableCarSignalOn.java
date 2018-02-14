package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class AvailableCarSignalOn implements LandingEvent {
   final EventType eventType = EventType.CAR_AVAILABLE_SIGNAL_LIT;
   
	int floorIndex;
	DirectionOfTravel direction;
	
	  
   public static AvailableCarSignalOn build(Consumer<AvailableCarSignalOnBuilder> director)
   {
      final AvailableCarSignalOnBuilder bldr = AvailableCarSignalOn.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public AvailableCarSignalOn copy(Consumer<AvailableCarSignalOnBuilder> director)
   {
      final AvailableCarSignalOnBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
