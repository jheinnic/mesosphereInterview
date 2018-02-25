package info.jchein.mesosphere.elevator.scheduler.model;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.common.graph.TravelPathStageNodes;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.elevator.common.physics.JourneyArcMomentSeries;
import info.jchein.mesosphere.elevator.common.physics.PathMoment;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorRoot;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder(toBuilder=true)
public class ElevatorCarPlan {
   private JourneyArcMomentSeries iterableTrajectory;
   private DirectionOfTravel currentDirection;
   private int currentDestination;
   private JourneyArc pathToDestination;
   private PathMoment physicsState;

   private TravelPathStageNodes travelPathNodes;
   
   /*
   @Override
   public void onNext(StopItineraryUpdated arg0)
   {
      this.latestItinerary = arg0;
      if (this.currentDirection != DirectionOfTravel.STOPPED) {
         Preconditions.checkArgument(arg0.getInitialDirection() == this.currentDirection, "Direction violation!");
      }

      // Check for a change in next destination
      if (this.currentDirection == DirectionOfTravel.GOING_UP) {
         int nextClosestFloor = (int) Math.ceil(this.physicsState.getHeight());
         int nextClosestStop = this.latestItinerary.getUpwardStops().nextSetBit(nextClosestFloor);
         if (nextClosestStop < this.destination) {
            double originalDistance = this.trajectory.distance();
            double newArcDistance = originalDistance - (this.metersPerFloor * (nextClosestStop - this.destination));
            if (this.trajectory.getShortestPossibleArc() >= newArcDistance) {
               this.trajectory = this.trajectory.adjustConstantRegion(newArcDistance);
               this.arcIterator = this.trajectory.asMomentIterable(this.tickDuration).iterator();
               PathMoment nextMoment = this.arcIterator.next();
               while(nextMoment.getHeight() < this.physicsState.getHeight()) {
                  nextMoment = this.arcIterator.next();
               }
               this.physicsState = nextMoment;
            } else {
               throw new IllegalArgumentException("Too late to adjust destination to next prescribed destinaation!");
            }
         }
      } else if (this.currentDirection == DirectionOfTravel.GOING_DOWN) {
         int nextClosestFloor = (int) Math.floor(this.physicsState.getHeight());
         int nextClosestStop = this.latestItinerary.getDownwardStops().previousSetBit(nextClosestFloor);
         if (nextClosestStop > this.destination) {
            double originalDistance = this.trajectory.distance();
            double newArcDistance = originalDistance - (this.metersPerFloor * (this.destination - nextClosestFloor));
            if (this.trajectory.getShortestPossibleArc() >= newArcDistance) {
               this.trajectory = this.trajectory.adjustConstantRegion(newArcDistance);
               this.arcIterator = this.trajectory.asMomentIterable(this.tickDuration).iterator();
               PathMoment nextMoment = this.arcIterator.next();
               while(nextMoment.getHeight() > this.physicsState.getHeight()) {
                  nextMoment = this.arcIterator.next();
               }
               this.physicsState = nextMoment;
            } else {
               throw new IllegalArgumentException("Too late to adjust destination to next prescribed destinaation!");
            }
         }
      } else if(this.latestItinerary.getInitialDirection() != DirectionOfTravel.STOPPED) {
        int currentFloor = (int) Math.round(this.physicsState.getHeight());
        if (this.latestItinerary.getInitialDirection() == DirectionOfTravel.GOING_UP) {
           this.destination = this.latestItinerary.getUpwardStops().nextSetBit(currentFloor + 1);
        } else {
           this.destination = this.latestItinerary.getDownwardStops().nextSetBit(currentFloor - 1);
        }
        this.trajectory = this.physics.getTraversalPath(currentFloor, this.destination);
        this.arcIterator = this.trajectory.asMomentIterable(this.tickDuration).iterator();
      }
   }
   */

}
