package info.jchein.mesosphere.elevator.common.graph;

/**
 * A polymorphism root for the three nodes that represent a group of passengers in an elevator car along a single hop of an elevator car's path.
 * 
 * @author jheinnic
 *
 */
public interface TravellingPassengers extends TravellingPathNode
{
   /**
    * In the special case of an InitialPickupStep, this method will return an interned BitSet with only ridingFromFloor set.
    * 
    * In the special case of a FinalDropOffStep, this method will return an interned BitSet with only the same value as returned by getPickupFloor().
    * 
    * @return
    */
   public ProtectedBitSet getPickupFloors();
   
   public int getRidingFromFloor();
   
   public int getRidingToFloor();
   
   /**
    * In the special case of a FinalDropOffStep, this method will return an interned BitSet with only ridingToFloor set.
    * 
    * @return
    */
   public ProtectedBitSet getDropOffFloors();
}
