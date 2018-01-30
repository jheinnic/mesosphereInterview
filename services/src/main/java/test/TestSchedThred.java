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

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.SystemRuntimeProperties;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public class TestSchedThred {
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

		public void arriveOne() {
			System.out.println(String.format("Arrived at %d on thread %d", this.port.now(), Thread.currentThread().getId()));
		}

		public void arriveTwo() {
			System.out.println(String.format("Arrived again at %d on thread %d", this.port.now(), Thread.currentThread().getId()));
		}

		public void arriveThree() {
			System.out.println(String.format("Arrived yet again at %d on thread %d", this.port.now(), Thread.currentThread().getId()));
		}

		public void arriveFour() {
			System.out.println(String.format("Arrived for the last time at %d on thread %d", this.port.now(), Thread.currentThread().getId()));
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
		   System.out.println(String.format("Scheduling from thread %d", Thread.currentThread().getId()));
		   Worker worker = this.scheduler.createWorker();
			worker.schedule(elevator::arriveOne, delay, TimeUnit.MILLISECONDS);
		   worker = this.scheduler.createWorker();
			worker.schedule(elevator::arriveTwo, delay, TimeUnit.MILLISECONDS);
		   worker = this.scheduler.createWorker();
			worker.schedule(elevator::arriveThree, delay, TimeUnit.MILLISECONDS);
		   worker = this.scheduler.createWorker();
			worker.schedule(elevator::arriveFour, delay, TimeUnit.MILLISECONDS);
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

	public TestSchedThred(Scheduler scheduler, EventBus eventBus, SystemRuntimeProperties runtimeProps,
			BuildingProperties bldgProps) {
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
		ThreadFactory foo = threadFactory("Worker %d");
		ExecutorService rxPool = Executors.newSingleThreadExecutor(foo);
		ExecutorService rxPool2 = Executors.newFixedThreadPool(4, foo);
		ExecutorService busPool = Executors.newSingleThreadExecutor(foo);
		ExecutorService busPool2 = Executors.newFixedThreadPool(1, foo);
		Scheduler scheduler = Schedulers.from(rxPool2);
		EventBus eventBus = new AsyncEventBus(busPool);
		SystemRuntimeProperties runtimeProps = SystemRuntimeProperties.build(bldr -> {
			bldr.clockTickDuration(0.01);
		});
		BuildingProperties bldgProps = BuildingProperties.build(bldr -> {
			bldr.metersPerFloor(3.5).numElevators(4).numFloors(10);
		});

//		TestSchedThred explorer = new TestSchedThred(scheduler, eventBus, runtimeProps, bldgProps);
//		explorer.doIt();

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

		TestScheduler schedulerTwo = Schedulers.test();
		EventBus eventBusTwo = new AsyncEventBus(busPool2);
		TestSchedThred explorerTwo = new TestSchedThred(schedulerTwo, eventBusTwo, runtimeProps, bldgProps);
		explorerTwo.doIt();
		for (int ii = 0; ii < 80; ii++) {
			System.out.println(String.format("Test iteration #%d", ii + 1));
			schedulerTwo.advanceTimeBy(100, TimeUnit.MILLISECONDS);
		}
		System.out.println("Done");
	}
}
