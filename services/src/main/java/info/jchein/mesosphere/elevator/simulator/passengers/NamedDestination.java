package info.jchein.mesosphere.elevator.simulator.passengers;


/**
 * An enumeration of recurring state names, each of which has a mapping to its common string form when used as a
 * StatefulFSM State.
 * 
 * @author jheinnic
 */
public enum NamedDestination
{
   BEFORE_ARRIVAL(CommonStateNames.BEFORE_ARRIVAL),
   IN_LOBBY(CommonStateNames.IN_LOBBY),
   QUEUED_FOR_PICKUP(CommonStateNames.QUEUED_FOR_PICKUP),
   RIDING_ELEVATOR(CommonStateNames.RIDING_ELEVATOR),
   IN_ACTIVITY(CommonStateNames.IN_ACTIVITY),
   AFTER_DEPARTURE(CommonStateNames.AFTER_DEPARTURE);

   
   public final String stateName;

   NamedDestination( String stateName )
   {
      this.stateName = stateName;
   }

   public String asStateName()
   {
      return this.stateName;
   }
   
   public static class CommonStateNames {
      public static final String BEFORE_ARRIVAL = "StateBeforeArrival";
      public static final String IN_LOBBY = "StateInLobby";
      public static final String QUEUED_FOR_PICKUP = "StateQueuedForPickup";
      public static final String RIDING_ELEVATOR = "StateRidingElevator";
      public static final String IN_ACTIVITY = "StateInActivity";
      public static final String AFTER_DEPARTURE = "StateAfterDeparture";
   }
}
