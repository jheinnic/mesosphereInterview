package info.jchein.mesosphere.elevator.control.manifest;

public enum FloorOfOriginEvents
{
   DOORS_OPENED(Names.DOORS_OPENED),
   DOORS_CLOSED(Names.DOORS_CLOSED),
   WEIGHT_INCREASED(Names.WEIGHT_INCREASED),
   WEIGHT_DECREASED(Names.WEIGHT_DECREASED),
   DROP_REQUESTED(Names.DROP_REQUESTED);
//   BEGAN_PICKUP(Names.BEGAN_PICKUP),
//   BEGAN_WAITING(Names.BEGAN_WAITING),
//   NOT_MY_STOP(Names.NOT_MY_STOP),
//   POTENTIAL_STOP(Names.POTENTIAL_STOP),
//   FINAL_STOP(Names.FINAL_STOP);

   public final String eventName;

   FloorOfOriginEvents( String eventName )
   {
      this.eventName = eventName;
   }

   public String asEventName()
   {
      return this.eventName;
   }
   
   public static class Names {
      public static final String DOORS_OPENED = "EventDoorsOpened";
      public static final String DOORS_CLOSED = "EventDoorsClosed";
//      public static final String BEGAN_PICKUP = "EventBeganPickup";
//      public static final String BEGAN_WAITING = "EventBeganWaiting";
//      public static final String NOT_MY_STOP = "EventNotMyStop";
//      public static final String POTENTIAL_STOP = "EventPotentialStop";
//      public static final String FINAL_STOP = "EventFinalStop";
      public static final String WEIGHT_INCREASED = "EventWeightIncreased";
      public static final String WEIGHT_DECREASED = "EventWeightDecreased";
      public static final String DROP_REQUESTED = "EventDropRequested";
   }
}
