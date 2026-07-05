package info.jchein.mesosphere.elevator.control.sdk;

public enum Priorities
{
   STEP_DRIVERS(-10000),
   SIMULATE_ARRIVALS(-8000),
   SCHEDULE_LANDING(-5000),
   OPEN_DOORS(-3000),
   TRANSFER_PASSENGERS(-1000),
   CLOSE_DOORS(3000),
   SCHEDULE_DISPATCH(5000),
   SIMULATE_DEPARTURES(8000);
   
   int value;
   
   Priorities(final int value) { this.value = value; }
   
   public int getValue() { return this.value; }
}
