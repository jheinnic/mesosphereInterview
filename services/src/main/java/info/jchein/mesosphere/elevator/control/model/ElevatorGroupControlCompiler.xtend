package info.jchein.mesosphere.elevator.control.model

import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriverFactory
import org.springframework.beans.factory.annotation.Autowired
import org.statefulj.framework.core.annotations.FSM
import org.statefulj.framework.core.model.StatefulFSM
import org.springframework.stereotype.Component

@Component
class ElevatorGroupControlCompiler implements IElevatorGroupControlCompiler
{
	// FSM reference used exclusively for sending an entity-initiating event, effectively using it as a factory method.
	@FSM(ElevatorCar.BEAN_NAME)
	var StatefulFSM<ElevatorCar> fsm
	
	val ElevatorGroupBootstrap configData
	val IElevatorCarDriverFactoryLocator driverLocator

	@Autowired
	new(ElevatorGroupBootstrap configData, IElevatorCarDriverFactoryLocator driverLocator)
	{
		this.configData = configData
		this.driverLocator = driverLocator
	}

	override IElevatorGroupControl compileBootstrapData(ElevatorGroupBootstrap data) 
	{	
		val int numElevators = data.building.numElevators;
		val String carDriverKey = data.building.carDriverKey;
		val IElevatorCarDriverFactory adapterFactory = 
			this.driverLocator.locateDriverFactory(carDriverKey);
		
		for( var int ii=0; ii<numElevators; ii++ ) {
			this.fsm.onEvent(ElevatorCar.ALLOCATED) as ElevatorCar => [elevatorCarPort |
				val IElevatorCarDriver portDriver = adapterFactory.allocateDriver(elevatorCarPort)
				elevatorCarPort.attachDriver(portDriver)
			]
		}
	
		var IElevatorGroupControl retVal = null
		return retVal
	}
}
