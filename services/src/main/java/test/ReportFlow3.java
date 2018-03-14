package test;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.MaximumFlow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphBuilder;

import test.ReportFlow3.AbstractNode;
import test.ReportFlow3.Edge;

public class ReportFlow3 {

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

	public static class Traversal extends AbstractNode {
		Traversal(String from, String to) {
			super(String.format("%s -> %s", from, to));
		}
	}

	public static class Arrival extends AbstractNode {
		Arrival(String index) {
			super(index);
		}
	}

	public static class SinkSource extends AbstractNode {
		SinkSource(String label) {
			super(label);
		}
	}

	public static class Edge {
	}

	public static void main(String[] args) {
		SinkSource source = new SinkSource("Source");
		SinkSource sink = new SinkSource("Sink");
		SinkSource downstream = new SinkSource("Downstream");

		Traversal[][] traversals = {
				{ new Traversal("A", "A"), new Traversal("A", "B"), new Traversal("A", "C"), new Traversal("A", "D"), new Traversal("A", "E"), new Traversal("A", "F") },
				{ new Traversal("B", "A"), new Traversal("B", "B"), new Traversal("B", "C"), new Traversal("B", "D"), new Traversal("B", "E"), new Traversal("B", "F") },
				{ new Traversal("C", "A"), new Traversal("C", "B"), new Traversal("C", "C"), new Traversal("C", "D"), new Traversal("C", "E"), new Traversal("C", "F") },
				{ new Traversal("D", "A"), new Traversal("D", "B"), new Traversal("D", "C"), new Traversal("D", "D"), new Traversal("D", "E"), new Traversal("D", "F") },
				{ new Traversal("E", "A"), new Traversal("E", "B"), new Traversal("E", "C"), new Traversal("E", "D"), new Traversal("E", "E"), new Traversal("E", "F") },
				{ new Traversal("F", "A"), new Traversal("F", "B"), new Traversal("F", "C"), new Traversal("F", "D"), new Traversal("F", "E"), new Traversal("F", "F") }
		};

		Arrival[] arrivals = { new Arrival("A"), new Arrival("B"), new Arrival("C"), new Arrival("D"), new Arrival("E"), new Arrival("F") };

		GraphBuilder<AbstractNode, DefaultWeightedEdge, DefaultDirectedWeightedGraph<AbstractNode, DefaultWeightedEdge>> builder = new GraphBuilder<AbstractNode, DefaultWeightedEdge, DefaultDirectedWeightedGraph<AbstractNode, DefaultWeightedEdge>>(
				new DefaultDirectedWeightedGraph<AbstractNode, DefaultWeightedEdge>(DefaultWeightedEdge.class));

		// Scenario of Test
		// Station A: 2 for B, 2 for C, 1 for E, and 2 for Downstream
		// Station B: 1 for C, 3 for D, 1 for F
		// Station C: 1 for D, 2 for E, 2 for Downstream
		// Station D: 2 for E, 1 for F
		// Station E: 3 for F, 2 for Downstream
		// Station F: 3 for Downstream

		// Summary:
		// Upstream=>7, 9=>Downstream
		// A: 7 In -- 7 Onboard (7[0]
		// B: 5 In, 2 Out, 5 Stay -- 10 Onboard (5[5], 5[5])
		// C: 5 In, 3 Out, 7 Stay -- 12 Onboard (5[10-3],5[10-3],5[7])
		// D: 3 In, 4 Out, 8 Stay -- 11 Onboard (5[13-7],5[13-7],5[10-4],3[8])
		// E: 5 In, 4 Out, 7 Stay -- 12 Onboard (5[18-11],5[18-11],5[15-8],3[13-4],5[7])
		// F: 3 In, 6 Out, 6 Stay -- 9 Onboard (5[21-17], 5[21-17], 5[18-14], 3[16-10], 5[10-6])

		builder
		   .addVertices(traversals[0]).addVertices(traversals[1]).addVertices(traversals[2])
		   .addVertices(traversals[3]).addVertices(traversals[4]).addVertices(traversals[5])
		   .addVertices(arrivals).addVertex(source).addVertex(sink).addVertex(downstream)
			.addEdge(source, traversals[0][0], 7).addEdge(source, traversals[1][1], 5)
			.addEdge(source, traversals[2][2], 5).addEdge(source, traversals[3][3], 3)
			.addEdge(source, traversals[4][4], 5).addEdge(source, traversals[5][5], 3)
			.addEdge(arrivals[1], sink, 2).addEdge(arrivals[2], sink, 3)
			.addEdge(arrivals[3], sink, 4).addEdge(arrivals[4], sink, 4)
			.addEdge(arrivals[5], sink, 6).addEdge(downstream, sink, 9);
		
		builder.addVertices(traversals[1]).addVertices(traversals[2]).addVertices(traversals[3]).addVertices(traversals[4]).addVertices(traversals[5]);

		builder.addEdge(traversals[0][0], arrivals[1], 2)
				.addEdge(traversals[0][0], traversals[0][1], 5)
				.addEdge(traversals[0][1], arrivals[2], 3)
				.addEdge(traversals[0][1], traversals[0][2], 4)
				.addEdge(traversals[0][2], arrivals[3], 0)
				.addEdge(traversals[0][2], traversals[0][3], 4)
				.addEdge(traversals[0][3], arrivals[4], 4)
				.addEdge(traversals[0][3], traversals[0][4], 3)
				.addEdge(traversals[0][4], arrivals[5], 0)
				.addEdge(traversals[0][4], traversals[0][5], 3)
				.addEdge(traversals[0][5], downstream, 3)

				.addEdge(traversals[1][1], arrivals[2], 3)
				.addEdge(traversals[1][1], traversals[1][2], 5)
				.addEdge(traversals[1][2], arrivals[3], 4)
				.addEdge(traversals[1][2], traversals[1][3], 4)
				.addEdge(traversals[1][3], arrivals[4], 4)
				.addEdge(traversals[1][3], traversals[1][4], 4)
				.addEdge(traversals[1][4], arrivals[5], 6)
				.addEdge(traversals[1][4], traversals[1][5], 3)
				.addEdge(traversals[1][5], downstream, 3)

				.addEdge(traversals[2][2], arrivals[3], 4)
				.addEdge(traversals[2][2], traversals[2][3], 5)
				.addEdge(traversals[2][3], arrivals[4], 4)
				.addEdge(traversals[2][3], traversals[2][4], 5)
				.addEdge(traversals[2][4], arrivals[5], 6)
				.addEdge(traversals[2][4], traversals[2][5], 5)
				.addEdge(traversals[2][5], downstream, 5)

				.addEdge(traversals[3][3], arrivals[4], 4)
				.addEdge(traversals[3][3], traversals[3][4], 3)
				.addEdge(traversals[3][4], arrivals[5], 6)
				.addEdge(traversals[3][4], traversals[3][5], 3)
				.addEdge(traversals[3][5], downstream, 3)

				.addEdge(traversals[4][4], arrivals[5], 6)
				.addEdge(traversals[4][4], traversals[4][5], 5)
				.addEdge(traversals[4][5], downstream, 5)

				.addEdge(traversals[5][5], downstream, 3);

		DefaultDirectedWeightedGraph<AbstractNode, DefaultWeightedEdge> myGraph = builder.build();
		MaximumFlowAlgorithm<AbstractNode, DefaultWeightedEdge> maxFlow = new PushRelabelMFImpl<AbstractNode, DefaultWeightedEdge>(myGraph);
		MaximumFlow<DefaultWeightedEdge> flow = maxFlow.getMaximumFlow(source, sink);
		flow.getFlow().entrySet().stream().forEach(entry -> {
			myGraph.getEdgeSource(entry.getKey()).printMe(entry.getValue().intValue(),
					myGraph.getEdgeTarget(entry.getKey()));
		});
		// return new int[]{myGraph.getDefaultWeightedEdgeSource(entry.getKey()).index,
		// entry.getValue().intValue()};
		// }).forEach(pair -> System.out.println(String.format("%d through %d", pair[1],
		// pair[0])));
	}

}
