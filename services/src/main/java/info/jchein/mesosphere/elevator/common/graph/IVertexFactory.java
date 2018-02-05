package info.jchein.mesosphere.elevator.common.graph;

import java.util.BitSet;
import java.util.Collection;

public interface IVertexFactory
{
   BuildingFloor getBuildingFloor(int floorIndex);
   
   BeforeTravelling getTravelPathOriginNode();

   AfterTravelling getTravelPathTerminalNode();

   TravelPathStageNodes getNextPathStage( int pickupFloorIndex, int nextFloorIndex, BitSet possibleDropFloors, Collection<TravellingPassengers> previousStage );
}
