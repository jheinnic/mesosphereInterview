package info.jchein.mesosphere.elevator.domain.model

import com.google.common.eventbus.EventBus
import info.jchein.mesosphere.domain.clock.IClock
import info.jchein.mesosphere.elevator.domain.^dispatch.event.StopItineraryUpdated
import java.util.BitSet
import org.springframework.beans.factory.annotation.Autowired
import org.statefulj.framework.core.annotations.FSM
import org.statefulj.framework.core.annotations.StatefulController
import org.statefulj.framework.core.model.StatefulFSM
import rx.Observer
import rx.Scheduler

@StatefulController(value=ElevatorCarService.BEAN_NAME, clazz=typeof(ElevatorCar), startState=ElevatorCar.BOOTSTRAPPING)
class ElevatorCarService implements IElevatorCarService {
	public static final String BEAN_NAME = "ElevatorCarFactory"

	@FSM(ElevatorCar.BEAN_NAME)
	StatefulFSM<ElevatorCar> fsm

	IClock systemClock

	Scheduler systemScheduler

	@Autowired
	new(EventBus eventBus, IClock systemClock, Scheduler systemScheduler) {
		this.systemClock = systemClock
		this.systemScheduler = systemScheduler
	}

	override bootstrapElevatorCar(double floorHeight, double weightLoad, BitSet floorStops,
		Observer<StopItineraryUpdated> portAdapter) {
		val elevatorCarPort = this.fsm.onEvent(
			ElevatorCar.DRIVER_ATTACHED, floorHeight, weightLoad, floorStops) as ElevatorCar

		return elevatorCarPort.attachDriver(portAdapter)
	}
}
