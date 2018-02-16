package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractTraveller;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonEvents.CommonEventNames;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TravellerWithLobbyReturn
extends AbstractTraveller<WithLobbyReturnRandomVariables, TravellerWithLobbyReturn>
{
   public static final String ENTERED_SIMULATION = CommonEventNames.ENTERED_SIMULATION;
   public static final String REQUESTED_PICKUP = CommonEventNames.REQUESTED_PICKUP;
   public static final String BOARDED_ELEVATOR = CommonEventNames.BOARDED_ELEVATOR;
   public static final String LEFT_ELEVATOR = CommonEventNames.DISEMBARKED_ELEVATOR;
   public static final String EXITED_SIMULATION = CommonEventNames.EXITED_SIMULATION;

   public static final String FINISHED_ACTIVITY = "EventFinishedActivity";

   @Getter
   @Setter
   @org.statefulj.persistence.annotations.State(accessorType = AccessorType.METHOD,
      setMethodName = "setState", getMethodName = "getState")
   private String state;

   private final State<TravellerWithLobbyReturn> activityState;
   private final int activityFloor;
   private final double activityDuration;
   private int elevatorTripCount = 0;


   @Autowired
   TravellerWithLobbyReturn( @NotNull PassengerId id,
      @NotNull WithLobbyReturnRandomVariables randomVariables,
      FSM<TravellerWithLobbyReturn> stateMachine, State<TravellerWithLobbyReturn> activityState,
      IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus )
   {
      super(id, randomVariables, stateMachine, clock, scheduler, eventBus);

      this.activityState = activityState;
      this.activityFloor = randomVariables.getActivityFloor();
      this.activityDuration = randomVariables.getActivityDuration();

      this.elevatorTripCount = 0;
      this.setCurrentFloor(0);
   }


   @Override
   protected State<TravellerWithLobbyReturn> getInitialState()
   {
      return null;
   }


   @Override
   protected void afterEnteredSimulation()
   {
      this.setDestinationFloor(this.activityFloor, this.activityState);
   }


   @Override
   protected void afterQueuedForPickup()
   {}


   @Override
   protected void afterBoardedElevator(int boardedCarIndex)
   {}


   @Override
   protected void afterDisembarkedElevator()
   {
      if (this.elevatorTripCount == 0) {
         this.elevatorTripCount += 1;
         this.scheduler.scheduleOnce(
            Math.round(this.activityDuration),
            TimeUnit.SECONDS,
            Priorities.TRANSFER_PASSENGERS.getValue(),
            this::returnFromActivity);
      } else if (this.elevatorTripCount == 1) {
         log.info("Completed second elevator trip with lobby return.");
      }
   }


   @SneakyThrows
   void returnFromActivity(long interval)
   {
      this.setDestinationFloor(0, null);
      this.stateMachine.onEvent(this, TravellerWithLobbyReturn.FINISHED_ACTIVITY);
   }


   protected void afterExitedSimulation()
   {

   }

   // void queueForReturn()
   // {
   // this.queueForPickup();
   // }
}
