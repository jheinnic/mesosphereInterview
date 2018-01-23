package test;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.builder.DirectedWeightedGraphBuilder;
import org.jgrapht.graph.builder.GraphBuilder;

public class TryFlow {

	public static class PassengerLog {
		final long timeIndex;
		final int floorIndex;
		final int passengersOnArrival;
		final int passengersDroppedOff;
		final int passengersPickedUp;
		final int passengersOnDeparture;

		public PassengerLog(long timeIndex, int floorIndex, int pOnArrival, int pDroppedOff, int pPickedUp,
				int pOnDep) {
			this.timeIndex = timeIndex;
			this.floorIndex = floorIndex;
			this.passengersOnArrival = pOnArrival;
			this.passengersDroppedOff = pDroppedOff;
			this.passengersPickedUp = pPickedUp;
			this.passengersOnDeparture = pOnDep;
		}
	}

	public static class SequentialStops {
		final double distanceTravelled;
		final double fuelConsumed;

		public SequentialStops(double distanceTravelled, double fuelConsumed) {
			this.distanceTravelled = distanceTravelled;
			this.fuelConsumed = fuelConsumed;
		}
	}

	public static class SetDropOffCallsFor {
		final int earliestPickupFrom;
		final long earliestBoarding;

		public SetDropOffCallsFor(int e1, long e2) {
			this.earliestPickupFrom = e1;
			this.earliestBoarding = e2;
		}
	}

	public static void main(String[] args) {
		PassengerLog[] logs = { new PassengerLog(System.currentTimeMillis(), 1, 3, 2, 1, 4),
				new PassengerLog(System.currentTimeMillis(), 2, 4, 2, 2, 4),
				new PassengerLog(System.currentTimeMillis(), 3, 4, 1, 0, 3),
				new PassengerLog(System.currentTimeMillis(), 4, 3, 2, 1, 2) };
		GraphBuilder<PassengerLog, SequentialStops, DefaultDirectedWeightedGraph<PassengerLog, SequentialStops>> builder = new GraphBuilder(
				new DefaultDirectedWeightedGraph<PassengerLog, SequentialStops>(SequentialStops.class));

		DefaultDirectedWeightedGraph<PassengerLog, SequentialStops> myGraph = builder.addVertices(logs)
				.addEdge(logs[0], logs[1], new SequentialStops(1, 4), 3.0)
		.addEdge(logs[1], logs[2], new SequentialStops(5, 5), 2.0)
		.addEdge(logs[2], logs[3], new SequentialStops(4, 3), 4.2)
		.build();
	}

}
