package info.jchein.mesosphere.elevator.control.model;

package info.jchein.mesosphere.elevator.control.model;


import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.graph.CarServiceState;
import info.jchein.mesosphere.elevator.common.graph.IGraphFactory;
import info.jchein.mesosphere.elevator.common.graph.IVertexFactory;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.control.event.PickupCallRemoved;
import info.jchein.mesosphere.elevator.control.event.TravelledThroughFloor;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.model.ICallDispatcher;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorDispatcherPort;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorDispatchingStrategy;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;


@SuppressWarnings("all")
public class CallDispatcher
implements ICallDispatcher, IElevatorDispatcherPort
{
   private IElevatorDispatchingStrategy strategy;

   private GraphBuilder<?, ?, DefaultDirectedWeightedGraph<?, ?>> graphBuilder;

   public CallDispatcher( final DeploymentConfiguration deployConfig, final IElevatorPhysicsService physicsService )
   {
      final int numFloors = deployConfig.building.numFloors;
      final int numElevators = deployConfig.building.numElevators;
   }


   public void attachStrategy(final IElevatorDispatchingStrategy strategy)
   {
      Preconditions.checkState(
         (this.strategy == null),
         "Elevator Dispatcher already has an attached strategy object");
      this.strategy = strategy;
   }


   @Override
   public void
   assignPickupCar(final int floorIndex, final DirectionOfTravel initialDirection, final int carIndex)
   {}


   @Subscribe
   public Object updateWeightLoad(final WeightLoadUpdated event)
   {
      return null;
   }


   @Subscribe
   public Object onPickupCallAdded(final PickupCallAdded event)
   {
      return null;
   }


   @Subscribe
   public Object onPickupCallRemoved(final PickupCallRemoved event)
   {
      return null;
   }


   @Subscribe
   public Object onDropOffRequested(final DropOffRequested event)
   {
      return null;
   }


   @Subscribe
   public Object onParkedAtLanding(final ParkedAtLanding event)
   {
      return null;
   }


   @Subscribe
   public Object onDepartedLanding(final DepartedLanding event)
   {
      return null;
   }


   @Subscribe
   public Object onPassengerDoorsOpened(final PassengerDoorsOpened event)
   {
      return null;
   }


   @Subscribe
   public Object onPassengerDoorsClosed(final PassengerDoorsClosed event)
   {
      return null;
   }


   @Subscribe
   public Object onTravelledThroughFloor(final TravelledThroughFloor event)
   {
      return null;
   }
}
