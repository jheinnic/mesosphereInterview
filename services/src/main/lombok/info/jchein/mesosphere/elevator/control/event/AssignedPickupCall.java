package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class AssignedPickupCall implements ElevatorCarEvent {
   private final static Interner<AssignedPickupCall> INTERN_CACHE = Interners.newStrongInterner();
   
   final EventType eventType = EventType.ASSIGNED_PICKUP_CALL;

   int carIndex;
   int floorIndex;
   DirectionOfTravel direction;
   
   public static AssignedPickupCall build(Consumer<AssignedPickupCallBuilder> director)
   {
      final AssignedPickupCallBuilder bldr = AssignedPickupCall.builder();
      director.accept(bldr);
      return INTERN_CACHE.intern(bldr.build());
   }


   public AssignedPickupCall copy(Consumer<AssignedPickupCallBuilder> director)
   {
      final AssignedPickupCallBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return INTERN_CACHE.intern(bldr.build());
   }
}
