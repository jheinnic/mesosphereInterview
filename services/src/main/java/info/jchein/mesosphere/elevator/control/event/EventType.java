package info.jchein.mesosphere.elevator.control.event;

public enum EventType
{
   ASSIGNED_PICKUP_CALL,
   CAR_AVAILABLE_SIGNAL_LIT,
   CAR_AVAILABLE_SIGNAL_OFF,
   CHANGED_DIRECTION,
   DEPARTED_LANDING,
   BOOTSTRAPPED_DRIVER,
   DROP_OFF_REQUESTED,
   FLOOR_SENSOR_TRIGGERED,
   UPDATED_ESTIMATED_LOCATION,
   PARKED_AT_LANDING,
   PASSENGER_DOORS_CLOSED,
   PASSENGER_DOORS_OPENED,
   PICKUP_CALL_ADDED,
   PICKUP_CALL_REMOVED,
   SLOWED_FOR_ARRIVAL,
   TRAVELLED_PAST_FLOOR,
   UNASSIGNED_PICKUP_CALL,
   UPDATED_WEIGHT_LOAD;
}
