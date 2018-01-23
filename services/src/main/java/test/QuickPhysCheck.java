package test;

import info.jchein.mesosphere.elevator.physics.BuildingProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorPhysicsService;
import info.jchein.mesosphere.elevator.physics.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.physics.PassengerToleranceProperties;

public class QuickPhysCheck {
	public static void main(String[] args) {
		PassengerToleranceProperties toleranceProps = PassengerToleranceProperties.build(bldr -> {
			// bldr.
		});
		ElevatorWeightProperties weightProps = ElevatorWeightProperties.build(bldr -> {
		});
		ElevatorMotorProperties motorProps = ElevatorMotorProperties.build(bldr -> {
			bldr.brakingDistance(0.3).brakingSpeed(0.5).maxAcceleration(1.5).maxJerk(2.0).maxDescentSpeed(3.2).maxRiseSpeed(4.0);
		});
		ElevatorDoorProperties doorProps = ElevatorDoorProperties.build(bldr -> { });
		BuildingProperties bldgProps = BuildingProperties.build(bldr -> {
			bldr.numElevators(6).numFloors(12).metersPerFloor(1.5);
		});
		
		ElevatorPhysicsService physicsService = new ElevatorPhysicsService(
			bldgProps, doorProps, motorProps, weightProps, toleranceProps);
		// physicsService.init();
	}
}
