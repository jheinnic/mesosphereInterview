package info.jchein.mesosphere.elevator.simulator.passengers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;
import org.javasim.streams.ExponentialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rx.Scheduler.Worker;
import rx.Subscription;
import rx.functions.Action0;
import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.simulator.FloorHallSimulation;
import info.jchein.mesosphere.elevator.simulator.SimulatedPassenger;
import info.jchein.mesosphere.validator.annotation.Positive;

@Component
@ScriptAssert(lang="javascript", script="_this.departFromFloorIndex != _this.travelToFloorIndex")
public class SimulatedSimplePassengerSource {
	private static final Logger LOG = LoggerFactory.getLogger(SimulatedSimplePassengerSource.class);

	private final IClock systemClock;
	private final IPassengerArrivalStrategy arrivalStrategy;

	private final ExponentialStream stream;
	private final long fallbackDelay;
	private final DirectionOfTravel travelDirection;
	private final int travelToFloorIndex;
	private final int departFromFloorIndex;
	private final Arrive action;

	private Subscription subscription;


	private class Arrive implements Action0 {
		@Override
		public void call() {
			SimulatedSimplePassengerSource.this.onPassengerArrival();
		}
	}

	@Autowired
	public SimulatedSimplePassengerSource(@NotNull IClock systemClock, @Positive double medianSecondsBetweenArrivals,
			@Min(0) int departFromFloorIndex, @Min(0) int travelToFloorIndex, @NotNull IPassengerArrivalStrategy arrivalStrategy) {
		this.systemClock = systemClock;
		this.arrivalStrategy = arrivalStrategy;
		this.fallbackDelay = Math.round(medianSecondsBetweenArrivals * 1000);
		this.departFromFloorIndex = departFromFloorIndex;
		this.travelToFloorIndex = travelToFloorIndex;

		if (departFromFloorIndex < travelToFloorIndex) {
			this.travelDirection = DirectionOfTravel.GOING_UP;
		} else if (departFromFloorIndex > travelToFloorIndex) {
			this.travelDirection = DirectionOfTravel.GOING_DOWN;
		} else {
			throw new IllegalArgumentException("Source and destination floors must be different");
		}

		this.stream = new ExponentialStream(medianSecondsBetweenArrivals * 1000);
		this.action = new Arrive();
	}

	public void init() {
		this.scheduleNextArrival();
	}

	private void scheduleNextArrival() {
		long delay;
		try {
			delay = Math.round(
				this.stream.getNumber());
		} catch (ArithmeticException | IOException e) {
			LOG.warn("Interrarrival randomization stream threw exception on generate.  Using raw median.", e);
			delay = this.fallbackDelay;
		}

		this.subscription = this.systemClock.scheduleOnce(this.action, delay, TimeUnit.MILLISECONDS);
	}

	public void onPassengerArrival() {
//		SimulatedPassenger nextPassenger =
//			new SimulatedPassenger("temp", 1, this.travelToFloorIndex, this.systemClock.now());
		this.arrivalStrategy.passengerArrival(this.systemClock.now(), this.departFromFloorIndex, this.travelToFloorIndex);
		this.subscription.unsubscribe();
		this.scheduleNextArrival();
	}
}