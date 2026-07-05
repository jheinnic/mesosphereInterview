package test;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import info.jchein.mesosphere.elevator.common.bootstrap.BuildingDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeConfiguration;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public class DecisionExplorer {
	private static ThreadFactory threadFactory(String pattern) {
		return new ThreadFactoryBuilder().setNameFormat(pattern).build();
	}

	public class ClockPulse {
		public final long now;

		public ClockPulse(Long now) {
			this.now = now;
		}
	}

	public static class SimulationBegin {
		private SimulationBegin() {
		}

		public static final SimulationBegin INSTANCE = new SimulationBegin();
	}

	private Scheduler scheduler;
	private VirtualRuntimeConfiguration runtimeConfig;
	private BuildingDescription bldgProps;
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

		public Elevator(ElevatorPort port) {
			this.elevatorId = ID_GENERATOR.getAndIncrement();
			this.port = port;
		}

		public void init() {
			this.port.emitReady(this);
		}

		public int getElevatorId() {
			return this.elevatorId;
		}

		public void arrive() {
			System.out.println(String.format("Arrived at %d on %d", this.port.now(), Thread.currentThread().getId()));
		}

		@Subscribe
		public void onBegin(SimulationBegin event) {
			this.port.scheduleArrival(this, this.elevatorId * 1000);
		}

	}

	public static class ElevatorPort {
		private EventBus eventBus;
		private Scheduler scheduler;
		private Worker worker;

		ElevatorPort(EventBus eventBus, Scheduler scheduler) {
			this.eventBus = eventBus;
			this.scheduler = scheduler;
			this.worker = scheduler.createWorker();
		}

		public long now() {
			return this.scheduler.now();
		}

		public void emitReady(Elevator elevator) {
			System.out.println("Emitting readiness");
			this.eventBus.post(new ElevatorReady(elevator));
		}

		public void scheduleArrival(Elevator elevator, long delay) {
//			Observable.timer(delay, TimeUnit.MILLISECONDS, this.scheduler).doOnCompleted(elevator::arrive).subscribe();
			worker.schedule(elevator::arrive, delay, TimeUnit.MILLISECONDS);
		}
	}

	public static class ElevatorControl {
		private EventBus eventBus;
		private final int numElevators;
		private final HashSet<Integer> elevatorsSeen = new HashSet<Integer>();

		public ElevatorControl(EventBus eventBus, BuildingDescription bldgProps) {
			this.eventBus = eventBus;
			this.numElevators = bldgProps.getNumElevators();
		}

		@Subscribe
		public void onElevatorReady(ElevatorReady event) {
			System.out.println("Seen ready");
			this.elevatorsSeen.add(event.elevatorId);
			if (this.elevatorsSeen.size() == this.numElevators) {
				this.eventBus.post(SimulationBegin.INSTANCE);
			}
		}

		@Subscribe
		public void onClockPulse(ClockPulse pulse) {
			// System.out.println(String.format("%d", pulse.now));
		}
	}

	public DecisionExplorer(Scheduler scheduler, EventBus eventBus, VirtualRuntimeConfiguration runtimeConfig,
			BuildingDescription bldgProps) {
		this.scheduler = scheduler;
		this.eventBus = eventBus;
		this.runtimeConfig = runtimeConfig;
		this.bldgProps = bldgProps;
		this.clockTickDuration = this.runtimeConfig.getTickDurationMillis();
	}

	public void doIt() {
		ElevatorPort ep = new ElevatorPort(this.eventBus, this.scheduler);
		Elevator e1 = new Elevator(ep);
		Elevator e2 = new Elevator(ep);
		Elevator e3 = new Elevator(ep);
		Elevator e4 = new Elevator(ep);
		ElevatorControl ec = new ElevatorControl(this.eventBus, this.bldgProps);

		this.eventBus.register(e1);
		this.eventBus.register(e2);
		this.eventBus.register(e3);
		this.eventBus.register(e4);
		this.eventBus.register(ec);

		Observable.interval(0, this.clockTickDuration, TimeUnit.MILLISECONDS, this.scheduler)
				.map(next -> new ClockPulse(next)).subscribe(pulse -> {
					this.eventBus.post(pulse);
				});

		e1.init();
		e2.init();
		e3.init();
		e4.init();
	}

	public static class LongEvent {
		private long data;

		public long getData() {
			return this.data;
		}

		public void setData(long data) {
			this.data = data;
		}
	}

	public static void main(String[] args) {
		final ThreadFactory foo = threadFactory("Worker %d");
		final ExecutorService rxPool = Executors.newSingleThreadExecutor(foo);
		final ExecutorService busPool = Executors.newSingleThreadExecutor(foo);
		final Scheduler scheduler = Schedulers.from(rxPool);
		final EventBus eventBus = new AsyncEventBus(busPool);

		final VirtualRuntimeConfiguration runtimeProps = VirtualRuntimeConfiguration.build( bldr -> {
		   bldr.tickDurationMillis(100);
		});

		final BuildingDescription bldgProps = BuildingDescription.build(bldr -> {
			bldr.metersPerFloor(3.5).numElevators(4).numFloors(10);
		});

		final DecisionExplorer explorer = new DecisionExplorer(scheduler, eventBus, runtimeProps, bldgProps);
		explorer.doIt();

		// Construct the Disruptor
		// Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, 512,
		// rxPool);
		//
		// Connect the handler
		// disruptor.handleEventsWith(LongEventMain::handleEvent);
		//
		// Start the Disruptor, starts all threads running
		// disruptor.start();
		//
		// Get the ring buffer from the Disruptor to be used for publishing.
		// RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
		//
		// ByteBuffer bb = ByteBuffer.allocate(8);
		// for (long l = 0; true; l++)
		// {
		// bb.putLong(0, l);
		// ringBuffer.publishEvent(LongEventMain::translate, bb);
		// Thread.sleep(1000);
		// }

		final TestScheduler schedulerTwo = Schedulers.test();
		final EventBus eventBusTwo = new EventBus();
		final DecisionExplorer explorerTwo = new DecisionExplorer(schedulerTwo, eventBusTwo, runtimeProps, bldgProps);

		explorerTwo.doIt();
		for (int ii = 0; ii < 80; ii++) {
			System.out.println(String.format("Test iteration #%d", ii + 1));
			schedulerTwo.advanceTimeBy(100, TimeUnit.MILLISECONDS);
		}
		System.out.println("Done");
	}
}
