package info.jchein.mesosphere.elevator.common;

import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

@Data
@Builder(toBuilder=true)
class PickupCallState {
    private final int floorIndex;
    private final boolean goingUp;
    private final boolean goingDown;

    public static PickupCallState build(Consumer<PickupCallStateBuilder> director) {
       PickupCallStateBuilder bldr = PickupCallState.builder();
       director.accept(bldr);
       return bldr.build();
    }
    
    public PickupCallState copy(Consumer<PickupCallStateBuilder> director) {
       PickupCallStateBuilder bldr = this.toBuilder();
       director.accept(bldr);
       return bldr.build();
    }
}
