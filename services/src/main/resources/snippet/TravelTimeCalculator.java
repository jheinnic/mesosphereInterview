package info.jchein.mesosphere.elevator.scheduler.tracking;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.physics.JourneyArc;

public class TravelTimeCalculator implements ITravelTimeCalculator {
	private IElevatorPhysicsService physicsService;
	private BuildingProperties bldgProperties;
	private final double[] travelTimesUp;
	private final double[] travelTimesDown;

	@Autowired
	public TravelTimeCalculator(IElevatorPhysicsService physicsService, BuildingProperties bldgProperties) {
		this.physicsService = physicsService;
		this.bldgProperties = bldgProperties;
		this.travelTimesUp = new double[bldgProperties.getNumFloors() - 1];
		this.travelTimesDown = new double[bldgProperties.getNumFloors() - 1];
	}
	
	@PostConstruct
	public void init() {
		int numFloors = this.bldgProperties.getNumFloors();
		for( int ii=1; ii<numFloors; ii++ ) {
			final JourneyArc arcUp = physicsService.getTraversalPath(0, ii);
			final JourneyArc arcDown = physicsService.getTraversalPath(ii, 0);
			this.travelTimesUp[ii-1] = arcUp.duration();
			this.travelTimesDown[ii-1] = arcDown.duration();
		}
	}
	
	public double getFloorTravelTime(int fromIndex, int toIndex) {
		if (fromIndex > toIndex) { 
			return this.travelTimesDown[fromIndex - toIndex - 1];
		}
		
		if (fromIndex < toIndex) { 
			return this.travelTimesUp[toIndex - fromIndex - 1];
		}
		
		return 0;
	}
}