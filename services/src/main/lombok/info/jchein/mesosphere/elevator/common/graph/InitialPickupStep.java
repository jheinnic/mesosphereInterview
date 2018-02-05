package info.jchein.mesosphere.elevator.common.graph;

import javax.validation.constraints.Size;

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
@ScriptAssert.List({
   @ScriptAssert(lang="javascript", alias="_this", script="_this.pickupFloors.get(_this.ridingFromFloor)"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.dropOffFloors.get(_this.ridingToFloor)"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.ridingFromFloor !== _this.ridingToFloor")
 })
public class InitialPickupStep implements TravellingPassengers
{
   @Size(min=1, max=1)
   private ProtectedBitSet pickupFloors;
   
   @Getter
   private int ridingFromFloor;
   
   @Getter
   private int ridingToFloor;

   @Size(min=1, max=1)
   private ProtectedBitSet dropOffFloors;

//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.INITIAL_PICKUP;
//   }
   
   @Override
   public ProtectedBitSet getPickupFloors() {
      return this.pickupFloors;
   }
   
   public int getPickupFloor()
   {
      return this.ridingFromFloor;
   }
   
   @Override
   public ProtectedBitSet getDropOffFloors() {
      return this.dropOffFloors;
   }
}
