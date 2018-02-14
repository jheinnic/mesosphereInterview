package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PassengerDoorsClosed implements ElevatorCarEvent {
   final EventType eventType = EventType.PASSENGER_DOORS_CLOSED;
   
	int carIndex;
	
   public static PassengerDoorsClosed build(Consumer<PassengerDoorsClosedBuilder> director)
   {
      final PassengerDoorsClosedBuilder bldr = PassengerDoorsClosed.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public PassengerDoorsClosed copy(Consumer<PassengerDoorsClosedBuilder> director)
   {
      final PassengerDoorsClosedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
