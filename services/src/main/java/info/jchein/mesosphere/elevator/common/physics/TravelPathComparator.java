package info.jchein.mesosphere.elevator.common.physics;


import java.util.Comparator;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledStop;


public class TravelPathComparator
implements Comparator<ScheduledStop>
{
   private final int numFloors;
   private final int originFloor;
   private final DirectionOfTravel outbound;


   public TravelPathComparator( @NotNull DirectionOfTravel outbound, int originFloor, int numFloors ) 
   {
      Preconditions.checkArgument(originFloor >= 0);
      Preconditions.checkArgument(originFloor < numFloors);
      Preconditions.checkArgument(outbound == DirectionOfTravel.GOING_UP || outbound == DirectionOfTravel.GOING_DOWN);

      this.numFloors = numFloors;
      this.originFloor = originFloor;
      this.outbound = outbound;
   }


   @Override
   public int compare(ScheduledStop o1, ScheduledStop o2)
   {
      Preconditions.checkNotNull(o1);
      Preconditions.checkNotNull(o2);
      Preconditions.checkArgument(0 <= o1.getFloorIndex() && o1.getFloorIndex() < this.numFloors);
      Preconditions.checkArgument(0 <= o2.getFloorIndex() && o2.getFloorIndex() < this.numFloors);

      if (o1 == o2) {
         return 0;
      }

      final DirectionOfTravel originOutbound = this.outbound;
      final DirectionOfTravel o1Outbound = o1.getOutbound();
      final DirectionOfTravel o2Outbound = o2.getOutbound();
      
      // If the main direction is GOING_DOWN, use the negative for all floor indices.  This inversion transforms all floor-to-floor
      // comparison semantics such that they become identical to the GOING_UP semantics with positive floor values.  This get rid of
      // a whole lot of redundant branching!
      final int originFloor = originOutbound == DirectionOfTravel.GOING_UP ? this.originFloor : this.originFloor * -1;
      final int o1Floor = originOutbound == DirectionOfTravel.GOING_UP ? o1.getFloorIndex() : o1.getFloorIndex() * -1;
      final int o2Floor = originOutbound == DirectionOfTravel.GOING_UP ? o2.getFloorIndex() : o2.getFloorIndex() * -1;
      
//      if (o1Outbound == o2Outbound) {
//         if (
//      }
      
      if (o1Floor == originFloor) {
         if (o2Floor == originFloor) { 
            if (o1Outbound == originOutbound) {
               if (o2Outbound == originOutbound) {
                  return 0;
               } else {
                  return -1;
               }
            } else if (o2Outbound == originOutbound) {
               return 1;
            } else {
               return 0;
            }
         } else if (o1Outbound == originOutbound) {
            return -1;  
         } else {
            // TODO: It's not this simple...
            return 1;
         }
      } else if (o2Floor == originFloor) {
         if (o2Outbound == originOutbound) {
            return 1;  
         } else {
            return -1;
         }
         
      } else if (o1Floor < originFloor) {
         if (o2Floor >= originFloor) {
            // If o2 is undirected or directed and up, then it is the very first path element.
            // Otherwise, it is the first we hit in the opposite direction.  o1 is somewhere in the
            // reverse direction path, not the start, therefore o2 is always earlier.
            // -Or-
            // o2 is ahead of the direction of travel, o1 is behind.  We will always reach o2 before o1,
            // regardless of directions of travel.
            return -1;
         } else {
            // o1 and o2 are both behind the initial direction of travel
            if (o1Floor > o2Floor) {
               if (o1Outbound != originOutbound) {
                  return -1;
               } else {
                  return 1;
               }
            } else if (o1Floor < o2Floor) {
               if (o2Outbound != originOutbound) {
                  return 1;
               } else {
                  return -1;
               }
            } else if (o1Outbound != originOutbound) {
               if (o2Outbound != originOutbound) {
                  return 0;
               } else {
                  return -1;
               }
            } else if (o2Outbound != originOutbound) {
               return 1;
            } else {
               return 0;
            }
         }
      } else if (o2Floor < originFloor) {
         // o1 is ahead of the direction of travel, o2 is behind.  We will always reach o1 before o2,
         // regardless of directions of travel.
         return 1;
      } else if (o2Floor == originFloor) {
         if (o2Outbound == originOutbound) {
            return 1;
         } else {
            return -1;
         }
      } else if (o1Outbound == originOutbound) {
         if (o2Outbound == originOutbound) {
            if (o1Floor > o2Floor) {
               return 1;
            } else if (o2Floor > o1Floor) {
               return -1;
            } else {
               return 0;
            }
         } else {
            return -1;
         }
      } else if (o2Outbound == originOutbound) {
         return 1;
      } else if (o1Floor < o2Floor) {
         return 1;
      } else if (o2Floor < o1Floor) {
         return -1;
      } else {
         return 0;
      }
   }
}
