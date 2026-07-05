package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;

import info.jchein.mesosphere.elevator.simulator.passengers.CommonEvents.CommonEventNames;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonStates.CommonStateNames;

public final class WithLobbyReturn
{
   private WithLobbyReturn() { }
   
   public static final class States {
      public static final String BEFORE_ARRIVAL = CommonStateNames.BEFORE_ARRIVAL;
      public static final String QUEUED_FOR_PICKUP = CommonStateNames.QUEUED_FOR_PICKUP;
      public static final String RIDING_ELEVATOR = CommonStateNames.RIDING_ELEVATOR;
      public static final String IN_ACTIVITY = "StateInActivity";
      public static final String AFTER_DEPARTURE = CommonStateNames.AFTER_DEPARTURE;
      
      private States() { }
   }
   
   public static final class Events {
      public static final String ENTERED_SIMULATION = CommonEventNames.ENTERED_SIMULATION;
      public static final String REQUESTED_PICKUP = CommonEventNames.REQUESTED_PICKUP;
      public static final String BOARDED_ELEVATOR = CommonEventNames.BOARDED_ELEVATOR;
      public static final String LEFT_ELEVATOR = CommonEventNames.DISEMBARKED_ELEVATOR;
      public static final String FINISHED_ACTIVITY = "EventFinishedActivity";
      public static final String EXITED_SIMULATION = CommonEventNames.EXITED_SIMULATION;
      
      private Events() { }
   }
}
