package test;

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.configuration.properties.PassengerToleranceProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorPhysicsService;
import info.jchein.mesosphere.elevator.physics.JourneyArc;
import info.jchein.mesosphere.elevator.physics.PathLeg;
import info.jchein.mesosphere.elevator.physics.PathMoment;

public class QuickPhysCheck {
	public static void main(String[] args) {
		PassengerToleranceProperties toleranceProps = PassengerToleranceProperties.build(bldr -> {
			// bldr.
		});
		ElevatorWeightProperties weightProps = ElevatorWeightProperties.build(bldr -> {
		});
		ElevatorMotorProperties motorProps = ElevatorMotorProperties.build(bldr -> {
			bldr.brakingDistance(0.3).brakingSpeed(0.5).maxAcceleration(1.5).maxJerk(2.0).slowSpeed(1.5).maxDescentSpeed(3.2).maxRiseSpeed(4.0);
		});
		ElevatorDoorProperties doorProps = ElevatorDoorProperties.build(bldr -> { });
		BuildingProperties bldgProps = BuildingProperties.build(bldr -> {
			bldr.numElevators(6).numFloors(12).metersPerFloor(3.5);
		});
		
		ElevatorPhysicsService physicsService = new ElevatorPhysicsService(
			bldgProps, doorProps, motorProps, weightProps, toleranceProps);
		
		for( int ii=0; ii<12; ii++ ) {
			for (int jj=ii+1; jj<12; jj++) {
				JourneyArc arc = physicsService.getTraversalPath(ii, jj);
				System.out.println(
					String.format("From %d to %d: %f meters in %f seconds", ii, jj,
						arc.distance(), arc.duration()));
			}
		}

		JourneyArc anArc = physicsService.getTraversalPath(6, 1);
		double lastDuration = anArc.duration();
		for (int jj=2; jj<12; jj++) {
			JourneyArc arc = physicsService.getTraversalPath(0, jj);
			double delta = arc.duration() - lastDuration;
			lastDuration = arc.duration();
			System.out.println(
				String.format("From %d to %d, time delta is %f", (jj-1), jj, delta));
		}
		
		for( final PathLeg nextLeg: anArc.legIterator()) {
			System.out.println(nextLeg.toString());
		}
		for( final PathMoment nextMoment : anArc.momentIterator(0.00015)) {
			System.out.println(
//				String.format("Traversing a path through %s", nextMoment.toString()));
				String.format("%f,%f,%f,%f,%f", nextMoment.getTime(), nextMoment.getHeight(), nextMoment.getVelocity(), nextMoment.getAcceleration(), nextMoment.getJerk()));
		}
	}
}
