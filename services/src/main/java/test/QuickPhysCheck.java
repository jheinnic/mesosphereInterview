package test;


import info.jchein.mesosphere.elevator.domain.common.BuildingProperties;
import info.jchein.mesosphere.elevator.domain.common.PhysicalDispatchContext;
import info.jchein.mesosphere.elevator.domain.common.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap;
import ElevatorPhysicsService;
import JourneyArc;
import PathLeg;
import PathMoment;


public class QuickPhysCheck
{
   public static void main(String[] args)
   {
      ElevatorMotorProperties motorProps = ElevatorMotorProperties.build(bldr -> {
         bldr.brakingDistance(0.3)
            .brakingSpeed(0.5)
            .maxAcceleration(1.4)
            .maxJerk(2.0)
            .shortTravelSpeed(1.25)
            .longDescentSpeed(1.75)
            .longAscentSpeed(2.0);
      });
      BuildingProperties bldgProps = BuildingProperties.build(bldr -> {
         bldr.numElevators(6)
            .numFloors(12)
            .metersPerFloor(3.5);
      });
      PhysicalDispatchContext dispatchProps = PhysicalDispatchContext.build(bldr -> {
         bldr.doorHoldTimePerPerson(0.02)
            .doorOpenCloseSlideTime(2.6)
            .minDoorHoldTimePerOpen(3.2)
            .idealWeightLoad(0.45)
            .passengerWeight(75.0);
      });

      ElevatorGroupBootstrap bootstrapData = ElevatorGroupBootstrap.build(bldr -> {
         bldr.dispatch(dispatchProps)
            .motor(motorProps)
            .building(bldgProps);
      });

      ElevatorPhysicsService physicsService = new ElevatorPhysicsService(bootstrapData);

      /*
      for (int ii = 0; ii < 12; ii++) {
         for (int jj = ii + 1; jj < 12; jj++) {
            System.out.println(String.format("<%d> to <%d>", ii, jj));
            JourneyArc arc = physicsService.getTraversalPath(ii, jj);
            System.out.println(
               String.format(
                  "From %d to %d: %f meters in %f seconds",
                  ii,
                  jj,
                  arc.distance(),
                  arc.duration()));
         }
      }
      */

      System.out.println("from,to,time,height,velocity,acceleration,jerk");
      for (int ii = 1; ii < 11; ii++) {
//         JourneyArc anArc = physicsService.getTraversalPath(ii, ii + 1);
//         double lastDuration = anArc.duration();
         for (int jj = ii+1; jj < 12; jj++) {
            JourneyArc arc = physicsService.getTraversalPath(jj, ii);
//            double delta = arc.duration() - lastDuration;
//            lastDuration = arc.duration();
            /*
             System.out.println(
             String.format("From %d to %d, time delta is %f", ii, jj, delta));
             for (final PathLeg nextLeg : arc.legIterator()) { System.out.println(nextLeg.toString()); }
             System.out.println("\n\n");
            */
            for (final PathMoment nextMoment : arc.momentIterator(0.0333)) {
               System.out.println(
                  String.format(
                     "%d,%d,%f,%f,%f,%f,%f",
                     jj, ii,
                     nextMoment.getTime(),
                     nextMoment.getHeight(),
                     nextMoment.getVelocity(),
                     nextMoment.getAcceleration(),
                     nextMoment.getJerk()));
            }
         }
      }

      /*
       * for (final PathLeg nextLeg : anArc.legIterator()) { System.out.println(nextLeg.toString()); } for (final
       * PathMoment nextMoment : anArc.momentIterator(0.00015)) { System.out.println( String.format( "%f,%f,%f,%f,%f",
       * nextMoment.getTime(), nextMoment.getHeight(), nextMoment.getVelocity(), nextMoment.getAcceleration(),
       * nextMoment.getJerk())); }
       */
   }
}
