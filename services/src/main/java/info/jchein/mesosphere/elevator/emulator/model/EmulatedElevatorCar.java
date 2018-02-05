package info.jchein.mesosphere.elevator.emulator.model;

import java.util.Iterator;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.InitialElevatorCarState;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.control.sdk.StopItineraryUpdated;
import info.jchein.mesosphere.elevator.emulator.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.emulator.physics.JourneyArc;
import info.jchein.mesosphere.elevator.emulator.physics.PathMoment;
import rx.Observer;

@Component
public class EmulatedElevatorCar implements IElevatorCarDriver, Observer<StopItineraryUpdated> {
	private final IElevatorCarPort port;
   private final IElevatorPhysicsService physics;

	private StopItineraryUpdated latestItinerary;
	private PathMoment physicsState;
	private Iterator<PathMoment> arcIterator;
	private JourneyArc trajectory;
	private DirectionOfTravel currentDirection;
	private int destination;
   private double metersPerFloor;
   private double tickDuration;
   private InitialElevatorCarState initialState;
	
	public EmulatedElevatorCar(IElevatorCarPort port, IElevatorPhysicsService physics, InitialElevatorCarState initialState)
	{
		this.port = port;
      this.physics = physics;
      this.initialState = initialState;
      this.metersPerFloor = physics.getMetersPerFloor();
	}

   @Override
   public InitialElevatorCarState initialize()
   {
      return this.initialState;
   }

   @Override
   public void travelTo(int floorIndex)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void openDoors()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void onCompleted()
   {
      System.out.println("Simulation shutdown?");
   }

   @Override
   public void onError(Throwable arg0)
   {
      System.out.println(String.format("Unexpected error: %s", arg0));
   }

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
}
