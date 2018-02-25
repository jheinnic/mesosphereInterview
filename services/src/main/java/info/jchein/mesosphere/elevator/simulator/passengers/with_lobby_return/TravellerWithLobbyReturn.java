package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.AbstractTraveller;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ToString
public class TravellerWithLobbyReturn
extends AbstractTraveller<VariablesWithLobbyReturn, TravellerWithLobbyReturn>
{
   @Getter
   @Setter
   @org.statefulj.persistence.annotations.State(accessorType = AccessorType.METHOD,
      setMethodName = "setState", getMethodName = "getState")
   private String state;

   private final State<TravellerWithLobbyReturn> activityState;
   private final int activityFloor;
   private final double activityDurationSeconds;
   private int elevatorTripCount = 0;


   @Autowired
   TravellerWithLobbyReturn( @NotNull PassengerId id,
      @NotNull VariablesWithLobbyReturn randomVariables,
      FSM<TravellerWithLobbyReturn> stateMachine, State<TravellerWithLobbyReturn> activityState,
      IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus )
   {
      super(id, randomVariables, stateMachine, clock, scheduler, eventBus);

      this.activityState = activityState;
      this.activityFloor = randomVariables.getActivityFloor();
      this.activityDurationSeconds = randomVariables.getActivitySeconds();

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
            Math.round(this.activityDurationSeconds),
            TimeUnit.SECONDS,
            Priorities.TRANSFER_PASSENGERS.getValue(),
            this::returnFromActivity);
      } else if (this.elevatorTripCount == 1) {
         log.info("Completed second elevator trip with lobby return.");
      }
   }


   @Override
   protected void afterExitedSimulation()
   { }


   @SneakyThrows
   void returnFromActivity(long interval)
   {
      this.setDestinationFloor(0, null);
      this.stateMachine.onEvent(this, WithLobbyReturn.Events.FINISHED_ACTIVITY);
   }
}
