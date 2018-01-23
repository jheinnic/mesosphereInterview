package info.jchein.mesosphere.elevator.simulator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.emulator.ElevatorCarEmulator;
import info.jchein.mesosphere.elevator.emulator.LandingControlEmulator;
import rx.Scheduler.Worker;
import rx.functions.Action0;

@Component("ElevatorSimulation")
public class ElevatorSimulation implements IElevatorSimulation {
	private final IClock schedulingWorker;
	private final List<LandingControlEmulator> hallList;
	private final List<ElevatorCarEmulator> carList;
	private final Action0 action;
	private final EventBus eventBus;

	public ElevatorSimulation(
		@NotNull IClock schedulingWorker,
		@NotNull EventBus eventBus,
		@NotNull List<LandingControlEmulator> hallList,
		@NotNull List<ElevatorCarEmulator> carList)
	{
		this.schedulingWorker = schedulingWorker;
		this.eventBus = eventBus;
		this.hallList = hallList;
		this.carList = carList;
		this.action = () -> {
			System.out.println(
				String.format("Pulse at %d", this.schedulingWorker.now()));
			this.schedulingWorker.scheduleOnce(this.getAction(), 100, TimeUnit.MILLISECONDS);
		};
	}
	
	private Action0 getAction() {
		return this.action;
	}

	public void start() {
		this.schedulingWorker.scheduleOnce(this.action, 100, TimeUnit.MILLISECONDS);
	}
}
