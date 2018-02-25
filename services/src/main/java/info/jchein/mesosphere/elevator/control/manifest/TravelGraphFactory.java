package info.jchein.mesosphere.elevator.control.manifest;


import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.control.manifest.bad.AfterTravelling;
import info.jchein.mesosphere.elevator.control.manifest.bad.BeforeTravelling;
import info.jchein.mesosphere.elevator.control.manifest.bad.BoardingNode;
import info.jchein.mesosphere.elevator.control.manifest.bad.LandingNode;
import info.jchein.mesosphere.elevator.control.manifest.bad.StillTravelling;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravelArcNode;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravelGraphIndex;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravelGraphIndex.TravelGraphIndexBuilder;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravellingNode;


public abstract class TravelGraphFactory
implements ITravelGraphFactory
{
   private final BeforeTravelling beforeNode;
   private final AfterTravelling afterNode;
   private final StillTravelling ongoingNode;
   private final BoardingNode[] boardingNodes;
   private final LandingNode[] landingNodes;
   private final TravelArcNode[][] travelArcNodes;
   private int numFloors;


   @Autowired
   public TravelGraphFactory( DeploymentConfiguration deployConfig )
   {
      this.numFloors =
         deployConfig.getBuilding()
            .getNumFloors();
      this.beforeNode = new BeforeTravelling();
      this.afterNode = new AfterTravelling();
      this.ongoingNode = new StillTravelling();

      this.boardingNodes = new BoardingNode[numFloors];
      this.landingNodes = new LandingNode[numFloors];
      this.travelArcNodes = new TravelArcNode[numFloors][numFloors];

      for (int ii = 0; ii < numFloors; ii++) {
         for (int jj = 0; jj < ii; jj++) {
            this.travelArcNodes[ii][jj] = new TravelArcNode(ii, jj);
         }
         for (int jj = ii + 1; jj < numFloors; jj++) {
            this.travelArcNodes[ii][jj] = new TravelArcNode(ii, jj);
         }

         this.boardingNodes[ii] = new BoardingNode(ii);
         this.landingNodes[ii] = new LandingNode(ii);
      }
   }


   @Override
   public ITravelGraph apply(DirectionOfTravel t)
   {
      Preconditions.checkArgument(t == DirectionOfTravel.GOING_UP || t == DirectionOfTravel.GOING_DOWN);

      final GraphBuilder<TravellingNode, DefaultWeightedEdge, DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>> builder =
         new GraphBuilder<TravellingNode, DefaultWeightedEdge, DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>>(
            new DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>(DefaultWeightedEdge.class));
      final int topNodeIdx = this.numFloors - 1;

      builder.addVertices(this.beforeNode, this.afterNode, this.ongoingNode);
      if (t == DirectionOfTravel.GOING_UP) {
         for (int ii = 0; ii < topNodeIdx; ii++) {
            builder.addEdge(this.beforeNode, this.boardingNodes[ii], 0);
            for (int jj = ii + 1; jj < this.numFloors; jj++) {
               builder.addEdge(this.boardingNodes[ii], this.travelArcNodes[ii][jj], 0);
               builder.addEdge(this.travelArcNodes[ii][jj], this.landingNodes[jj], 0);
            }
            builder.addEdge(this.landingNodes[ii + 1], this.afterNode, 0);
            builder.addEdge(this.landingNodes[ii + 1], this.ongoingNode, 0);
         }
      } else if (t == DirectionOfTravel.GOING_DOWN) {
         for (int ii = 1; ii < this.numFloors; ii++) {
            builder.addEdge(this.beforeNode, this.boardingNodes[ii], 0);
            for (int jj = 0; jj < ii; jj++) {
               builder.addEdge(this.boardingNodes[ii], this.travelArcNodes[ii][jj], 0);
               builder.addEdge(this.travelArcNodes[ii][jj], this.landingNodes[jj], 0);
            }
            builder.addEdge(this.landingNodes[ii - 1], this.afterNode, 0);
            builder.addEdge(this.landingNodes[ii - 1], this.ongoingNode, 0);
         }
      } else {
         throw new IllegalArgumentException(
            "Travel graph must be allocate either going up or going down");
      }

      final DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph = builder.build();
      final TravelGraphIndexBuilder indexBuilder = TravelGraphIndex.builder()
         .t(t)
         .numFloors(this.numFloors)
         .topFloorIdx(topNodeIdx)
         .graph(graph)
         .beforeNode(this.beforeNode)
         .afterNode(this.afterNode)
         .ongoingNode(this.ongoingNode)
         .boardingNodes(this.boardingNodes)
         .landingNodes(this.landingNodes)
         .travelArcNodes(this.travelArcNodes);
      this.indexGraphEdges(this.numFloors, graph, indexBuilder);

      return this.allocateTravelGraph(indexBuilder.build());
   }


   private void indexGraphEdges(
      int numFloors, DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph, TravelGraphIndexBuilder indexBuilder)
   {
      final DefaultWeightedEdge[] boardingEdges = new DefaultWeightedEdge[this.numFloors];
      final DefaultWeightedEdge[] landingEdges = new DefaultWeightedEdge[this.numFloors];
      final DefaultWeightedEdge[] ongoingEdges = new DefaultWeightedEdge[this.numFloors];
      final DefaultWeightedEdge[][] travelEdges = new DefaultWeightedEdge[this.numFloors][this.numFloors];

      for (final DefaultWeightedEdge boardingEdge : graph.outgoingEdgesOf(beforeNode)) {
         final TravellingNode boardingNode = graph.getEdgeTarget(boardingEdge);
         boardingEdges[boardingNode.getFromFloorIndex()] = boardingEdge;
         for (final DefaultWeightedEdge travelEdge : graph.outgoingEdgesOf(boardingNode)) {
            final TravellingNode travelingNode = graph.getEdgeTarget(travelEdge);
            travelEdges[travelingNode.getFromFloorIndex()][travelingNode.getToFloorIndex()] =
               travelEdge;
         }
      }

      for (final DefaultWeightedEdge landingEdge : graph.incomingEdgesOf(afterNode)) {
         final TravellingNode landingNode = graph.getEdgeSource(landingEdge);
         landingEdges[landingNode.getToFloorIndex()] = landingEdge;
         for (final DefaultWeightedEdge travelEdge : graph.incomingEdgesOf(landingNode)) {
            final TravellingNode travelingNode = graph.getEdgeSource(travelEdge);
            travelEdges[travelingNode.getToFloorIndex()][travelingNode.getFromFloorIndex()] =
               travelEdge;
         }
      }

      for (final DefaultWeightedEdge ongoingEdge : graph.incomingEdgesOf(ongoingNode)) {
         final TravellingNode landingNode = graph.getEdgeSource(ongoingEdge);
         ongoingEdges[landingNode.getToFloorIndex()] = ongoingEdge;
      }

      indexBuilder.boardingEdges(boardingEdges)
         .landingEdges(landingEdges)
         .ongoingEdges(ongoingEdges)
         .travelEdges(travelEdges);
   }


   protected abstract TravelGraph allocateTravelGraph(TravelGraphIndex graphIndex);
}
