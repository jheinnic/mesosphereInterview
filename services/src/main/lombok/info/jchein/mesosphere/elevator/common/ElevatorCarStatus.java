package info.jchein.mesosphere.elevator.common;

import java.util.function.Consumer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.elevator.common.physics.JourneyArcMomentSeries;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class ElevatorCarStatus
{
   private static final Interner<ElevatorCarStatus> INTERN_CACHE = Interners.newWeakInterner();

   private final JourneyArcMomentSeries carTrajectory;
   
   @NotNull
   private final ServiceLifecycleStage serviceStage;
   
   private final long transitionIn;

   @NotNull
   @Size(min=1)
   @Singular
   private ImmutableList<PassengerPool> passengersPools;
   
   public static ElevatorCarStatus build(Consumer<ElevatorCarStatusBuilder> director)
   {
      final ElevatorCarStatusBuilder bldr = ElevatorCarStatus.builder();
      director.accept(bldr);
      final ElevatorCarStatus retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }

   public ElevatorCarStatus copy(Consumer<ElevatorCarStatusBuilder> director)
   {
      final ElevatorCarStatusBuilder bldr = this.toBuilder();
      director.accept(bldr);
      final ElevatorCarStatus retVal = bldr.build();

      return INTERN_CACHE.intern(retVal);
   }
}
