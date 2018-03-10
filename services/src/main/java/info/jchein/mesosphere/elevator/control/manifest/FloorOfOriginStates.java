package info.jchein.mesosphere.elevator.control.manifest;

public enum FloorOfOriginStates
{
   BEFORE_PICKUP(Names.BEFORE_PICKUP),
   DURING_PICKUP(Names.DURING_PICKUP),
//   TRACKING_REQUESTS(Names.TRACKING_REQUESTS),
//   WAITING_FOR_STOP(Names.WAITING_FOR_STOP),
   MAKING_STOPS(Names.MAKING_STOPS),
//   MARKING_LAST_STOP(Names.MAKING_LAST_STOP),
   FINISHED(Names.FINISHED);

   
   public final String stateName;

   FloorOfOriginStates( String stateName )
   {
      this.stateName = stateName;
   }

   public String asStateName()
   {
      return this.stateName;
   }
   
   public static class Names {
      public static final String BEFORE_PICKUP = "StateBeforeArrival";
      public static final String DURING_PICKUP = "StateDuringPickup";
//      public static final String TRACKING_REQUESTS = "StateTrackingRequests";
//      public static final String WAITING_FOR_STOP = "StateWaitingForStop";
      public static final String MAKING_STOPS = "StateMakingStops";
//      public static final String MAKING_LAST_STOP = "StateMakingLastStop";
      public static final String FINISHED = "StateFinished";
   }
}
