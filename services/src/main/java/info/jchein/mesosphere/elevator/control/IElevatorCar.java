package info.jchein.mesosphere.elevator.control;


import info.jchein.mesosphere.elevator.common.DirectionOfTravel;


/**
 * The inward facing interface the software adapter for an elevator car provides for the elevator group control's use.
 * It very closely resembles the Driver API because most such operations are delegated to the embedded hardware
 * implementation.
 * 
 * @author jheinnic
 */
interface IElevatorCar
{
   public static final String FACTORY_BEAN_NAME = "mesosphere.elevator.ElevatorCarFactory";


   void notifyFloorSensorTriggered(int floorIndex, DirectionOfTravel direction);


   void confirmNextDispatch(int floorIndex);


   void acceptPickupRequest(int floorIndex, DirectionOfTravel direction);


   void removePickupRequest(int floorIndex, DirectionOfTravel direction);


   static final class States
   {
      private States() {}

      public static final String BEFORE_ALLOCATION = "StateBeforeAllocation";
      public static final String WAITING_FOR_DRIVER = "StateWaitingForDriver";
      public static final String AVAILABLE = "StateAvailable";
      public static final String TRAVELLING = "StateTraveling";
      public static final String SLOWING = "StateSlowing";
      public static final String LANDING = "StateLanding";
      public static final String OPENING_DOORS = "StateOpeningDoors";
      public static final String BOARDING = "StateBoarding";
      public static final String CLOSING_DOORS = "StateClosingDoors";
      public static final String SAFETY_LOCKOUT = "StateSafetyLockout";
      public static final String HANDLING_CLOCK = "StateHandlingClock";
   }


   static final class Events
   {
      private Events() {}

      public static final String ALLOCATED = "EventAllocated";
      public static final String DRIVER_INITIALIZED = "EventDriverSentBootstrap";
      public static final String PARKED = "EventParked";
      public static final String DISPATCHED = "EventDispatched";
      public static final String LANDING_BRAKE_APPLIED = "EventLandingBrakeApplied";
      public static final String TRIGGERED_FLOOR_SENSOR = "EventTriggeredFloorSensor";
      public static final String TRAVELLED_THROUGH_FLOOR = "EventTravelledThroughFloor";
      public static final String ARRIVED_AT_FLOOR = "EventArrivedAtFloor";
      public static final String STOPPED_AT_LANDING = "EventStoppedAtLanding";
      public static final String ANSWERED_CALL = "EventAnsweredCall";
      public static final String DOOR_ACTIVATED = "EventDoorActivated";
      public static final String DOOR_OPENED = "EventDoorOpened";
      public static final String DOOR_CLOSED = "EventDoorClosed";
      public static final String DROPOFF_REQUESTED = "EventDropOffRequested";
      public static final String ACCEPTED_PICKUP_REQUEST = "EventAcceptedPickupRequest";
      public static final String DROPPED_PICKUP_REQUEST = "EventDroppedPickupRequest";
      public static final String WEIGHT_UPDATED = "EventWeightUpdated";
      public static final String RESUME_AVAILABLE = "EventResumeAvailable";
      public static final String RESUME_TRAVELLING = "EventResumeTravelling";
      public static final String RESUME_BOARDING = "EventResumeTravelling";
      public static final String PANICKED = "EventPanicked";
   }
}
