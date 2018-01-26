package info.jchein.mesosphere.elevator.simulator.passengers;

import javax.validation.constraints.NotNull;

import org.javasim.streams.ExponentialStream;
import org.javasim.streams.NormalStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.model.StatefulFSM;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.validator.annotation.Positive;
import rx.Subscription;
import rx.functions.Action0;

@StatefulController(value = TravelPathFSM.BEAN_ID, clazz = TravelPathFSM.class, startState = TravelPathFSM.START_STATE, blockingStates = {},
noops = { @Transition(from=TravelPathFSM.START_STATE, event=TravelPathFSM.BEGIN_FROM_LOBBY, to=TravelPathFSM.GO_TO_ACTIVITY)
})
public class SimulatedTravellingPassengerSource {
	private static final Logger LOG = LoggerFactory.getLogger(SimulatedTravellingPassengerSource.class);

	@FSM
	StatefulFSM<TravelPathFSM> fsm;

	private final IClock systemClock;
	private final EventBus eventBus;
	private final IPassengerArrivalStrategy arrivalStrategy;
//	private final IBehaviorStrategy traversalBehavior;
	private final ExponentialStream arrivalStream;
//	private final long fallbackDelay;
	private final Arrive action;

	private Subscription subscription;

	private TravellerContext travellerContext;

	private ExponentialStream stepStream;

	private NormalStream bldgTimeStream;


	private class Arrive implements Action0 {
		@Override
		public void call() {
//			SimulatedTravellingPassengerSource.this.onPassengerArrival();
		}
	}

	@Autowired
	public SimulatedTravellingPassengerSource(
			@NotNull IClock systemClock,
			@NotNull EventBus eventBus,
			@Positive double medianSecondsBetweenArrivals,
			@Positive double medianSecondsBetweenFloorChanges,
			@Positive double medianTimeInBuilding,
			@Positive double stdDevTimeInBuilding,
			@NotNull IPathSelector pathSelector,
			@NotNull IPassengerArrivalStrategy arrivalStrategy) {
//			@NotNull IBehaviorStrategy traversalBehavior) {
		this.systemClock = systemClock;
		this.eventBus = eventBus;
		this.arrivalStrategy = arrivalStrategy;
//		this.traversalBehavior = traversalBehavior;
//		this.fallbackDelay = Math.round(medianSecondsBetweenArrivals * 1000);

		this.arrivalStream = new ExponentialStream(medianSecondsBetweenArrivals * 1000);
		this.stepStream = new ExponentialStream(medianSecondsBetweenFloorChanges * 1000);
		this.bldgTimeStream = new NormalStream(medianTimeInBuilding, stdDevTimeInBuilding);
		this.action = new Arrive();
	}

	/*
	public void init() {
		this.scheduleNextArrival();
	}

	private void scheduleNextArrival() {
		long delay;
		try {
			delay = Math.round(this.stream.getNumber());
		} catch (ArithmeticException | IOException e) {
			LOG.warn("Interrarrival randomization stream threw exception on generate.  Using raw median.", e);
			delay = this.fallbackDelay;
		}

		this.subscription = this.systemClock.scheduleOnce(this.action, delay, TimeUnit.MILLISECONDS);
	}

	public void onPassengerArrival() {
		this.arrivalStrategy.passengerArrival(this.systemClock.now(), this.departFromFloorIndex,
				this.travelToFloorIndex);
		this.subscription.unsubscribe();
		this.scheduleNextArrival();
	}

	@Transition(from = TravelPathFSM.START_STATE, event = TravelPathFSM.ENTER_SIMULATION)
	public String enterSimulation(TravelPathFSM fsm, String event) {
		this.travellerContext = this.traversalBehavior.activate(this.travellerContext);
		switch (this.travellerContext.getCurrentLocation()) {
		case LOBBY: {
			return "event: " + TravelPathFSM.BEGIN_FROM_LOBBY;
		}
		case WORKFLOOR_A: {
			return "event: " + TravelPathFSM.BEGIN_FROM_A;
		}
		case WORKFLOOR_B: {
			return "event: " + TravelPathFSM.BEGIN_FROM_B;
		}
		case WORKFLOOR_C: {
			return "event: " + TravelPathFSM.BEGIN_FROM_C;
		}
		default: {
			throw new IllegalStateException("Unknown location value");
		}
		}
	}
	
//	@Transition(from=TravelPathFSM.START_STATE, event=TravelPathFSM.BEGIN_FROM_LOBBY, to=TravelPathFSM.BEGIN_ACTIVITY)
	*/
}