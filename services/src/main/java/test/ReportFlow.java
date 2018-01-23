package test;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.MaximumFlow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;

public class ReportFlow {

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

	public abstract static class AbstractNode {
		public final int index;

		AbstractNode(int index) {
			this.index = index;
		}
		
		public void printMe(int value, AbstractNode dest) {
			System.out.println(
				String.format("%s(%d) to %s(%s) sees %d", this.getClass().getSimpleName(), this.index, dest.getClass().getSimpleName(), dest.index, value));
		}
	}

	public static class Origin extends AbstractNode {
		Origin(int index) {
			super(index);
		}
	}

	public static class Departure extends AbstractNode {
		Departure(int index) {
			super(index);
		}
	}

	public static class Arrival extends AbstractNode {
		Arrival(int index) {
			super(index);
		}
	}

	public static class Destination extends AbstractNode {
		Destination(int index) {
			super(index);
		}
	}

	public static class Upstream extends AbstractNode {
		Upstream(int label) {
			super(label);
		}
	}

	public static class Downstream extends AbstractNode {
		Downstream(int label) {
			super(label);
		}
	}

	public static class Edge {
	}

	public static void main(String[] args) {
		Upstream source = new Upstream(-5000);
		Downstream sink = new Downstream(5000);
		Upstream upstream = new Upstream(-100);
		Downstream downstream = new Downstream(100);
		AbstractNode[] origins = { upstream, new Origin(1), new Origin(2), new Origin(3), new Origin(4), new Origin(5) };
		Departure[] departures = { new Departure(0), new Departure(1), new Departure(2), new Departure(3), new Departure(4), new Departure(5) };
		AbstractNode[] arrivals = { source, new Arrival(1), new Arrival(2), new Arrival(3), new Arrival(4), new Arrival(5), new Arrival(6) };
		AbstractNode[] destinations = { sink, new Destination(1), new Destination(2), new Destination(3), new Destination(4), new Destination(5), downstream };

		GraphBuilder<AbstractNode, Edge, DefaultDirectedWeightedGraph<AbstractNode, Edge>> builder =
			new GraphBuilder(new DefaultDirectedWeightedGraph<AbstractNode, Edge>(Edge.class));

		// Scenario of Test
		// 7 On Board from upstream.  Destinations Ax2, Bx2, E, and 2x Downstream
		// Station A: 1 for B, 3 for C, 1 for E
		// Station B: 1 for C, 2 for D, 2 for downstream
		// Station C: 2 for D, 1 for E
		// Station D: 3 for E, 2 for downstream
		// Station E: 3 for downstream
	
		// Summary:
		// Upstream=>7, 9=>Downstream
		// A: 5 In, 2 Out, 5 Stay -- 10 Onboard
		// B: 5 In, 3 Out, 7 Stay -- 12 Onboard
		// C: 3 In, 4 Out, 8 Stay -- 11 Onboard
		// D: 5 In, 4 Out, 7 Stay -- 12 Onboard
		// E: 3 In, 6 Out, 6 Stay -- 9 Onboard
		
		
			builder.addVertices(origins).addVertices(departures).addVertices(arrivals).addVertices(destinations)
				.addEdge(origins[0], departures[0], new Edge(), 7)
				.addEdge(origins[1], departures[1], new Edge(), 5)
				.addEdge(origins[2], departures[2], new Edge(), 5)
				.addEdge(origins[3], departures[3], new Edge(), 3)
				.addEdge(origins[4], departures[4], new Edge(), 5)
				.addEdge(origins[5], departures[5], new Edge(), 3)
				.addEdge(arrivals[1], destinations[1], new Edge(), 2)
				.addEdge(arrivals[2], destinations[2], new Edge(), 3)
				.addEdge(arrivals[3], destinations[3], new Edge(), 4)
				.addEdge(arrivals[4], destinations[4], new Edge(), 4)
				.addEdge(arrivals[5], destinations[5], new Edge(), 6)
				.addEdge(arrivals[6], destinations[6], new Edge(), 9);
			
			builder.addEdge(departures[0], arrivals[1], new Edge(), 7)
			.addEdge(departures[0], arrivals[2], new Edge(), 10)
			.addEdge(departures[0], arrivals[3], new Edge(), 12)
			.addEdge(departures[0], arrivals[4], new Edge(), 11)
			.addEdge(departures[0], arrivals[5], new Edge(), 12)
			.addEdge(departures[0], arrivals[6], new Edge(), 9)
			.addEdge(departures[1], arrivals[2], new Edge(), 10)
			.addEdge(departures[1], arrivals[3], new Edge(), 12)
			.addEdge(departures[1], arrivals[4], new Edge(), 11)
			.addEdge(departures[1], arrivals[5], new Edge(), 12)
			.addEdge(departures[1], arrivals[6], new Edge(), 9)
			.addEdge(departures[2], arrivals[3], new Edge(), 12)
			.addEdge(departures[2], arrivals[4], new Edge(), 11)
			.addEdge(departures[2], arrivals[5], new Edge(), 12)
			.addEdge(departures[2], arrivals[6], new Edge(), 9)
			.addEdge(departures[3], arrivals[4], new Edge(), 11)
			.addEdge(departures[3], arrivals[5], new Edge(), 12)
			.addEdge(departures[3], arrivals[6], new Edge(), 9)
			.addEdge(departures[4], arrivals[5], new Edge(), 12)
			.addEdge(departures[4], arrivals[6], new Edge(), 9)
			.addEdge(departures[5], arrivals[6], new Edge(), 9)
			.addEdge(source, origins[0], new Edge(), 7)
			.addEdge(source, origins[1], new Edge(), 5)
			.addEdge(source, origins[2], new Edge(), 5)
			.addEdge(source, origins[3], new Edge(), 3)
			.addEdge(source, origins[4], new Edge(), 5)
			.addEdge(source, origins[5], new Edge(), 3)
			.addEdge(destinations[1], sink, new Edge(), 2)
			.addEdge(destinations[2], sink, new Edge(), 3)
			.addEdge(destinations[3], sink, new Edge(), 4)
			.addEdge(destinations[4], sink, new Edge(), 4)
			.addEdge(destinations[5], sink, new Edge(), 6)
			.addEdge(destinations[6], sink, new Edge(), 9);
			
//			for( int ii=0; ii<6; ii++ ) {
//				for( int jj=ii+1; jj<7; jj++) {
//					builder.addEdge(departures[ii], arrivals[jj], new Edge(), 15);
//				}
//			}

		DefaultDirectedWeightedGraph<AbstractNode, Edge> myGraph = builder.build();
		MaximumFlowAlgorithm<AbstractNode, Edge> maxFlow = new PushRelabelMFImpl(myGraph);
		MaximumFlow<Edge> flow = maxFlow.getMaximumFlow(source, sink);
		flow.getFlow().entrySet().stream().forEach(entry -> {
			myGraph.getEdgeSource(
				entry.getKey()
			).printMe(entry.getValue().intValue(), myGraph.getEdgeTarget(entry.getKey()));
		});
//			return new int[]{myGraph.getEdgeSource(entry.getKey()).index,
//			    entry.getValue().intValue()};
//		}).forEach(pair -> System.out.println(String.format("%d through %d", pair[1], pair[0])));
	}

}
