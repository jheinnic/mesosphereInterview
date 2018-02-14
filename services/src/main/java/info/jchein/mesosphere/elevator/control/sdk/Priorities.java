package info.jchein.mesosphere.elevator.control.sdk;

public enum Priorities
{
   STEP_DRIVERS(-5000),
   SIMULATE_ARRIVALS(-3000),
   TRANSFER_PASSENGERS(-1000),
   SCHEDULE_DISPATCH(1000),
   SIMULATE_DEPARTURES(3000);
   
   int value;
   
   Priorities(final int value) { this.value = value; }
   
   public int getValue() { return this.value; }
}
