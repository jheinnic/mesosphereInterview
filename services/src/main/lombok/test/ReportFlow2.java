package test;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.MaximumFlow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;

import test.ReportFlow2.AbstractNode;
import test.ReportFlow2.Edge;

public class ReportFlow2 {

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
		public final String index;

		AbstractNode(String index) {
			this.index = index;
		}

		public void printMe(int value, AbstractNode dest) {
			if (value > 0) {
				System.out.println(String.format("%s(%s) to %s(%s) sees %d", this.getClass().getSimpleName(),
						this.index, dest.getClass().getSimpleName(), dest.index, value));
			}
		}
	}

	public static class Departure extends AbstractNode {
		Departure(String index) {
			super(index);
		}
	}

	public static class Traversal extends AbstractNode {
		Traversal(String[] path) {
			super(Arrays.asList(path).stream().collect(Collectors.joining(" -> ")));
		}
	}

	public static class Arrival extends AbstractNode {
		Arrival(String index) {
			super(index);
		}
	}

	public static class Destination extends AbstractNode {
		Destination(String index) {
			super(index);
		}
	}

	public static class Origin extends AbstractNode {
		Origin(String label) {
			super(label);
		}
	}

	public static class Downstream extends AbstractNode {
		Downstream(String label) {
			super(label);
		}
	}

	public static class Edge {
	}

	public static void main(String[] args) {
		Origin source = new Origin("Source");
		Downstream sink = new Downstream("Sink");
		AbstractNode[][] departures = {
				{ new Departure("UP"), new Departure("A"), new Departure("B"), new Departure("C"), new Departure("D"),
						new Departure("E") },
				{ new Traversal(new String[] { "UP", "A" }), new Traversal(new String[] { "A", "B" }),
						new Traversal(new String[] { "B", "C" }), new Traversal(new String[] { "C", "D" }),
						new Traversal(new String[] { "D", "E" }) },
				{ new Traversal(new String[] { "UP", "A", "B" }), new Traversal(new String[] { "A", "B", "C" }),
						new Traversal(new String[] { "B", "C", "D" }), new Traversal(new String[] { "C", "D", "E" }) },
				{ new Traversal(new String[] { "UP", "A", "B", "C" }),
						new Traversal(new String[] { "A", "B", "C", "D" }),
						new Traversal(new String[] { "B", "C", "D", "E" }) },
				{ new Traversal(new String[] { "UP", "A", "B", "C", "D" }),
						new Traversal(new String[] { "A", "B", "C", "D", "E" }) },
				{ new Traversal(new String[] { "UP", "A", "B", "C", "D", "E" }) } };

		AbstractNode[] arrivals = { source, new Arrival("A"), new Arrival("B"), new Arrival("C"), new Arrival("D"),
				new Arrival("E"), new Arrival("DOWN") };

		GraphBuilder<AbstractNode, Edge, DefaultDirectedWeightedGraph<AbstractNode, Edge>> builder = new GraphBuilder<AbstractNode, Edge, DefaultDirectedWeightedGraph<AbstractNode, Edge>>(
				new DefaultDirectedWeightedGraph<AbstractNode, Edge>(Edge.class));

		// Scenario of Test
		// 7 On Board from upstream. Destinations Ax2, Bx2, E, and 2x Downstream
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

		builder.addVertices(departures[0]).addVertices(arrivals).addVertex(source).addVertex(sink)
				.addEdge(source, departures[0][0], new Edge(), 7).addEdge(source, departures[0][1], new Edge(), 5)
				.addEdge(source, departures[0][2], new Edge(), 5).addEdge(source, departures[0][3], new Edge(), 3)
				.addEdge(source, departures[0][4], new Edge(), 5).addEdge(source, departures[0][5], new Edge(), 3)
				.addEdge(arrivals[1], sink, new Edge(), 2).addEdge(arrivals[2], sink, new Edge(), 3)
				.addEdge(arrivals[3], sink, new Edge(), 4).addEdge(arrivals[4], sink, new Edge(), 4)
				.addEdge(arrivals[5], sink, new Edge(), 6).addEdge(arrivals[6], sink, new Edge(), 9);
		
		builder.addVertices(departures[1]).addVertices(departures[2]).addVertices(departures[3]).addVertices(departures[4]).addVertices(departures[5]);

		builder.addEdge(departures[0][0], arrivals[1], new Edge(), 2)
				.addEdge(departures[0][0], departures[1][0], new Edge(), 5)
				.addEdge(departures[1][0], departures[2][0], new Edge(), 7)
				.addEdge(departures[2][0], departures[3][0], new Edge(), 8)
				.addEdge(departures[3][0], departures[4][0], new Edge(), 7)
				.addEdge(departures[4][0], departures[5][0], new Edge(), 6)

				.addEdge(departures[0][1], arrivals[2], new Edge(), 3)
				.addEdge(departures[0][1], departures[1][1], new Edge(), 7)
				.addEdge(departures[1][1], departures[2][1], new Edge(), 8)
				.addEdge(departures[2][1], departures[3][1], new Edge(), 7)
				.addEdge(departures[3][1], departures[4][1], new Edge(), 6)

				.addEdge(departures[0][2], arrivals[3], new Edge(), 4)
				.addEdge(departures[0][2], departures[1][2], new Edge(), 8)
				.addEdge(departures[1][2], departures[2][2], new Edge(), 7)
				.addEdge(departures[2][2], departures[3][2], new Edge(), 6)

				.addEdge(departures[0][3], arrivals[4], new Edge(), 4)
				.addEdge(departures[0][3], departures[1][3], new Edge(), 7)
				.addEdge(departures[1][3], departures[2][3], new Edge(), 6)

				.addEdge(departures[0][4], arrivals[5], new Edge(), 6)
				.addEdge(departures[0][4], departures[1][4], new Edge(), 6)

				.addEdge(departures[0][5], arrivals[6], new Edge(), 9);

		DefaultDirectedWeightedGraph<AbstractNode, Edge> myGraph = builder.build();
		MaximumFlowAlgorithm<AbstractNode, Edge> maxFlow = new PushRelabelMFImpl<AbstractNode, Edge>(myGraph);
		MaximumFlow<Edge> flow = maxFlow.getMaximumFlow(source, sink);
		flow.getFlow().entrySet().stream().forEach(entry -> {
			myGraph.getEdgeSource(entry.getKey()).printMe(entry.getValue().intValue(),
					myGraph.getEdgeTarget(entry.getKey()));
		});
		// return new int[]{myGraph.getEdgeSource(entry.getKey()).index,
		// entry.getValue().intValue()};
		// }).forEach(pair -> System.out.println(String.format("%d through %d", pair[1],
		// pair[0])));
	}

}
