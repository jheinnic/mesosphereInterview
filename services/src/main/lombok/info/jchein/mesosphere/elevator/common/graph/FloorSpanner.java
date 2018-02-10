package info.jchein.mesosphere.elevator.common.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class FloorSpanner extends DefaultWeightedEdge
{
   private static final long serialVersionUID = -7979437908338452135L;

   private final int fromFloorIndex;
   private final int toFloorIndex;
   
   FloorSpanner(ISchedulingVertex source, ISchedulingVertex target, double weight) {
      super();
      
      if (source.getNodeType() == SchedulingNodeType.PATH_TRACKING) {
         this.fromFloorIndex = ((CarServiceState) source).getFloorIndex();

         if (target.getNodeType() == SchedulingNodeType.PATH_TRACKING) {
            this.toFloorIndex = ((CarServiceState) target).getFloorIndex();
         } else {
            this.toFloorIndex = -1;
         }
      } else {
         this.fromFloorIndex = -1;
         this.toFloorIndex = ((CarServiceState) target).getFloorIndex();
      }
   }
}
