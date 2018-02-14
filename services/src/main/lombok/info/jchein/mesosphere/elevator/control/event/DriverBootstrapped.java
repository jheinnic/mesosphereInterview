package info.jchein.mesosphere.elevator.control.event;

import lombok.Builder;
import lombok.Value;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

import java.util.BitSet;
import java.util.function.Consumer;

@Value
@Builder(toBuilder=true)
public class DriverBootstrapped implements ElevatorCarEvent {
   final EventType eventType = EventType.BOOTSTRAPPED_DRIVER;

	long clockTime;
	int carIndex;
	int floorIndex;
	double weightLoad;
	BitSet dropRequests;
	DirectionOfTravel initialDirection;
	
   public static DriverBootstrapped build(Consumer<DriverBootstrappedBuilder> director)
   {
      final DriverBootstrappedBuilder bldr = DriverBootstrapped.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public DriverBootstrapped copy(Consumer<DriverBootstrappedBuilder> director)
   {
      final DriverBootstrappedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

}
