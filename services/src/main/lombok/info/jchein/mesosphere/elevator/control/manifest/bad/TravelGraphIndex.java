package info.jchein.mesosphere.elevator.control.manifest.bad;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TravelGraphIndex
{
   private int numFloors;
   private int topFloorIdx;
   private DirectionOfTravel t;
   private DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph;
   private BeforeTravelling beforeNode;
   private AfterTravelling afterNode;
   private StillTravelling ongoingNode;
   private BoardingNode[] boardingNodes;
   private LandingNode[] landingNodes;
   private TravelArcNode[][] travelArcNodes;
   private DefaultWeightedEdge[] boardingEdges;
   private DefaultWeightedEdge[] landingEdges;
   private DefaultWeightedEdge[][] travelEdges;
   private DefaultWeightedEdge[] ongoingEdges;
}
