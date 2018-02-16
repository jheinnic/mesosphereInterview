package info.jchein.mesosphere.elevator.control.event;

import java.util.function.Consumer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class DropOffRequested implements ElevatorCarEvent {
   private final static Interner<DropOffRequested> INTERN_CACHE = Interners.newStrongInterner();
   
   final EventType eventType = EventType.DROP_OFF_REQUESTED;

   int carIndex;
   int dropOffFloorIndex;
   
   public static DropOffRequested build(Consumer<DropOffRequestedBuilder> director)
   {
      final DropOffRequestedBuilder bldr = DropOffRequested.builder();
      director.accept(bldr);
      return INTERN_CACHE.intern(bldr.build());
   }


   public DropOffRequested copy(Consumer<DropOffRequestedBuilder> director)
   {
      final DropOffRequestedBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return INTERN_CACHE.intern(bldr.build());
   }
}
