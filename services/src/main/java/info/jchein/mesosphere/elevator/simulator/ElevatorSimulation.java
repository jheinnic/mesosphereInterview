package info.jchein.mesosphere.elevator.simulator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import rx.Scheduler.Worker;

@Component("ElevatorSimulation")
public class ElevatorSimulation implements IElevatorSimulation {
	private final Worker schedulingWorker;
	private final List<FloorHallSimulation> hallList;
	private final List<ElevatorCarEmulator> carList;

	public ElevatorSimulation(
		@NotNull Worker schedulingWorker,
		@NotNull List<FloorHallSimulation> hallList,
		@NotNull List<ElevatorCarEmulator> carList)
	{
		this.schedulingWorker = schedulingWorker;
		this.hallList = hallList;
		this.carList = carList;
	}
	
	public void start() {
		this.schedulingWorker.schedulePeriodically(() -> {
			System.out.println(
				String.format("Pulse at %d", this.schedulingWorker.now()));
		}, 0, 100, TimeUnit.MILLISECONDS);
	}
}
