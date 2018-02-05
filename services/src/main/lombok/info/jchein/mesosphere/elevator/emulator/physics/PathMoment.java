package info.jchein.mesosphere.elevator.emulator.physics;

import lombok.Value;
import lombok.Builder;
import java.util.function.Consumer;

@Value
@Builder(toBuilder=true)
public class PathMoment {
    private final double time;
    private final double height;
    private final double velocity;
    private final double acceleration;
    private final double jerk;

    public static PathMoment build(Consumer<PathMomentBuilder> director) {
       PathMomentBuilder bldr = PathMoment.builder();
       director.accept(bldr);
       return bldr.build();
    }
    
    public PathMoment copy(Consumer<PathMomentBuilder> director) {
       PathMomentBuilder bldr = this.toBuilder();
       director.accept(bldr);
       return bldr.build();
    }
}
