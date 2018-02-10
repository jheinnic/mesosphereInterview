package info.jchein.mesosphere.elevator.simulator.model;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorControl;
import info.jchein.mesosphere.elevator.simulator.passengers.ITravellerQueueService;

@Component("ElevatorSimulation")
public class ElevatorSimulation implements IElevatorSimulation, ITravellerQueueService
{
   private final IEmulatorControl emulatedControl;
//   private final ISimulatedLandingHall[] landingHalls;

   private final ArrayList<SimulatedTraveller> upwardBoundPickups = new ArrayList<SimulatedTraveller>(10);
   private final ArrayList<SimulatedTraveller> downwardBoundPickups = new ArrayList<SimulatedTraveller>(10);


   @Autowired
   public ElevatorSimulation(IEmulatorControl emulatedControl) {
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
         final SimulatedTraveller p =
            new SimulatedTraveller(uuid.toString(), destinationFloorIndex, destinationFloorIndex, timeIndex);
         this.upwardBoundPickups.add(p);
      } else {
         direction = DirectionOfTravel.GOING_DOWN;
         final UUID uuid = UUID.randomUUID();
         final SimulatedTraveller p =
            new SimulatedTraveller(uuid.toString(), destinationFloorIndex, destinationFloorIndex, timeIndex);
         this.downwardBoundPickups.add(p);
      }
   }

	public void start() {
//		this.schedulingWorker.scheduleOnce(this.action, 100, TimeUnit.MILLISECONDS);
	}
}
