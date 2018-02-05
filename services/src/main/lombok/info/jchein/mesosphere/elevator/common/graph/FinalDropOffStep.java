package info.jchein.mesosphere.elevator.common.graph;

import org.hibernate.validator.constraints.ScriptAssert;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

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
@ToString 
@AllArgsConstructor 
@EqualsAndHashCode(doNotUseGetters=true)
@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
@ScriptAssert(lang="javascript", alias="_this", script="_this.pickupFloor !== _this.dropOffFloor")
public class FinalDropOffStep implements TravellingPathNode
{
   @Getter
   private final int pickupFloor;
   
   @Getter
   private final int dropOffFloor;

//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.FINAL_DROP_OFF;
//   }
}
