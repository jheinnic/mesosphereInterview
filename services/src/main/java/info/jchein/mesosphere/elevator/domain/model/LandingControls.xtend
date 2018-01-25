package info.jchein.mesosphere.elevator.domain.model

import com.google.common.eventbus.EventBus
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallRemoved
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelDriver
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelPort
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties

@Component
@Scope(scopeName="prototype")
class LandingControls implements ILandingControls, IHallPanelPort {
	private static val AtomicInteger ID_SEQUENCE = new AtomicInteger();
	
	@NotNull private val EventBus eventBus
	@NotNull private val IClock systemClock
	@Min(0) private val int floorIndex

	private IHallPanelDriver driver;
	private val ArrayList<IElevatorCarParkRelease> parkedKeys;

	new(@NotNull IClock systemClock, @NotNull EventBus eventBus, @NotNull BuildingProperties bldgProperties
	) {
		super()
		this.eventBus = eventBus
		this.systemClock = systemClock;
		this.parkedKeys = new ArrayList<IElevatorCarParkRelease>(bldgProperties.numElevators);
		this.floorIndex = ID_SEQUENCE.incrementAndGet();
	}

	def void attachDriver(@NotNull IHallPanelDriver driver) {
		if (this.driver !== null) {
			throw new RuntimeException("Driver has already been attached")
		}
		this.driver = driver
	}

	override void parkIdleCar(@NotNull IElevatorCarParkRelease parkRelease, @NotNull DirectionOfTravel direction, @Min(0) int carIndex) {
		this.driver.invitePassengersToBoard(direction, carIndex)
		this.parkedKeys.set(carIndex, parkRelease);
	}

	override void releaseParkedCar(@NotNull DirectionOfTravel direction, @Min(0) int carIndex)
	{
		this.driver.withdrawInvitationToBoard(direction, carIndex)
		var IElevatorCarParkRelease release = this.parkedKeys.get(carIndex);
		if (release !== null) {
			release.releaseParkLock();
			this.parkedKeys.set(carIndex, null);
		}
	}

	override int getFloorIndex()
	{
		return this.floorIndex;
	}

	override void requestPickup(@NotNull DirectionOfTravel direction)
	{
		this.eventBus.post(
			PickupCallAdded.build[b|
				b.timeIndex(this.systemClock.now())
				.floorIndex(this.floorIndex)
				.direction(direction)
			]);
	}

	override void floorSensorTriggered(int carIndex, double floorHeight, DirectionOfTravel direction)
	{
		this.eventBus.post(
			FloorSensorTriggered.build [ it |
				it.floorIndex(this.floorIndex)
				.timeIndex(this.systemClock.now())
				.carIndex(carIndex)
				.direction(direction);
			]
		);
	}
	
	
	override cancelPickupRequest(DirectionOfTravel direction) {
		this.eventBus.post(
			PickupCallRemoved.build[ it |
				it.floorIndex(this.floorIndex)
				.direction(direction)
				.timeIndex(this.systemClock.now());
			]
		)
	}
	
	override pollForBootstrap() {
		this.driver.pollForBootstrap();
	}
	
	override pollForClock() {
		this.driver.pollForClock();
	}
	
}
