package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class AvailableCarSignalOff implements LandingEvent {
	long clockTime;
	long floorSequence;
	DirectionOfTravel direction;
	int floorIndex;
	
	  
   public static AvailableCarSignalOff build(Consumer<AvailableCarSignalOffBuilder> director)
   {
      final AvailableCarSignalOffBuilder bldr = AvailableCarSignalOff.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public AvailableCarSignalOff copy(Consumer<AvailableCarSignalOffBuilder> director)
   {
      final AvailableCarSignalOffBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
