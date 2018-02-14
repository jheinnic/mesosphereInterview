package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.PassengerId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class BoardedElevator implements TravellerEvent
{
   final SimulationEventType eventType = SimulationEventType.BOARDED_ELEVATOR;

   long clockTime;
   PassengerId travellerId;
   String populationName;
   int floorIndex;
   DirectionOfTravel direction;
   int carIndex;
   
   
   public static BoardedElevator build(Consumer<BoardedElevatorBuilder> director)
   {
      final BoardedElevatorBuilder bldr = BoardedElevator.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public BoardedElevator copy(Consumer<BoardedElevatorBuilder> director)
   {
      final BoardedElevatorBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
