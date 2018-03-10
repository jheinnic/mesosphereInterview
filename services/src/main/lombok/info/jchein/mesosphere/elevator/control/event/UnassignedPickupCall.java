package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class UnassignedPickupCall implements ElevatorCarEvent {
   private final static Interner<UnassignedPickupCall> INTERN_CACHE = Interners.newStrongInterner();
   
   final EventType eventType = EventType.UNASSIGNED_PICKUP_CALL;

   int carIndex;
   int floorIndex;
   DirectionOfTravel direction;
   
   public static UnassignedPickupCall build(Consumer<UnassignedPickupCallBuilder> director)
   {
      final UnassignedPickupCallBuilder bldr = UnassignedPickupCall.builder();
      director.accept(bldr);
      return INTERN_CACHE.intern(bldr.build());
   }


   public UnassignedPickupCall copy(Consumer<UnassignedPickupCallBuilder> director)
   {
      final UnassignedPickupCallBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return INTERN_CACHE.intern(bldr.build());
   }
}
