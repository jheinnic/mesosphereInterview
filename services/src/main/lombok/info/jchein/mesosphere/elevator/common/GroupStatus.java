package info.jchein.mesosphere.elevator.common;

import java.util.function.Consumer;

import javax.validation.constraints.Size;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.elevator.common.graph.DirectedAdjacency;
import info.jchein.mesosphere.elevator.common.graph.TravelArc;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * A LocationIntent is a node that captures a present and future location pair.  There is no restriction on valid floor pairs.  Use this instance for
 * graphs that need a vertex count bounded by the cartesian product, Floor X Floor.  The distinct case where from and to are the same floor represents an
 * explicit decision to go nowhere.  Both floor indices must be valid for the host building (e.g. neither negative nor greater than the highest known floor.)
 * 
 * More common use cases will be interested in either floor pairs that must not be identical.  Vertex classes with this semantic difference are found
 * in {@link TravelArc}, for all non-identical floor pairs, and {@link DirectedAdjacency}, for immediately adjacent floor pairs only.
 * 
 * @author jheinnic
 */
@Value
@Builder(toBuilder=true)
public class GroupStatus 
{
   private static final Interner<GroupStatus> INTERN_CACHE = Interners.newWeakInterner();

   @Size(min=1)
   @Singular
   private ImmutableList<FloorLandingStatus> floors;

   private ImmutableList<ElevatorCarStatus> cars;
   
   private ImmutableList<CompletedTrip> completedTrips;
   
   private ImmutableList<FutureCall> arrivalForecast;
   
   public static GroupStatus build(Consumer<GroupStatusBuilder> director)
   {
      final GroupStatusBuilder bldr = GroupStatus.builder();
      director.accept(bldr);
      final GroupStatus retVal = bldr.build();
      return INTERN_CACHE.intern(retVal);
   }


   public GroupStatus copy(Consumer<GroupStatusBuilder> director)
   {
      final GroupStatusBuilder bldr = this.toBuilder();
      director.accept(bldr);
      final GroupStatus retVal = bldr.build();
      return INTERN_CACHE.intern(retVal);
   }
}
