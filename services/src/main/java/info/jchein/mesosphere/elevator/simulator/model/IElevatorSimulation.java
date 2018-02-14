package info.jchein.mesosphere.elevator.simulator.model;


public interface IElevatorSimulation
{
   public static final String TRAVELLER_SOURCE_QUALIFIER = "mesosphere.elevator.simulator.travellers.source";

   public static final String ELEVATOR_SCHEDULER_QUALIFIER = "mesosphere.elevator.simulator.scheduler";

   public void start();
}
