package fixtures.elevator.common.physics;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledStop;

public class ScheduledStopFixture
{
   public static final DirectionOfTravel UP = DirectionOfTravel.GOING_UP;
   public static final DirectionOfTravel DOWN = DirectionOfTravel.GOING_DOWN;
   public static final int NUM_FLOORS = 10;

   public static final ScheduledStop[] UP_UP_DUAL = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] DOWN_UP_DUAL = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] UP_DOWN_DUAL = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] DOWN_DOWN_DUAL = new ScheduledStop[NUM_FLOORS];

   public static final ScheduledStop[] UP_UP_DROP = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] DOWN_UP_DROP = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] UP_DOWN_DROP = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] DOWN_DOWN_DROP = new ScheduledStop[NUM_FLOORS];

   public static final ScheduledStop[] UP_UP_PICKUP = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] DOWN_UP_PICKUP = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] UP_DOWN_PICKUP = new ScheduledStop[NUM_FLOORS];
   public static final ScheduledStop[] DOWN_DOWN_PICKUP = new ScheduledStop[NUM_FLOORS];
   
   static {
      for( int ii=0; ii<NUM_FLOORS; ii++ ) {
         final int floorIndex = ii;
         if (ii > 0) {
            UP_UP_DROP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(UP).outbound(UP).dropOff(true).pickUp(false);
            });
            UP_UP_PICKUP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(UP).outbound(UP).dropOff(false).pickUp(true);
            });
            UP_UP_DUAL[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(UP).outbound(UP).dropOff(true).pickUp(true);
            });

            UP_DOWN_DROP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(UP).outbound(DOWN).dropOff(true).pickUp(false);
            });
            UP_DOWN_PICKUP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(UP).outbound(DOWN).dropOff(false).pickUp(true);
            });
            UP_DOWN_DUAL[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(UP).outbound(DOWN).dropOff(true).pickUp(true);
            });
         }
         
         if (ii < (NUM_FLOORS - 1)) {
            DOWN_DOWN_DROP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(DOWN).outbound(DOWN).dropOff(true).pickUp(false);
            });
            DOWN_DOWN_PICKUP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(DOWN).outbound(DOWN).dropOff(false).pickUp(true);
            });
            DOWN_DOWN_DUAL[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(DOWN).outbound(DOWN).dropOff(true).pickUp(true);
            });

            DOWN_UP_DROP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(DOWN).outbound(UP).dropOff(true).pickUp(false);
            });
            DOWN_UP_PICKUP[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(DOWN).outbound(UP).dropOff(false).pickUp(true);
            });
            DOWN_UP_DUAL[ii] = ScheduledStop.build(bldr -> {
               bldr.floorIndex(floorIndex).inbound(DOWN).outbound(UP).dropOff(true).pickUp(true);
            });
         }
         
      }
   }
}
