package info.jchein.mesosphere.elevator.emulator.physics;


import java.util.Comparator;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.scheduler.ScheduledStop;


public class TravelPathDownComparator
implements Comparator<ScheduledStop>
{
   private final int originFloor;


   public TravelPathDownComparator( int originFloor ) 
   {
      this.originFloor = originFloor;

      Preconditions.checkArgument(originFloor >= 0);
   }


   public int compare(ScheduledStop o1, ScheduledStop o2) 
   {
      final int o1Floor = o1.getFloorIndex();
      final int o2Floor = o2.getFloorIndex();
      if (o1Floor == this.originFloor) {
         if (o2Floor == this.originFloor) { 
            if (o1.hasDirection() && o1.getDirection() == DirectionOfTravel.GOING_UP) {
               if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_UP) {
                  return 0;
               } else {
                  return 1;
               }
            } else if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_UP) {
               return -1;
            } else {
               return 0;
            }
         } else if (o2Floor > this.originFloor) {
            return -1;
         } else if (o1.hasDirection() && o1.getDirection() == DirectionOfTravel.GOING_UP) {
            return 1;  
         } else {
            return -1;
         }
      } else if (o1Floor > this.originFloor) {
         if (o2Floor <= this.originFloor) {
            return 1;
         } else {
            // o1 and o2 are both behind the initial direction of travel
            if (o1Floor < o2Floor) {
               if (o1.hasDirection() && o1.getDirection() == DirectionOfTravel.GOING_DOWN) {
                  return 1;
               } else {
                  return -1;
               }
            } else if (o1Floor > o2Floor) {
               if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_DOWN) {
                  return -1;
               } else {
                  return 1;
               }
            } else if (o1.hasDirection() && o1.getDirection() == DirectionOfTravel.GOING_DOWN) {
               if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_DOWN) {
                  return 0;
               } else {
                  return 1;
               }
            } else if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_DOWN) {
               return -1;
            } else {
               return 0;
            }
         }
      } else if (o2Floor > this.originFloor) {
         // o1 is ahead of the direction of travel, o2 is behind.  We will always reach o1 before o2,
         // regardless of directions of travel.
         return -1;
      } else if (o2Floor == this.originFloor) {
         if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_UP) {
            return -1;
         } else {
            return 1;
         }
      } else if (o1.hasDirection() && o1.getDirection() == DirectionOfTravel.GOING_UP) {
         if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_UP) {
            if (o1Floor < o2Floor) {
               return -1;
            } else if (o2Floor < o1Floor) {
               return 1;
            } else {
               return 0;
            }
         } else {
            return 1;
         }
      } else if (o2.hasDirection() && o2.getDirection() == DirectionOfTravel.GOING_UP) {
         return -1;
      } else if (o1Floor > o2Floor) {
         return -1;
      } else if (o2Floor > o1Floor) {
         return 1;
      } else {
         return 0;
      }
   }
}
