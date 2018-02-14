package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.persistence.annotations.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.CompletedTrip;
import info.jchein.mesosphere.elevator.common.IValueFactory;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.control.model.ElevatorCar;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import lombok.Getter;
import lombok.Setter;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTraveller;
import info.jchein.mesosphere.elevator.simulator.model.ITravellerQueueService;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractSimulatedTraveller;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonEvents.CommonEventNames;
import info.jchein.mesosphere.elevator.simulator.passengers.NamedDestination.CommonStateNames;
import info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return.IWithLobbyReturnRandomVariables;

@StatefulController(
   value=WithLobbyReturnTraveller.BEAN_NAME,
   clazz = WithLobbyReturnTraveller.class,
   startState = CommonStateNames.BEFORE_ARRIVAL, 
   blockingStates = { CommonStateNames.BEFORE_ARRIVAL },
   noops = {
      @Transition(from=CommonStateNames.BEFORE_ARRIVAL, event=CommonEventNames.ENTERED_SIMULATION, to=CommonStateNames.IN_LOBBY)
   }
)
public class WithLobbyReturnTraveller extends AbstractSimulatedTraveller<IWithLobbyReturnRandomVariables>
{
   public static final String BEAN_NAME = "withLobbyReturnTraveller";
   
   public static final String BEFORE_ARRIVAL = CommonStateNames.BEFORE_ARRIVAL;
   public static final String IN_LOBBY = CommonStateNames.IN_LOBBY;
   public static final String QUEUED_FOR_PICKUP = CommonStateNames.QUEUED_FOR_PICKUP;
   public static final String RIDING_ELEVATOR = CommonStateNames.RIDING_ELEVATOR;
   public static final String IN_ACTIVITY = CommonStateNames.IN_ACTIVITY;
   public static final String AFTER_DEPARTURE = CommonStateNames.AFTER_DEPARTURE;
   
   public static final String ENTERED_SIMULATION = CommonEventNames.ENTERED_SIMULATION;
   public static final String REQUESTED_PICKUP = CommonEventNames.REQUESTED_PICKUP;
   public static final String OBSERVED_CAR_STOP = CommonEventNames.OBSERVED_CAR_STOP;
   public static final String BOARD_ELEVATOR = CommonEventNames.BOARDED_ELEVATOR;
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
   WithLobbyReturnTraveller(IRuntimeScheduler scheduler, ITravellerQueueService queueService) {
      super(scheduler, queueService);
      this.activityFloor = -1;
      this.activityDuration = -1;
      this.elevatorTripCount = 0;
   }

   @Transition(from=CommonStateNames.BEFORE_ARRIVAL, event=CommonEventNames.ENTERED_SIMULATION, to=CommonStateNames.IN_LOBBY)
   public void onArrival(String eventName, IWithLobbyReturnRandomVariables randomVariables) {
//      this.arrivalTime = this.clock.now();
      this.activityFloor = activityFloor;
      this.activityDuration = activityDuration;
      this.initVariables(randomVariables);
      this.callForPickup(this.activityFloor);
   }

   @Override
   public PassengerId getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void onSuccessfulDropOff(CompletedTrip tripSummary)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   <P> void fireCommonEvent(CommonEvents eventType, P parameter)
   {
      // TODO Auto-generated method stub
      
   }
   
//   @Transition(from=IN_LOBBY)
}
