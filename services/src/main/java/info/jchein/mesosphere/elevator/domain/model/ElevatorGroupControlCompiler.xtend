package info.jchein.mesosphere.elevator.domain.model

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap
import info.jchein.mesosphere.elevator.domain.model.ElevatorCar
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriverFactory
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler
import org.springframework.beans.factory.annotation.Autowired
import org.statefulj.framework.core.annotations.FSM
import org.statefulj.framework.core.model.StatefulFSM
import rx.Scheduler
import info.jchein.mesosphere.elevator.domain.common.InitialElevatorCarState

class ElevatorGroupControlCompiler implements IElevatorGroupControlCompiler
{
	// FSM reference used exclusively for sending an entity-initiating event, effectively using
	// it as a factory method.
	@FSM(ElevatorCar.BEAN_NAME)
	StatefulFSM<ElevatorCar> fsm

	private val IRuntimeEventBus eventBus
	private val IRuntimeClock clock
	private val IRuntimeScheduler scheduler
	
	IElevatorCarDriverFactory<?> adapterFactory

	@Autowired
	new(IRuntimeEventBus eventBus, IRuntimeClock clock, IRuntimeScheduler scheduler, IElevatorCarDriverFactory<?> adapterFactory ) {
		this.adapterFactory = adapterFactory
		this.scheduler = scheduler
		this.eventBus = eventBus
		this.clock = clock
	}

	override <T extends Scheduler> compileBootstrapData(ElevatorGroupBootstrap data, T scheduler) 
	{	
		data.cars.map[InitialElevatorCarState carData |
			return this.fsm.onEvent(ElevatorCar.ALLOCATED) as ElevatorCar => [elevatorCarPort |
				val portDriver = this.adapterFactory.initialize(elevatorCarPort, carData)
				elevatorCarPort.attachDriver(portDriver, carData)
			]
		]
		
		return null
	}
}
