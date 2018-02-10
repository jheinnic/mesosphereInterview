package info.jchein.mesosphere.elevator.common.physics;


import lombok.Value;
import lombok.Builder;
import java.util.function.Consumer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;


@Value
@Builder(toBuilder = true)
public class PathMoment
{
   private static final Interner<PathMoment> INTERN_CACHE = Interners.newWeakInterner();

   private final double time;
   private final double height;
   private final double velocity;
   private final double acceleration;
   private final double jerk;


   public static PathMoment build(Consumer<PathMomentBuilder> director)
   {
      PathMomentBuilder bldr = PathMoment.builder();
      director.accept(bldr);
      final PathMoment retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }


   public PathMoment copy(Consumer<PathMomentBuilder> director)
   {
      PathMomentBuilder bldr = this.toBuilder();
      director.accept(bldr);
      final PathMoment retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }
}
