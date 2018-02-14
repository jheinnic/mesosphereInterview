package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

@Value
@Builder(toBuilder=true)
public class PassengerDoorsOpened implements ElevatorCarEvent {
   final EventType eventType = EventType.PASSENGER_DOORS_OPENED;
   
	int carIndex;
	int floorIndex;
	DirectionOfTravel direction;
	
   public static PassengerDoorsOpened build(Consumer<PassengerDoorsOpenedBuilder> director)
   {
      final PassengerDoorsOpenedBuilder bldr = PassengerDoorsOpened.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public PassengerDoorsOpened copy(Consumer<PassengerDoorsOpenedBuilder> director)
   {
      final PassengerDoorsOpenedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
