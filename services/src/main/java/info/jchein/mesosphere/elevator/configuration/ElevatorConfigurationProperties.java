package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.configuration.properties.PassengerToleranceProperties;

//@Component("ElevatorConfigurationProperties")
@Configuration
@ConfigurationProperties("mesosphere.elevator")
public class ElevatorConfigurationProperties {

	public int foo;

	// @Min(3)
	public int numFloors;

	// @Min(1)
	public int numElevators;

	public double metersPerFloor = 3.5;

	public double brakeSpeed = 0.5;
	
	public double slowSpeed = 1.5;

	public double maxRiseSpeed = 4.0;

	public double maxDescentSpeed = 3.2;

	public double maxAcceleration = 1.5;

	public double maxJerk = 2.0;

	public int maxWeightLoad = 1250;

	public double idealWeightLoad = 600;

	public int passengerWeight = 75;

	public int minDoorHoldTimePerOpen = 3000;

	public int doorHoldTimePerPerson = 250;

	public int doorOpenCloseSlideTime = 2000;

	public int simulationTickTime = 20;

	public double brakingDistance = 300;

	public int passengerStopTolerance = 4;

	public int maxTravelTimeForHallCall = 18500;

	public double passengerPickupTimeTolerance = 30000;

	public double maxSpeedForStopAtFloor = 0.25;

	public double refusePickupAfterWeightPct = 0.85;

	public double passengerTravelTimeTolerance;

	public int getNumFloors() {
		return numFloors;
	}

	public int getNumElevators() {
		return numElevators;
	}

	public double getMetersPerFloor() {
		return metersPerFloor;
	}

	public double getBrakeSpeed() {
		return brakeSpeed;
	}

	public double getSlowSpeed() {
		return slowSpeed;
	}

	public double getMaxRiseSpeed() {
		return maxRiseSpeed;
	}

	public double getMaxDescentSpeed() {
		return maxDescentSpeed;
	}

	public double getMaxAcceleration() {
		return maxAcceleration;
	}

	public double getMaxHallCallWeightPct() {
		return refusePickupAfterWeightPct;
	}

	public int getMaxWeightAllowance() {
		return maxWeightLoad;
	}

	public int getAvgPassengerWeight() {
		return passengerWeight;
	}

	public int getMaxStopsForHallCall() {
		return passengerStopTolerance;
	}

	public double getMaxTravelTimeForHallCall() {
		return maxTravelTimeForHallCall;
	}

	public int getMinDoorHoldTimePerOpen() {
		return minDoorHoldTimePerOpen;
	}

	public int getDoorHoldTimePerPerson() {
		return doorHoldTimePerPerson;
	}

	public int getDoorOpenCloseSlideTime() {
		return doorOpenCloseSlideTime;
	}

	public double getSimulationTickTime() {
		return simulationTickTime;
	}

	public double getBrakingDistance() {
		return brakingDistance;
	}

	public double getPassengerPickupTimeTolerance() {
		return passengerPickupTimeTolerance;
	}

	public double getPassengerTravelTimeTolerance() {
		return passengerTravelTimeTolerance;
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	BuildingProperties getBuildingProperties() {
		return BuildingProperties.build(builder -> {
			builder.numFloors(this.numFloors)
				.numElevators(this.numElevators)
				.metersPerFloor(this.metersPerFloor);
		});
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	ElevatorDoorProperties getDoorProperties() {
		return ElevatorDoorProperties.build(builder -> {
			builder.doorHoldTimePerPerson(this.doorHoldTimePerPerson)
				.doorOpenCloseSlideTime(this.doorOpenCloseSlideTime)
				.minDoorHoldTimePerOpen(this.minDoorHoldTimePerOpen);
		});
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	ElevatorMotorProperties getMotorProperties() {
		return ElevatorMotorProperties.build(builder -> {
			builder.brakingDistance(this.brakingDistance)
				.brakingSpeed(this.brakeSpeed)
				.maxJerk(this.maxJerk)
				.maxAcceleration(this.maxAcceleration)
				.maxSlowSpeed(this.maxSlowSpeed)
				.maxDescentSpeed(this.maxDescentSpeed)
				.maxRiseSpeed(this.maxRiseSpeed);
		});
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	ElevatorWeightProperties getWeightProperties() {
		return ElevatorWeightProperties.build(builder -> {
			builder.maxWeightLoad(this.maxWeightLoad)
				.idealWeightLoad(this.idealWeightLoad)
				.passengerWeight(this.passengerWeight);
		});
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	PassengerToleranceProperties getPassengerTolerance() {
		return PassengerToleranceProperties.build(builder -> {
			builder.passengerPickupTimeTolerance(this.passengerPickupTimeTolerance)
				.passengerTravelTimeTolerance(this.passengerTravelTimeTolerance)
				.passengerStopTolerance(this.passengerStopTolerance)
				.refusePickupAfterWeightPct(this.refusePickupAfterWeightPct);
		});
	}
}
