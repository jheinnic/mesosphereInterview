package info.jchein.mesosphere.elevator.simulator.passengers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.distribution.ExponentialDistribution;
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
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.runtime.IRuntimeService;
import info.jchein.mesosphere.elevator.simulator.FloorHallSimulation;
import info.jchein.mesosphere.elevator.simulator.SimulatedPassenger;
import info.jchein.mesosphere.validator.annotation.Positive;

@Component
@ScriptAssert(lang="javascript", script="_this.departFromFloorIndex != _this.travelToFloorIndex")
public class SimulatedSimplePassengerSource {
	private static final Logger LOG = LoggerFactory.getLogger(SimulatedSimplePassengerSource.class);

	private final IRuntimeService systemClock;
	private final IPassengerArrivalStrategy arrivalStrategy;

	private final ExponentialDistribution distribution;
	private final DirectionOfTravel travelDirection;
	private final int travelToFloorIndex;
	private final int departFromFloorIndex;

	@Autowired
	public SimulatedSimplePassengerSource(@NotNull IRuntimeService systemClock, @Positive double medianSecondsBetweenArrivals,
			@Min(0) int departFromFloorIndex, @Min(0) int travelToFloorIndex, @NotNull IPassengerArrivalStrategy arrivalStrategy) {
		this.systemClock = systemClock;
		this.arrivalStrategy = arrivalStrategy;
		this.departFromFloorIndex = departFromFloorIndex;
		this.travelToFloorIndex = travelToFloorIndex;

		if (departFromFloorIndex < travelToFloorIndex) {
			this.travelDirection = DirectionOfTravel.GOING_UP;
		} else if (departFromFloorIndex > travelToFloorIndex) {
			this.travelDirection = DirectionOfTravel.GOING_DOWN;
		} else {
			throw new IllegalArgumentException("Source and destination floors must be different");
		}

		this.distribution = new ExponentialDistribution(medianSecondsBetweenArrivals * 1000, 1e-6);
	}

	public void init() {
		this.systemClock.scheduleVariable(this.drawNextArrival(), TimeUnit.MILLISECONDS, 0, this::onPassengerArrival);
	}

	private long drawNextArrival() {
		return Math.round(
		   this.distribution.sample());
	}

	public long onPassengerArrival(long delta) {
//		SimulatedPassenger nextPassenger =
//			new SimulatedPassenger("temp", 1, this.travelToFloorIndex, this.systemClock.now());
		this.arrivalStrategy.passengerArrival(
		   this.systemClock.now(), this.departFromFloorIndex, this.travelToFloorIndex);

		return this.drawNextArrival();
	}
}