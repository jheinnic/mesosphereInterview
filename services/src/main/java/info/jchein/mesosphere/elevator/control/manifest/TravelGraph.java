package info.jchein.mesosphere.elevator.control.manifest;


import java.util.BitSet;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.MaximumFlow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.statefulj.fsm.FSM;
import org.statefulj.persistence.annotations.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import lombok.Data;
import lombok.SneakyThrows;


public class TravelGraph
implements ITravelGraph
{
   private final IRuntimeClock clock;
   private final FSM<FloorOfOrigin> floorFsm;
   private final DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph;

   private final int numFloors;
   private final DirectionOfTravel direction;
   private final InboundNode inboundNode;
   private final OutboundNode outboundNode;
   private final ElevatorCarNode elevatorCarNode;
   private final DisembarkingNode[] disembarkNodes;
   private final TravelArcNode[][] travelArcNodes;
   private final DefaultWeightedEdge[] outboundEdges;
   private final DefaultWeightedEdge ongoingEdge;

   private final FloorOfOrigin[] floorOrigins;

   private int currentFloorStop;
   private double currentWeightLoad;
   private double visitWeightBoarded;
   private double visitWeightDisembarked;

   TravelGraph( TravelGraphIndex graphIndex, IRuntimeClock clock ) // , IPathNavigator ) 
   {
      this.clock = clock;
      this.direction = graphIndex.getT();
      this.floorFsm = graphIndex.getFloorFsm();
      this.graph = graphIndex.getGraph();
      this.inboundNode = graphIndex.getInboundNode();
      this.outboundNode = graphIndex.getOutboundNode();
      this.elevatorCarNode = graphIndex.getElevatorCarNode();
      this.disembarkNodes = graphIndex.getDisembarkNodes();
      this.travelArcNodes = graphIndex.getTravelArcNodes();
      this.ongoingEdge = graphIndex.getOngoingTravelEdge();
      this.numFloors = graphIndex.getNumFloors();
      this.currentFloorStop = -1;
      this.currentWeightLoad = 0;
      this.visitWeightBoarded = 0;
      this.visitWeightDisembarked = 0;
      
      this.outboundEdges = new DefaultWeightedEdge[this.numFloors];
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
      final DisembarkingNode disembarkNode = this.disembarkNodes[this.currentFloorStop];
      TravelGraph.this.graph.addVertex(disembarkNode);
      this.outboundEdges[this.currentFloorStop] = TravelGraph.this.graph.addEdge(disembarkNode, this.outboundNode);
      TravelGraph.this.graph.setEdgeWeight(this.outboundEdges[this.currentFloorStop], this.visitWeightDisembarked);
      TravelGraph.this.graph.setEdgeWeight(this.ongoingEdge, TravelGraph.this.currentWeightLoad);

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


   public void solveFlow() {
      MaximumFlowAlgorithm<TravellingNode, DefaultWeightedEdge> maxFlow = new PushRelabelMFImpl<>(this.graph);
      MaximumFlow<DefaultWeightedEdge> flow = maxFlow.getMaximumFlow(this.inboundNode, this.outboundNode);
      flow.getFlow().entrySet().stream().forEach(entry -> {
         this.graph.getEdgeSource(entry.getKey()).printMe(entry.getValue(),
               this.graph.getEdgeTarget(entry.getKey()));
      });
   }

   @Data
   class FloorOfOrigin
   {
      @State(accessorType = AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
      private String state;

      private final int pickupFloor;
      private long pickupTime;
      private BitSet possibleDropFloors;
      private BitSet unvisittedDropFloors;
      private double ownWeightAtPickup;
      private double ownWeightRemaining;
      private double ownWeightAtVisit;
      private double peerWeightOnboard;
      private double peerWeightDisembarked;
      private boolean finalStop;

      private DefaultWeightedEdge firstEdgeFromInbound;
      private DefaultWeightedEdge lastEdgeToElevatorCar;
      private DefaultWeightedEdge[] disembarkEdges;
      private TravelArcNode lastTravelArcNode;

      private DefaultWeightedEdge lastEdgeToTravelArc;


      FloorOfOrigin( int pickupFloor )
      {
         this.pickupFloor = pickupFloor;
         this.possibleDropFloors = new BitSet();
         this.unvisittedDropFloors = new BitSet();
         this.disembarkEdges = new DefaultWeightedEdge[TravelGraph.this.numFloors];
      }


      public int getFloorIndex()
      {
         return this.pickupFloor;
      }
      
      
      public long getPickupTime()
      {
         return this.pickupTime;
      }


      double getOwnWeightRemaining() 
      {
         return this.ownWeightRemaining;
      }


      void trackBeginPickup(long pickupTime)
      {
         this.pickupTime = pickupTime;
      }
      

      void trackCompletePickup() 
      {
         this.peerWeightOnboard -= this.peerWeightDisembarked;
         this.peerWeightDisembarked = 0;
         this.unvisittedDropFloors.or(this.possibleDropFloors);
         
         this.lastTravelArcNode = TravelGraph.this.travelArcNodes[this.pickupFloor][this.pickupFloor];
         TravelGraph.this.graph.addVertex(this.lastTravelArcNode);
         
         this.firstEdgeFromInbound = TravelGraph.this.graph.addEdge(TravelGraph.this.inboundNode, this.lastTravelArcNode);
         this.lastEdgeToElevatorCar = TravelGraph.this.graph.addEdge(this.lastTravelArcNode, TravelGraph.this.elevatorCarNode);
         this.lastEdgeToTravelArc = this.firstEdgeFromInbound;

         TravelGraph.this.graph.setEdgeWeight(this.firstEdgeFromInbound, this.ownWeightAtPickup);
         TravelGraph.this.graph.setEdgeWeight(this.lastEdgeToElevatorCar, this.ownWeightAtPickup);

         // TODO Move this last weight assignment up to the graph to avoid redundant assignment!
         // TravelGraph.this.graph.setEdgeWeight(TravelGraph.this.ongoingEdge, TravelGraph.this.totalWeightLoad);
      }
      
      void trackBeginVisit(long pickupTime)
      {
         this.unvisittedDropFloors.clear(TravelGraph.this.currentFloorStop);
         this.ownWeightAtVisit = this.ownWeightRemaining;
      }
      
      void trackCompleteVisit()
      {
         final int fromFloorIndex = this.pickupFloor;
         final int toFloorIndex = TravelGraph.this.currentFloorStop;

         DisembarkingNode disembarkNode = TravelGraph.this.disembarkNodes[toFloorIndex];
         TravelArcNode previousTravelArcNode = this.lastTravelArcNode;
         this.lastTravelArcNode = TravelGraph.this.travelArcNodes[fromFloorIndex][toFloorIndex];
         TravelGraph.this.graph.addVertex(this.lastTravelArcNode);

         TravelGraph.this.graph.removeEdge(this.lastEdgeToElevatorCar);
         this.lastEdgeToElevatorCar = TravelGraph.this.graph.addEdge(this.lastTravelArcNode, TravelGraph.this.elevatorCarNode);
         this.lastEdgeToTravelArc = TravelGraph.this.graph.addEdge(previousTravelArcNode, this.lastTravelArcNode);
         this.disembarkEdges[toFloorIndex] = TravelGraph.this.graph.addEdge(previousTravelArcNode, disembarkNode);

         TravelGraph.this.graph.setEdgeWeight(this.lastEdgeToElevatorCar, this.ownWeightRemaining);
         TravelGraph.this.graph.setEdgeWeight(this.lastEdgeToTravelArc, this.ownWeightAtVisit);
         TravelGraph.this.graph.setEdgeWeight(this.disembarkEdges[toFloorIndex], Math.min(this.ownWeightAtVisit, TravelGraph.this.visitWeightDisembarked));
      }


      void trackDropRequest(int floorIndex)
      {
         if ((TravelGraph.this.direction == DirectionOfTravel.GOING_UP && floorIndex > this.pickupFloor) ||
            (TravelGraph.this.direction == DirectionOfTravel.GOING_DOWN && floorIndex < this.pickupFloor))
         {
            this.possibleDropFloors.set(floorIndex);
         }
      }


      boolean isPotentialStop()
      {
         return this.possibleDropFloors.get(TravelGraph.this.currentFloorStop);
      }

      boolean isLastStop()
      {
         return this.isPotentialStop() && (this.unvisittedDropFloors.size() == 0);
      }


      void trackDisembarkingWeight(double weightLoad)
      {
         this.peerWeightDisembarked += weightLoad;
         if (this.peerWeightDisembarked > this.peerWeightOnboard) {
            final double ownDisembarkedWeight = this.peerWeightDisembarked - this.peerWeightOnboard;
            if (ownDisembarkedWeight > this.ownWeightRemaining) { throw new IllegalArgumentException(
               String.format(
                  "Insufficient weight remaining (%f) for actual disembarking weight with no residual peer weight (%f)",
                  this.ownWeightRemaining,
                  ownDisembarkedWeight)); }
            this.peerWeightDisembarked = this.peerWeightOnboard;
            this.ownWeightRemaining -= ownDisembarkedWeight;
         }
      }


      void trackOwnBoardingWeight(double weightLoad)
      {
         this.ownWeightAtPickup += weightLoad;
      }


      void trackPeerBoardingWeight(double weightLoad)
      {
         this.peerWeightOnboard += weightLoad;
      }
   }
}
