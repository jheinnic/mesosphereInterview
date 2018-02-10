package test;


import java.time.ZoneId;

import info.jchein.mesosphere.elevator.common.bootstrap.BuildingDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.DoorTimeDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.StartStopDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.TravelSpeedDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.WeightDescription;
import info.jchein.mesosphere.elevator.common.physics.ElevatorPhysicsService;
import info.jchein.mesosphere.elevator.common.physics.IPathLeg;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.elevator.common.physics.PathMoment;
import info.jchein.mesosphere.elevator.runtime.virtual.RuntimeClock;
import info.jchein.mesosphere.elevator.runtime.virtual.VirtualRuntimeProperties;
import rx.schedulers.Schedulers;


public class QuickPhysCheck
{
   public static void main(String[] args)
   {
      StartStopDescription motorProps = StartStopDescription.build(bldr -> {
         bldr.brakeDistance(0.3)
            .brakeSpeed(0.5)
            .maxAcceleration(1.73205080756887729352744635)
            .maxJerk(2.0);
      });
      BuildingDescription bldgProps = BuildingDescription.build(bldr -> {
         bldr.numElevators(6)
            .numFloors(12)
            .metersPerFloor(3.75);
      });

      TravelSpeedDescription speedProps = TravelSpeedDescription.build(bldr-> {
         bldr.shortHop(1.25)
            .longDescent(1.75)
            .longAscent(2.0);
      });
      
      WeightDescription weightProps = WeightDescription.build(bldr -> {
         bldr.maxForTravel(3000)
            .pctMaxForIdeal(0.45)
            .pctMaxForPickup(0.88)
            .avgPassenger(75.0);
      });
      
      DoorTimeDescription doorProps = DoorTimeDescription.build(bldr -> {
         bldr.personHold(0.02)
            .minHold(3.2)
            .openCloseTime(2.6);
      });

      DeploymentConfiguration bootstrapData = DeploymentConfiguration.build(bldr -> {
         bldr.topSpeed(speedProps)
            .motor(motorProps)
            .building(bldgProps)
            .weight(weightProps)
            .doors(doorProps);
      });
      
      VirtualRuntimeProperties runtimeProps = new VirtualRuntimeProperties();
      runtimeProps.setTickDurationMillis(100);

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
      for (int ii = 1; ii < 3; ii++) {
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
