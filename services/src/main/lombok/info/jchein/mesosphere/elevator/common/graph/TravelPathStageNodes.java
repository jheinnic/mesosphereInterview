package info.jchein.mesosphere.elevator.common.graph;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(doNotUseGetters=true)
public class TravelPathStageNodes
{
   final InitialPickupStep currentPickupNode;
   
   @Singular("ongoingNode")
   final ImmutableList<OngoingRidersStep> ongoingNodes;

   @Singular("departureNode")
   final ImmutableList<FinalDropOffStep> departureNodes;
}
