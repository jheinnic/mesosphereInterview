package info.jchein.mesosphere.elevator.simulator.event;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.PassengerId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class DisembarkedElevator implements TravellerEvent
{
   final SimulationEventType eventType = SimulationEventType.DISEMBARKED_ELEVATOR;

   long clockTime;
   PassengerId travellerId;
   String populationName;
   int floorIndex;
   int carIndex;
   
   public static DisembarkedElevator build(Consumer<DisembarkedElevatorBuilder> director)
   {
      final DisembarkedElevatorBuilder bldr = DisembarkedElevator.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DisembarkedElevator copy(Consumer<DisembarkedElevatorBuilder> director)
   {
      final DisembarkedElevatorBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
