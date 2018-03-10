package info.jchein.mesosphere.elevator.control.manifest;


import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.Action;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.Transition;
import org.statefulj.fsm.model.impl.StateActionPairImpl;
import org.statefulj.fsm.model.impl.StateImpl;
import org.statefulj.persistence.memory.MemoryPersisterImpl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.control.manifest.TravelGraph.FloorOfOrigin;
import info.jchein.mesosphere.elevator.control.manifest.TravelGraphIndex.TravelGraphIndexBuilder;


public abstract class TravelGraphFactory
implements ITravelGraphFactory
{
   private final InboundNode inboundNode;
   private final OutboundNode outboundNode;
   private final ElevatorCarNode elevatorCarNode;
   private final DisembarkingNode[] disembarkNodes;
   private final TravelArcNode[][] travelArcNodes;
   private final int numFloors;

   private final StateImpl<FloorOfOrigin> beforePickup =
      new StateImpl<>(FloorOfOriginStates.Names.BEFORE_PICKUP);
   private final StateImpl<FloorOfOrigin> duringPickup =
      new StateImpl<>(FloorOfOriginStates.Names.DURING_PICKUP);
   private final StateImpl<FloorOfOrigin> makingStops =
      new StateImpl<>(FloorOfOriginStates.Names.MAKING_STOPS);
   private final StateImpl<FloorOfOrigin> finished =
      new StateImpl<>(FloorOfOriginStates.Names.FINISHED);
   private final FSM<FloorOfOrigin> floorFsm;


   @Autowired
   public TravelGraphFactory( DeploymentConfiguration deployConfig )
   {
      this.numFloors =
         deployConfig.getBuilding()
            .getNumFloors();
      this.inboundNode = new InboundNode();
      this.outboundNode = new OutboundNode();
      this.elevatorCarNode = new ElevatorCarNode();
      this.disembarkNodes = new DisembarkingNode[numFloors];
      this.travelArcNodes = new TravelArcNode[numFloors][numFloors];

      for (int ii = 0; ii < numFloors; ii++) {
         for (int jj = 0; jj < numFloors; jj++) {
            this.travelArcNodes[ii][jj] = new TravelArcNode(ii, jj);
         }

         this.disembarkNodes[ii] = new DisembarkingNode(ii);
      }

      Action<FloorOfOrigin> onDropRequested =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Integer floorIndex = (Integer) args[0];
            stateful.trackDropRequest(floorIndex);
         };
      Action<FloorOfOrigin> onDoorsOpenedForPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Long clockTime = (Long) args[1];
            stateful.trackBeginPickup(clockTime);
         };
      Action<FloorOfOrigin> onDoorsOpenedForVisit =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Long clockTime = (Long) args[1];
            stateful.trackBeginVisit(clockTime);
         };
      Action<FloorOfOrigin> onOwnWeightIncreased =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Double weightIncrease = (Double) args[0];
            stateful.trackOwnBoardingWeight(weightIncrease);
         };
      Action<FloorOfOrigin> onPeerWeightIncreased =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Double weightIncrease = (Double) args[0];
            stateful.trackPeerBoardingWeight(weightIncrease);
         };
      Action<FloorOfOrigin> onWeightDecreased =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Double weightDecrease = (Double) args[0];
            stateful.trackDisembarkingWeight(weightDecrease);
         };
      Action<FloorOfOrigin> onDoorsClosedForPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            stateful.trackCompletePickup();
         };
      Action<FloorOfOrigin> onDoorsClosedForVisit =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            stateful.trackCompleteVisit();
         };

      Transition<FloorOfOrigin> onDoorsOpenedBeforePickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            int floorIndex = ((Integer) args[0]).intValue();

            if (floorIndex == stateful.getFloorIndex()) {
               return new StateActionPairImpl<FloorOfOrigin>(this.duringPickup, onDoorsOpenedForPickup);
            }

            return new StateActionPairImpl<FloorOfOrigin>(this.beforePickup, null);
         };
      Transition<FloorOfOrigin> onDoorsClosedDuringPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            if (stateful.getOwnWeightAtPickup() == 0) {
               return new StateActionPairImpl<FloorOfOrigin>(this.finished, null);
            }
            
            return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, onDoorsClosedForPickup);
         };


      Transition<FloorOfOrigin> onDoorsOpenedAfterPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            if (stateful.isPotentialStop()) {
               return new StateActionPairImpl<FloorOfOrigin>( this.makingStops, onDoorsOpenedForVisit);
            }

            return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, null);
         };
      Transition<FloorOfOrigin> onDoorsClosedAfterPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            if (stateful.isPotentialStop()) {
               if (stateful.isLastStop() || (stateful.getOwnWeightRemaining() == 0)) {
                  return new StateActionPairImpl<FloorOfOrigin>(this.finished, onDoorsClosedForVisit);
               }

               return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, onDoorsClosedForVisit);
            }

            return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, null);
         };

      this.beforePickup.addTransition(FloorOfOriginEvents.Names.DOORS_OPENED, onDoorsOpenedBeforePickup);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.beforePickup, onPeerWeightIncreased);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.beforePickup, onWeightDecreased);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, this.beforePickup, null);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.beforePickup, onDropRequested);

      this.duringPickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.duringPickup, onOwnWeightIncreased);
      this.duringPickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.duringPickup, onWeightDecreased);
      this.duringPickup.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, onDoorsClosedDuringPickup);
      this.duringPickup.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.duringPickup, onDropRequested);

      this.makingStops.addTransition(FloorOfOriginEvents.Names.DOORS_OPENED, onDoorsOpenedAfterPickup);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.makingStops, onPeerWeightIncreased);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.makingStops, onWeightDecreased);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.makingStops, null);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, onDoorsClosedAfterPickup);
      
      this.finished.addTransition(FloorOfOriginEvents.Names.DOORS_OPENED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.finished, null);
      
      ImmutableList.Builder<State<FloorOfOrigin>> listBuilder = ImmutableList.<State<FloorOfOrigin>>builder();
      listBuilder.add(this.beforePickup)
         .add(this.duringPickup)
         .add(this.makingStops)
         .add(this.finished);
      MemoryPersisterImpl<FloorOfOrigin> persister = new MemoryPersisterImpl<FloorOfOrigin>(listBuilder.build(), this.beforePickup);
      this.floorFsm = new FSM<FloorOfOrigin>("FloorOfOrigin", persister);
   }


   @Override
   public ITravelGraph apply(DirectionOfTravel t)
   {
      Preconditions.checkArgument(t == DirectionOfTravel.GOING_UP || t == DirectionOfTravel.GOING_DOWN);

      final GraphBuilder<TravellingNode, DefaultWeightedEdge, DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>> builder =
         new GraphBuilder<TravellingNode, DefaultWeightedEdge, DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>>(
            new DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>(
               DefaultWeightedEdge.class));
      final int topNodeIdx = this.numFloors - 1;

      builder.addVertices(this.inboundNode, this.outboundNode, this.elevatorCarNode);
      builder.addEdge(this.elevatorCarNode, this.outboundNode, 0);
      /*
       * if (t == DirectionOfTravel.GOING_UP) { for (int ii = 0; ii < topNodeIdx; ii++) {
       * builder.addEdge(this.beforeNode, this.boardingNodes[ii], 0); for (int jj = ii + 1; jj < this.numFloors; jj++) {
       * builder.addEdge(this.boardingNodes[ii], this.travelArcNodes[ii][jj], 0);
       * builder.addEdge(this.travelArcNodes[ii][jj], this.disembarkingNodes[jj], 0); }
       * builder.addEdge(this.disembarkingNodes[ii + 1], this.afterNode, 0); builder.addEdge(this.disembarkingNodes[ii +
       * 1], this.ongoingNode, 0); } } else if (t == DirectionOfTravel.GOING_DOWN) { for (int ii = 1; ii <
       * this.numFloors; ii++) { builder.addEdge(this.beforeNode, this.boardingNodes[ii], 0); for (int jj = 0; jj < ii;
       * jj++) { builder.addEdge(this.boardingNodes[ii], this.travelArcNodes[ii][jj], 0);
       * builder.addEdge(this.travelArcNodes[ii][jj], this.disembarkingNodes[jj], 0); }
       * builder.addEdge(this.disembarkingNodes[ii - 1], this.afterNode, 0); builder.addEdge(this.disembarkingNodes[ii -
       * 1], this.ongoingNode, 0); } } else { throw new IllegalArgumentException(
       * "Travel graph must be allocate either going up or going down"); }
       */

      final DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge> graph = builder.build();
      final TravelGraphIndexBuilder indexBuilder =
         TravelGraphIndex.builder()
            .t(t)
            .numFloors(this.numFloors)
            .topFloorIdx(topNodeIdx)
            .floorFsm(this.floorFsm)
            .graph(graph)
            .inboundNode(this.inboundNode)
            .outboundNode(this.outboundNode)
            .elevatorCarNode(this.elevatorCarNode)
            .disembarkNodes(this.disembarkNodes)
            .travelArcNodes(this.travelArcNodes)
            .ongoingTravelEdge(
               graph.outgoingEdgesOf(this.elevatorCarNode)
                  .iterator()
                  .next());
      // this.indexGraphEdges(this.numFloors, graph, indexBuilder);

      return this.allocateTravelGraph(indexBuilder.build());
   }


   /*
    * private void indexGraphEdges(int numFloors, DefaultDirectedWeightedGraph<TravellingNode, DefaultWeightedEdge>
    * graph, TravelGraphIndexBuilder indexBuilder) { final DefaultWeightedEdge[] boardingEdges = new
    * DefaultWeightedEdge[this.numFloors]; final DefaultWeightedEdge[] landingEdges = new
    * DefaultWeightedEdge[this.numFloors]; final DefaultWeightedEdge[] ongoingEdges = new
    * DefaultWeightedEdge[this.numFloors]; final DefaultWeightedEdge[][] travelEdges = new
    * DefaultWeightedEdge[this.numFloors][this.numFloors];
    * 
    * for (final DefaultWeightedEdge boardingEdge : graph.outgoingEdgesOf(inboundNode)) { final TravellingNode
    * boardingNode = graph.getEdgeTarget(boardingEdge); boardingEdges[boardingNode.getFromFloorIndex()] = boardingEdge;
    * for (final DefaultWeightedEdge travelEdge : graph.outgoingEdgesOf(boardingNode)) { final TravellingNode
    * travelingNode = graph.getEdgeTarget(travelEdge);
    * travelEdges[travelingNode.getFromFloorIndex()][travelingNode.getToFloorIndex()] = travelEdge; } }
    * 
    * for (final DefaultWeightedEdge landingEdge : graph.incomingEdgesOf(outboundNode)) { final TravellingNode
    * landingNode = graph.getEdgeSource(landingEdge); landingEdges[landingNode.getToFloorIndex()] = landingEdge; for
    * (final DefaultWeightedEdge travelEdge : graph.incomingEdgesOf(landingNode)) { final TravellingNode travelingNode =
    * graph.getEdgeSource(travelEdge); travelEdges[travelingNode.getToFloorIndex()][travelingNode.getFromFloorIndex()] =
    * travelEdge; } }
    * 
    * for (final DefaultWeightedEdge ongoingEdge : graph.incomingEdgesOf(elevatorCarNode)) { final TravellingNode
    * landingNode = graph.getEdgeSource(ongoingEdge); ongoingEdges[landingNode.getToFloorIndex()] = ongoingEdge; }
    * 
    * indexBuilder.boardingEdges(boardingEdges) .landingEdges(landingEdges) .ongoingEdges(ongoingEdges)
    * .travelEdges(travelEdges); }
    */

   protected abstract TravelGraph allocateTravelGraph(TravelGraphIndex graphIndex);
}
