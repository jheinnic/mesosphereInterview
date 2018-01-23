package info.jchein.mesosphere.elevator.simulator.traveller;


public interface IBehaviorStrategy {
	TravellerContext allocateNewTraveller();

	TravellerContext activate(TravellerContext travellerContext);
	
}
