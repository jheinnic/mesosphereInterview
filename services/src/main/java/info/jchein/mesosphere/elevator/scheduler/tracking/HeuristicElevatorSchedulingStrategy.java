package info.jchein.mesosphere.elevator.scheduler.tracking;


import java.util.BitSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.graph.IVertexFactory;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.control.event.PickupCallRemoved;
import info.jchein.mesosphere.elevator.control.event.TravelledPastFloor;
import info.jchein.mesosphere.elevator.control.sdk.AbstractElevatorSchedulingStrategy;
import info.jchein.mesosphere.elevator.control.sdk.IDispatchPort;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HeuristicElevatorSchedulingStrategy
extends AbstractElevatorSchedulingStrategy
{

   // private ElevatorGroupBootstrap rootProps;
   // private final IElevatorPhysicsService physicsService;
   // private final DeploymentConfiguration deploymentConfig;

   private final IRuntimeScheduler scheduler;
   private UniformIntegerDistribution tempSched;


   public HeuristicElevatorSchedulingStrategy( final IDispatchPort port,
      final DeploymentConfiguration deploymentConfig, final IElevatorPhysicsService physicsService,
      final IVertexFactory vertexFactory, final IRuntimeScheduler scheduler )
   {
      super(port, deploymentConfig, physicsService, vertexFactory);
      // this.deploymentConfig = deploymentConfig;
      // this.physicsService = physicsService;
      this.scheduler = scheduler;
      this.tempSched = new UniformIntegerDistribution(
         0, deploymentConfig.getBuilding().getNumElevators() - 1);

      // this.currentItineraries = new StopItineraryUpdated[bldgProps.getNumElevators()];
   }


   @Override
   public void onPickupCallAdded(PickupCallAdded event)
   {
      this.dispatchPort.assignPickupCall(event.getFloorIndex(), event.getDirection(), this.tempSched.sample());
   }


   @Override
   public void onPickupCallRemoved(PickupCallRemoved event)
   {
      this.dispatchPort.removePickupCall(event.getFloorIndex(), event.getDirection(), this.tempSched.sample());
   }


   @Override
   public void onDropOffRequested(DropOffRequested event)
   {}


   @Override
   public void onParkedAtLanding(ParkedAtLanding event)
   {}


   // @Override
   // public void onSlowedForArrival(SlowedForArrival event) {
   // }

   @Override
   public void onTravelledThroughFloor(TravelledPastFloor event)
   {}


   static class PassengerManifest
   {
      private int pickupFloor;
      private long pickupTime;
      private BitSet originalDestinations;
      private BitSet stopsSincePickup;
      private double weightAtPickup;


      PassengerManifest( int pickupFloor, long pickupTime, BitSet originalDestinations,
         double weightAtPickup )
      {
         this.pickupFloor = pickupFloor;
         this.pickupTime = pickupTime;
         this.originalDestinations = (BitSet) originalDestinations.clone();
         this.stopsSincePickup = new BitSet();
         this.weightAtPickup = weightAtPickup;
      }


      boolean trackCurrentStop(int floorIndex)
      {
         if (this.originalDestinations.get(floorIndex)) {
            this.stopsSincePickup.nextSetBit(floorIndex);
            return this.stopsSincePickup.size() == this.originalDestinations.size();
         }
         return false;
      }
   }


   static class TravelTimes
   {
      private int currentFloor;
      private DirectionOfTravel initialDirection;
      private BitSet dropOffFloors;
      private List<PassengerManifest> onBoard;


      TravelTimes( int currentFloor, DirectionOfTravel initialDirection, BitSet dropOffFloors,
         List<PassengerManifest> onBoard )
      {
         this.currentFloor = currentFloor;
         this.initialDirection = initialDirection;
         this.dropOffFloors = dropOffFloors;
         this.onBoard = onBoard;
      }
   }


   abstract static class TravelPath
   {
      protected final int fromFloor;
      protected final int toFloor;
      protected final JourneyArc pathArcIn;
      protected final JourneyArc pathArcOut;


      protected TravelPath( int fromFloor, int toFloor, JourneyArc pathArcIn, JourneyArc pathArcOut )
      {
         this.fromFloor = fromFloor;
         this.toFloor = toFloor;
         this.pathArcIn = pathArcIn;
         this.pathArcOut = pathArcOut;
      }


      abstract boolean isExisting();


      int fromFloor()
      {
         return this.fromFloor;
      }


      int toFloor()
      {
         return this.toFloor;
      }


      JourneyArc pathArcIn()
      {
         return this.pathArcIn;
      }


      JourneyArc pathArcOut()
      {
         return this.pathArcOut;
      }
   }


   static class ExistingTravelPath
   extends TravelPath
   {
      public boolean isExisting()
      {
         return true;
      }


      private final double cumulativeDuration;


      ExistingTravelPath( int fromFloor, int toFloor, JourneyArc pathArcIn, JourneyArc pathArcOut,
         double cumulativeDuration )
      {
         super(fromFloor, toFloor, pathArcIn, pathArcOut);
         this.cumulativeDuration = cumulativeDuration;
      }


      double getCumulativeDuration()
      {
         return this.cumulativeDuration;
      }
   }


   static class SkippedTravelPath
   extends TravelPath
   {
      public boolean isExisting()
      {
         return false;
      }


      private ImmutableSet<PassengerManifest> affectedPassengers;


      SkippedTravelPath( int fromFloor, int toFloor, JourneyArc pathArcIn, JourneyArc pathArcOut,
         Set<PassengerManifest> affectedPassengers )
      {
         super(fromFloor, toFloor, pathArcIn, pathArcOut);

         this.affectedPassengers =
            ImmutableSet.<PassengerManifest> builder()
               .addAll(affectedPassengers)
               .build();
      }


      ImmutableSet<PassengerManifest> getCumulativeDuration()
      {
         return this.affectedPassengers;
      }
   }
}
