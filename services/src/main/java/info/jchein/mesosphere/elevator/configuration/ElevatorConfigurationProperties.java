package info.jchein.mesosphere.elevator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//@Component("ElevatorConfigurationProperties")
@Configuration
@ConfigurationProperties("mesosphere.elevator")
public class ElevatorConfigurationProperties {

	public int foo;
	
//	@Min(3)
	public int numFloors;

//	@Min(1)
	public int numElevators;

	public double metersPerFloor = 3.5;

	public double brakeSpeed = 0.5;

	public double maxRiseSpeed = 4.0;

	public double maxDescentSpeed = 3.2;

	public double maxAcceleration = 1.5;
	
	public double maxJerk = 2.0;

	public double maxHallCallWeightPct = 0.85;
	
	public int maxWeightAllowance = 1250;
	
	public int avgPassengerWeight = 75;

	public int maxStopsForHallCall = 4;

	public int maxTravelTimeForHallCall = 18500;

	public int minDoorHoldTimeOnOpen = 3000;

	public int perAccessDoorHoldTime = 250;

	public int doorSlideTime = 2000;

//	public double maxSpeedForStopAtFloor = 0.25;

	public int terminalFloorTime = 5040;

	public int middleFloorTime = 4667;

	public int singleFloorTime = 5420;
	
	public int simulationTickTime = 20;

	public int slowBarrierForStop = 300;

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
		return maxHallCallWeightPct;
	}

	public int getMaxWeightAllowance() {
		return maxWeightAllowance;
	}

	public int getAvgPassengerWeight() {
		return avgPassengerWeight;
	}

	public int getMaxStopsForHallCall() {
		return maxStopsForHallCall;
	}

	public double getMaxTravelTimeForHallCall() {
		return maxTravelTimeForHallCall;
	}

	public int getMinDoorHoldTimeOnOpen() {
		return minDoorHoldTimeOnOpen;
	}

	public int getPerAccessDoorHoldTime() {
		return perAccessDoorHoldTime;
	}

	public int getDoorSlideTime() {
		return doorSlideTime;
	}

//	public double getMaxSpeedForStopAtFloor() {
//		return maxSpeedForStopAtFloor;
//	}

	public double getTerminalFloorTime() {
		return terminalFloorTime;
	}

	public double getMiddleFloorTime() {
		return middleFloorTime;
	}

	public double getSingleFloorTime() {
		return singleFloorTime;
	}
	
	public double getSimulationTickTime() {
		return simulationTickTime;
	}

	public int getSlowBarrierForStop() {
		return slowBarrierForStop;
	}
}
