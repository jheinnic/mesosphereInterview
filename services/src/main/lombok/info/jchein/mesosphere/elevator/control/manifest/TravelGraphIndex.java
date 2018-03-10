package info.jchein.mesosphere.elevator.control.manifest;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.manifest.TravelGraph.FloorOfOrigin;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TravelGraphIndex
{
   private int numFloors;
   private int topFloorIdx;
   private DirectionOfTravel t;
   private InboundNode inboundNode;
   private OutboundNode outboundNode;
   private ElevatorCarNode elevatorCarNode;
   private DisembarkingNode[] disembarkNodes;
   private TravelArcNode[][] travelArcNodes;
   private DefaultWeightedEdge ongoingTravelEdge;
   private DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph;
   private FSM<FloorOfOrigin> floorFsm;
}
