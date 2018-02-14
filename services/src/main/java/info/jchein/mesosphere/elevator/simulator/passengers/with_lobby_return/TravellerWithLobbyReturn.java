package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.persistence.annotations.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.CompletedTrip;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.event.PickupRequested;
import info.jchein.mesosphere.elevator.simulator.model.ITravellerQueueService;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractTraveller;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonEvents;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonEvents.CommonEventNames;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonStates.CommonStateNames;
import lombok.Getter;
import lombok.Setter;

public class TravellerWithLobbyReturn extends AbstractTraveller<IWithLobbyReturnRandomVariables>
{
   public static final String ENTERED_SIMULATION = CommonEventNames.ENTERED_SIMULATION;
   public static final String REQUESTED_PICKUP = CommonEventNames.REQUESTED_PICKUP;
//   public static final String OBSERVED_CAR_STOP = CommonEventNames.OBSERVED_CAR_STOP;
   public static final String BOARDED_ELEVATOR = CommonEventNames.BOARDED_ELEVATOR;
   public static final String FAILED_TO_BOARD = CommonEventNames.FAILED_TO_BOARD;
   public static final String LEFT_ELEVATOR = CommonEventNames.LEFT_ELEVATOR;
   public static final String EXITED_SIMULATION = CommonEventNames.EXITED_SIMULATION;
   
   @Getter
   @Setter
   @State(accessorType=AccessorType.METHOD, setMethodName="setState", getMethodName="getState")
   private String state;
   
   private int activityFloor;
   private double activityDuration;
   private int elevatorTripCount = 0;

   
   @Autowired
   TravellerWithLobbyReturn(IRuntimeScheduler scheduler, ITravellerQueueService queueService) {
      super(scheduler, queueService);
      this.activityFloor = -1;
      this.activityDuration = -1;
      this.elevatorTripCount = 0;
   }

   public void initRandomVariables(IWithLobbyReturnRandomVariables randomVariables)
   {
      this.activityFloor = randomVariables.getActivityFloor();
      this.activityDuration = randomVariables.getActivityDuration();
      
      // Call the inheritted method we augmented because we want it to extract values from the base interface, IRandomValues,
      // and we want it to emit to the event bus as well.
      super.initRandomVariables(randomVariables);
   }

   @Override
   public void onQueuedForPickup()
   {
      final DirectionOfTravel dir;                                                                             
      if (this.getCurrentFloor() < this.getDestinationFloor()) {
         dir = DirectionOfTravel.GOING_UP;
      } else if (this.getCurrentFloor() > this.getDestinationFloor()) {
         dir = DirectionOfTravel.GOING_DOWN;
      } else {
         throw new IllegalStateException();
      }

      TravellerWithLobbyReturn.this.eventBus.post(
         PickupRequested.build( bldr dmv -> {
            bldr.clockTime(this.clock.now())
            .travellerId(this.getId())
            .populationName(this.stateMachine.getName())
            .floorIndex(this.currentFloorIndex())
            .direction(dir);
         })
      );
   }

   @Override
   public void onSuccessfulPickup(int boardedCarIndex)
   {
   }

   @Override
   public void onSuccessfulDropOff()
   {
      
   }

}
