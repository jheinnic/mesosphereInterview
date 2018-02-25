package info.jchein.mesosphere.elevator.control.manifest;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.IPathNavigator;
import info.jchein.mesosphere.elevator.control.manifest.bad.AfterTravelling;
import info.jchein.mesosphere.elevator.control.manifest.bad.BeforeTravelling;
import info.jchein.mesosphere.elevator.control.manifest.bad.BoardingNode;
import info.jchein.mesosphere.elevator.control.manifest.bad.LandingNode;
import info.jchein.mesosphere.elevator.control.manifest.bad.StillTravelling;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravelArcNode;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravelGraphIndex;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravellingNode;


public class TravelGraph
implements ITravelGraph
{
   private DirectionOfTravel direction;
   private DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph;
   private BeforeTravelling beforeNode;
   private AfterTravelling afterNode;
   private StillTravelling ongoingNode;
   private BoardingNode[] boardingNodes;
   private LandingNode[] landingNodes;
   private TravelArcNode[][] travelArcNodes;
   private int topFloorIdx;
   private int numFloors;
   private DefaultWeightedEdge[] boardingEdges;
   private DefaultWeightedEdge[] landingEdges;
   private DefaultWeightedEdge[][] travelEdges;
   private DefaultWeightedEdge[] ongoingEdges;
   private Comparator<ScheduledStop> currentComparator;
   private ArrayList<ScheduledStop> sortedStops;


   TravelGraph( TravelGraphIndex graphIndex ) // , IPathNavigator ) 
   {
      this.direction = graphIndex.getT();
      this.graph = graphIndex.getGraph();
      this.beforeNode = graphIndex.getBeforeNode();
      this.afterNode = graphIndex.getAfterNode();
      this.ongoingNode = graphIndex.getOngoingNode();
      this.boardingNodes = graphIndex.getBoardingNodes();
      this.landingNodes = graphIndex.getLandingNodes();
      this.travelArcNodes = graphIndex.getTravelArcNodes();
      this.topFloorIdx = graphIndex.getTopFloorIdx();
      this.numFloors = graphIndex.getNumFloors();
      this.boardingEdges = graphIndex.getBoardingEdges();
      this.landingEdges = graphIndex.getLandingEdges();
      this.ongoingEdges = graphIndex.getOngoingEdges();
      this.travelEdges = graphIndex.getTravelEdges();
   }

   public void recordAssignedPickup(int floorIndex, DirectionOfTravel newDirection) {
      ScheduledPickup newStop = new ScheduledPickup(floorIndex, newDirection);
      int insertionPoint = Collections.binarySearch(this.sortedStops, newStop, this.currentComparator);
      this.sortedStops.add(insertionPoint, newStop);
   }

   public void recordCancelledPickup(int floorIndex, DirectionOfTravel newDirection) {
      ScheduledPickup newStop = new ScheduledPickup(floorIndex, newDirection);
      int insertionPoint = Collections.binarySearch(this.sortedStops, newStop, this.currentComparator);
      this.sortedStops.add(insertionPoint, newStop);
   }

   public void prepareForBoarding(int floorIndex)
   {
      // TODO Auto-generated method stub
      
   }

   public void prepareForDisembarking(int floorIndex)
   {
      // TODO Auto-generated method stub
      
   }

   public void recordDisembarked(double outgoingLoad)
   {
      // TODO Auto-generated method stub
      
   }

   public void recordBoarded(double incomingLoad)
   {
      // TODO Auto-generated method stub
      
   }

   public void recordDropRequest(int floorIndex)
   {
      // TODO Auto-generated method stub
      
   }

   public double getCurrentWeightLoad()
   {
      // TODO Auto-generated method stub
      return 0;
   }

}
