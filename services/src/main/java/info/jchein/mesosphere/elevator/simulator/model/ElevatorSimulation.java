package info.jchein.mesosphere.elevator.simulator.model;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatedLandingControls;
import info.jchein.mesosphere.elevator.simulator.passengers.IPassengerArrivalStrategy;

@Component("ElevatorSimulation")
public class ElevatorSimulation implements IElevatorSimulation, IPassengerArrivalStrategy
{
   private final IEmulatedLandingControls emulatedControl;
//   private final ISimulatedLandingHall[] landingHalls;

   private final ArrayList<SimulatedPassenger> upwardBoundPickups = new ArrayList<SimulatedPassenger>(10);
   private final ArrayList<SimulatedPassenger> downwardBoundPickups = new ArrayList<SimulatedPassenger>(10);


   @Autowired
   public ElevatorSimulation(IEmulatedLandingControls emulatedControl) {
      this.emulatedControl = emulatedControl;
   }
   
   public void passengerArrival(long timeIndex, int originFloorIndex, int destinationFloorIndex) {
      Preconditions.checkArgument(originFloorIndex != destinationFloorIndex, "Origin and destination must be different");
      Preconditions.checkArgument(originFloorIndex >= 0, "Origin floor must be non-negative and within building");
      Preconditions.checkArgument(destinationFloorIndex >= 0, "Destination floor must be non-negative and within building");

      final DirectionOfTravel direction;
      if (originFloorIndex < destinationFloorIndex) {
         direction = DirectionOfTravel.GOING_UP;
         final UUID uuid = UUID.randomUUID();
         final SimulatedPassenger p =
            new SimulatedPassenger(uuid.toString(), destinationFloorIndex, destinationFloorIndex, timeIndex);
         this.upwardBoundPickups.add(p);
      } else {
         direction = DirectionOfTravel.GOING_DOWN;
         final UUID uuid = UUID.randomUUID();
         final SimulatedPassenger p =
            new SimulatedPassenger(uuid.toString(), destinationFloorIndex, destinationFloorIndex, timeIndex);
         this.downwardBoundPickups.add(p);
      }
   }

	public void start() {
//		this.schedulingWorker.scheduleOnce(this.action, 100, TimeUnit.MILLISECONDS);
	}
}
