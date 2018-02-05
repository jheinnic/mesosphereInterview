package test;


import java.time.ZoneId;

import info.jchein.mesosphere.elevator.common.bootstrap.BuildingProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.PhysicalDispatchContext;
import info.jchein.mesosphere.elevator.emulator.physics.ElevatorPhysicsService;
import info.jchein.mesosphere.elevator.emulator.physics.IPathLeg;
import info.jchein.mesosphere.elevator.emulator.physics.JourneyArc;
import info.jchein.mesosphere.elevator.emulator.physics.PathMoment;
import info.jchein.mesosphere.elevator.runtime.virtual.RuntimeClock;
import info.jchein.mesosphere.elevator.runtime.virtual.VirtualRuntimeProperties;
import rx.schedulers.Schedulers;


public class QuickPhysCheck
{
   public static void main(String[] args)
   {
      ElevatorMotorProperties motorProps = ElevatorMotorProperties.build(bldr -> {
         bldr.brakingDistance(0.3)
            .brakingSpeed(0.5)
            .maxAcceleration(1.73205080756887729352744635)
            .maxJerk(2.0)
            .shortTravelSpeed(1.25)
            .longDescentSpeed(1.75)
            .longAscentSpeed(2.0);
      });
      BuildingProperties bldgProps = BuildingProperties.build(bldr -> {
         bldr.numElevators(6)
            .numFloors(12)
            .metersPerFloor(3.75);
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
      
      VirtualRuntimeProperties runtimeProps = new VirtualRuntimeProperties(100);

      RuntimeClock clock = new RuntimeClock(ZoneId.systemDefault(), Schedulers.test(), runtimeProps);
      
      ElevatorPhysicsService physicsService = new ElevatorPhysicsService(bootstrapData, clock);

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
                  arc.getDuration()));
         }
      }

      System.out.println("from,to,time,height,velocity,acceleration,jerk");
      for (int ii = 1; ii < 11; ii++) {
         final JourneyArc anArc = physicsService.getTraversalPath(ii, ii + 1);
         double lastDuration = anArc.getDuration();
         for (int jj = ii+1; jj < 12; jj++) {
            final JourneyArc arc = physicsService.getTraversalPath(jj, ii);
            final double nextDuration = arc.getDuration();
            final double delta = nextDuration - lastDuration;
            lastDuration = nextDuration;

            System.out.println(
               String.format("From %d to %d, time delta is %f", ii, jj, delta));
            for (final IPathLeg nextLeg : arc) { System.out.println(nextLeg.toString()); }
            System.out.println("\n\n");

            /*
            for (final PathMoment nextMoment : arc.asMomentIterable(0.0333)) {
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
            */
         }
      }
   }
}
