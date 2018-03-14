package info.jchein.mesosphere.elevator.control.manifest;


import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import lombok.SneakyThrows;


public class TravelGraph
implements ITravelGraph
{
   private final IRuntimeClock clock;
   private final FSM<FloorOfOrigin> floorFsm;

   final int numFloors;
   final DirectionOfTravel direction;

   private final FloorOfOrigin[] floorOrigins;

   int currentFloorStop;
   private double currentWeightLoad;
   private double visitWeightBoarded;
   private double visitWeightDisembarked;

   TravelGraph( TravelGraphIndex graphIndex, IRuntimeClock clock ) // , IPathNavigator ) 
   {
      this.clock = clock;
      this.direction = graphIndex.getT();
      this.floorFsm = graphIndex.getFloorFsm();
      this.numFloors = graphIndex.getNumFloors();
      this.currentFloorStop = -1;
      this.currentWeightLoad = 0;
      this.visitWeightBoarded = 0;
      this.visitWeightDisembarked = 0;
      
      this.floorOrigins = new FloorOfOrigin[this.numFloors];
      for ( int ii=0; ii<this.numFloors; ii++ ) {
         this.floorOrigins[ii] = new FloorOfOrigin(ii);
      }
   }

   @Override
   @SneakyThrows
   public void recordDoorsOpening(int floorIndex)
   {
      this.currentFloorStop = floorIndex;
      this.visitWeightBoarded = this.visitWeightDisembarked = 0;
      final long now = this.clock.now();
      for( final FloorOfOrigin nextFloor : this.floorOrigins ) {
         this.floorFsm.onEvent(nextFloor, FloorOfOriginEvents.Names.DOORS_OPENED, floorIndex, now);
      }
   }

   @Override
   @SneakyThrows
   public void recordDoorsClosed()
   {
      for( final FloorOfOrigin nextFloor : this.floorOrigins ) {
         this.floorFsm.onEvent(nextFloor, FloorOfOriginEvents.Names.DOORS_CLOSED);
      }
   }


   @Override
   @SneakyThrows
   public void recordDisembarked(double outgoingLoad)
   {
      this.currentWeightLoad -= outgoingLoad;
      this.visitWeightDisembarked += outgoingLoad;
      for( final FloorOfOrigin nextFloor : this.floorOrigins ) {
         this.floorFsm.onEvent(nextFloor, FloorOfOriginEvents.Names.WEIGHT_DECREASED, outgoingLoad);
      }
   }

   @Override
   @SneakyThrows
   public void recordBoarded(double incomingLoad)
   {
      this.currentWeightLoad += incomingLoad;
      this.visitWeightBoarded += incomingLoad;
      for( final FloorOfOrigin nextFloor : this.floorOrigins ) {
         this.floorFsm.onEvent(nextFloor, FloorOfOriginEvents.Names.WEIGHT_INCREASED, incomingLoad);
      }
   }

   @Override
   @SneakyThrows
   public void recordDropRequest(int floorIndex)
   {
      for( final FloorOfOrigin nextFloor : this.floorOrigins ) {
         this.floorFsm.onEvent(nextFloor, FloorOfOriginEvents.Names.DROP_REQUESTED, floorIndex);
      }
   }
   

   @Override
   public double getCurrentWeightLoad()
   {
      return this.currentWeightLoad;
   }

   @Override
   public void recordAssignedPickup(int floorIndex, DirectionOfTravel direction)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void recordCancelledPickup(int floorIndex, DirectionOfTravel direction)
   {
      // TODO Auto-generated method stub
      
   }
}
