package info.jchein.mesosphere.elevator.simulator.passengers;


/**
 * An enumeration of recurring state names, each of which has a mapping to its common string form when used as a
 * StatefulFSM State.
 * 
 * @author jheinnic
 */
public enum CommonEvents
{
   ENTERED_SIMULATION(CommonEventNames.ENTERED_SIMULATION),
   REQUESTED_PICKUP(CommonEventNames.REQUESTED_PICKUP),
   OBSERVED_CAR_STOP(CommonEventNames.OBSERVED_CAR_STOP),
   FAILED_TO_BOARD(CommonEventNames.FAILED_TO_BOARD),
   BOARDED_ELEVATOR(CommonEventNames.BOARDED_ELEVATOR),
   LEFT_ELEVATOR(CommonEventNames.LEFT_ELEVATOR),
   BEGAN_ACTIVITY(CommonEventNames.BEGAN_ACTIVITY),
   EXITED_SIMULATION(CommonEventNames.EXITED_SIMULATION);

   
   public final String stateName;

   CommonEvents( String stateName )
   {
      this.stateName = stateName;
   }

   public String asStateName()
   {
      return this.stateName;
   }
   
   public static class CommonEventNames {
      public static final String ENTERED_SIMULATION = "EventEnteredSimulation";
      public static final String REQUESTED_PICKUP = "EventRequestedPickup";
      public static final String OBSERVED_CAR_STOP = "EventObservedCarStop";
      public static final String FAILED_TO_BOARD = "EventFailedToBoard";
      public static final String BOARDED_ELEVATOR = "EventBoardedElevator";
      public static final String LEFT_ELEVATOR = "EventLeftElevator";
      public static final String BEGAN_ACTIVITY = "EventBeganActivity";
      public static final String EXITED_SIMULATION = "EventExitedSimulation";
   }
}
