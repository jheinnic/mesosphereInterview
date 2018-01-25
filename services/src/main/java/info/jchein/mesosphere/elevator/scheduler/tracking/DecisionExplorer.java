package info.jchein.mesosphere.elevator.scheduler.tracking;

import java.util.HashSet;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import info.jchein.mesosphere.domain.factory.IBuilder;
import info.jchein.mesosphere.domain.factory.IDirector;
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.SystemRuntimeProperties;
import info.jchein.mesosphere.elevator.scheduler.tracking.DecisionExplorer.ClockPulse;
import info.jchein.mesosphere.elevator.scheduler.tracking.DecisionExplorer.SimulationBegin;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;

public class DecisionExplorer {
	private ThreadFactory threadFactory(String pattern) {
	    return new ThreadFactoryBuilder()
	      .setNameFormat(pattern)
	      .build();
	}

	public class ClockPulse {
		public final long now;

		public ClockPulse(Long now) {
			this.now = now;
		}
	}

	public static class SimulationBegin {
		private SimulationBegin() { }
		
		public static final SimulationBegin INSTANCE = new SimulationBegin();
	}

	private Scheduler scheduler;
	private SystemRuntimeProperties runtimeProps;
	private BuildingProperties bldgProps;
	private long clockTickDuration;
	private EventBus eventBus;
	
	public static class ElevatorReady {
		public final int elevatorId;
		
		public ElevatorReady(Elevator elevator) {
			this.elevatorId = elevator.getElevatorId();
		}
	}

	public static class Elevator {
		private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
		private final int elevatorId;
		private final ElevatorPort port;

		public Elevator( ElevatorPort port ) {
			this.elevatorId = ID_GENERATOR.getAndIncrement();
			this.port = port;
		}
		
		public void init() {
			this.port.emitReady(this);
		}
		
		public int getElevatorId() {
			return this.elevatorId;
		}
	}

	public static class ElevatorPort {
		private EventBus eventBus;
		private Scheduler scheduler;

		ElevatorPort(EventBus eventBus, Scheduler scheduler) {
			this.eventBus = eventBus;
			this.scheduler = scheduler;
		}
		
		public void emitReady(Elevator elevator) {
			this.eventBus.post(
				new ElevatorReady(elevator)
			);
		}
		
		public void scheduleArrival(Elevator elevator, long delay) {
			this.scheduler.when( src -> {
				return src.<Completable>flatMap( inner -> inner).buffer(2).map(list -> {
					Completable one = list.get(0);
					Completable two = list.get(1);
					
				});
			});
		}
	}
	
	public static class ElevatorControl {
		private EventBus eventBus;
		private final int numElevators;
		private final HashSet<Integer> elevatorsSeen = new HashSet<Integer>();
		
		public ElevatorControl(EventBus eventBus, BuildingProperties bldgProps) {
			this.eventBus = eventBus;
			this.numElevators = bldgProps.getNumElevators();
		}
		
		public void onElevatorReady(ElevatorReady event) {
			this.elevatorsSeen.add(event.elevatorId);
			if (this.elevatorsSeen.size() == this.numElevators) {
				this.eventBus.post(SimulationBegin.INSTANCE);
			}
		}
	}

	
	public DecisionExplorer(Scheduler scheduler, EventBus eventBus, SystemRuntimeProperties runtimeProps, BuildingProperties bldgProps) {
		this.scheduler = scheduler;
		this.eventBus = eventBus;
		this.runtimeProps = runtimeProps;
		this.bldgProps = bldgProps;
		this.clockTickDuration = Math.round(this.runtimeProps.getClockTickDuration() * 1000);
	}
	
	public void doIt() {
		ElevatorPort ep = new ElevatorPort(this.eventBus, this.scheduler);
		Elevator e1 = new Elevator(ep);
		Elevator e2 = new Elevator(ep);
		ElevatorControl ec = new ElevatorControl(this.eventBus, this.bldgProps);

		this.eventBus.register(e1);
		this.eventBus.register(e2);
		this.eventBus.register(ec);
		
		Observable.interval(0, this.clockTickDuration, TimeUnit.MILLISECONDS, this.scheduler)
		.map( next -> new ClockPulse(next) )
		.subscribe( pulse -> { this.eventBus.post(pulse); } );
	}
	
	/*
	public ScenarioPool createScenarioPool(IDirector<IScenarioBuilder> director) {
		ScenarioBuilder builder = new ScenarioBuilder();
		director.accept(builder);
		builder.build();
	}
	
	public static class ScenarioBuilder implements IScenarioBuilder, IBuilder<ScenarioPool>

		@Override
		public ScenarioPool build() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IScenarioBuilder addElevatorCar(int carIndex, int floorIndex, IDirector<ICarDetailsBuilder> carDetails) {
			CarDetailsBuilder carDetails = new CarDetailsBuilder(carIndex, floorIndex)
			return null;
		}

		@Override
		public IScenarioBuilder schedulePickupCall(long timestamp, int floorIndex) {
			// TODO Auto-generated method stub
			return null;
		}
		*/
}
