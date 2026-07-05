package test;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;


public class ReportFlow4
{

   public abstract static class AbstractNode
   {
      public final String index;


      AbstractNode( String index )
      {
         this.index = index;
      }


      public void printMe(double value, AbstractNode dest)
      {
         if (value > 0) {
            System.out.println(
               String.format(
                  "%s(%s) to %s(%s) sees %f",
                  this.getClass()
                     .getSimpleName(),
                  this.index,
                  dest.getClass()
                     .getSimpleName(),
                  dest.index,
                  value));
         }
      }
   }


   public static class Pickup
   extends AbstractNode
   {
      Pickup( String from )
      {
         super(from);
      }
   }


   public static class DropOff
   extends AbstractNode
   {
      DropOff( String to )
      {
         super(to);
      }
   }


   public static class Rider
   extends AbstractNode
   {
      Rider( String index )
      {
         super("Rider" + index);
      }
   }


   public static void main(String[] args) {
      GraphBuilder<AbstractNode, DefaultWeightedEdge, SimpleWeightedGraph<AbstractNode, DefaultWeightedEdge>> builder = new GraphBuilder<AbstractNode, DefaultWeightedEdge, SimpleWeightedGraph<AbstractNode, DefaultWeightedEdge>>(
            new SimpleWeightedGraph<AbstractNode, DefaultWeightedEdge>(DefaultWeightedEdge.class));

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
      Pickup pickups[] = new Pickup[] {
         new Pickup("A1"), new Pickup("A2"), new Pickup("A3"), new Pickup("A4"), new Pickup("A5"), new Pickup("A6"), new Pickup("A7"),
         new Pickup("B1"), new Pickup("B2"), new Pickup("B3"), new Pickup("B4"), new Pickup("B5"),
         new Pickup("C1"), new Pickup("C2"), new Pickup("C3"), new Pickup("C4"), new Pickup("C5"),
         new Pickup("D1"), new Pickup("D2"), new Pickup("D3"),
         new Pickup("E1"), new Pickup("E2"), new Pickup("E3"), new Pickup("E4"), new Pickup("E5"),
         new Pickup("F1"), new Pickup("F2"), new Pickup("F3")
      };
      DropOff dropOffs[] = new DropOff[] {
        new DropOff("B1"), new DropOff("B2"), 
        new DropOff("C1"), new DropOff("C2"), new DropOff("C3"),
        new DropOff("D1"), new DropOff("D2"), new DropOff("D3"), new DropOff("D4"),
        new DropOff("E1"), new DropOff("E2"), new DropOff("E3"), new DropOff("E4"),
        new DropOff("F1"), new DropOff("F2"), new DropOff("F3"), new DropOff("F4"), new DropOff("F5"), new DropOff("F6"), 
      };
      
      Rider riders[] = new Rider[] {
        new Rider("1"), new Rider("2"), new Rider("3"), new Rider("4"), new Rider("5"), new Rider("6"), new Rider("7"), new Rider("8"), new Rider("9"),
      };

      builder
         .addVertices(pickups).addVertices(dropOffs).addVertices(riders)
      ;
      
      // A: 7 In -- 7 Onboard (7[0])
      // B: 5 In, 2 Out, 5 Stay -- 10 Onboard 
      // -- P(A1->B1): 7/7
      // -- P(A2->B2) = P(A2->B2|A1===>B1) + P(A2->B2|A1=/=>B1) = 6/6 * 7/7 + 7/7 * 0/7 = 1 
      // -- A->B2: 0 + (7/7)*(6/6) + 0 + 0 + 0 + 0 + 0 + 0
      // C: 5 In, 3 Out, 7 Stay -- 12 Onboard (5[10-3],5[10-3],5[7])
      // -- A->C1: 0 + 0 + (5/10) + 0 + 0 + 0 + 0 + 0; 
      // -- A->C2: 0 + 0 + (5/10)*(
      // D: 3 In, 4 Out, 8 Stay -- 11 Onboard (5[13-7],5[13-7],5[10-4],3[8])
      // E: 5 In, 4 Out, 7 Stay -- 12 Onboard (5[18-11],5[18-11],5[15-8],3[13-4],5[7])
      // F: 3 In, 6 Out, 6 Stay -- 9 Onboard (5[21-17], 5[21-17], 5[18-14], 3[16-10], 5[10-6])
      int initialCount = 7;
      double[][] partialOddsA = new double[19][initialCount+1];
      double[] normScoresA = new double[19];
      partialOddsA[0][initialCount] = 1.0;

      populateOdds(initialCount, 7, partialOddsA, 0, 2, normScoresA);
      populateOdds(initialCount, 10, partialOddsA, 2, 3, normScoresA);
      populateOdds(initialCount, 11, partialOddsA, 5, 4, normScoresA);
      populateOdds(initialCount, 9, partialOddsA, 9, 9, normScoresA);

//      for (ii = 2, total = 10; ii<5; ii++, total--) {
//         final int iiNext = ii + 1;
//         int jj, peers;
//         for (jj = (initialCount - 1), peers = (total - initialCount + 1); jj >= 0; jj--, peers++) {
//            final int jjNext = jj + 1;
//            normScoresA[iiNext][jj] = ((normScoresA[ii][jjNext] * jj) + (normScoresA[ii][jj] * peers)) / total;
//         }
//         normScoresA[iiNext][initialCount] = (normScoresA[ii][initialCount] * peers) / total;
//      }
      

      // B
      builder
         .addEdge(pickups[0], dropOffs[0], partialOddsA[1][7])
         .addEdge(pickups[0], dropOffs[1], partialOddsA[2][7])
         .addEdge(pickups[1], dropOffs[0], partialOddsA[1][6])
         .addEdge(pickups[1], dropOffs[1], partialOddsA[2][6])
      ;
      
      int ii;
      for( ii = 2; ii<7; ii++ ) {
         builder
             // C
            .addEdge(pickups[ii], dropOffs[2], normScoresA[3])
            .addEdge(pickups[ii], dropOffs[3], normScoresA[4])
            .addEdge(pickups[ii], dropOffs[4], normScoresA[5])
            // D
            // .addEdge(pickups[ii], dropOffs[5], 1)
            // E
            .addEdge(pickups[ii], dropOffs[9], normScoresA[6])
            .addEdge(pickups[ii], dropOffs[10], normScoresA[7])
            .addEdge(pickups[ii], dropOffs[11], normScoresA[8])
            .addEdge(pickups[ii], dropOffs[12], normScoresA[9])
            // F
            // .addEdge(pickups[ii], dropOffs[13], 1)
            // G
            .addEdge(pickups[ii], riders[0], normScoresA[10])
            .addEdge(pickups[ii], riders[1], normScoresA[11])
            .addEdge(pickups[ii], riders[2], normScoresA[12])
            .addEdge(pickups[ii], riders[3], normScoresA[13])
            .addEdge(pickups[ii], riders[4], normScoresA[14])
            .addEdge(pickups[ii], riders[5], normScoresA[15])
            .addEdge(pickups[ii], riders[6], normScoresA[16])
            .addEdge(pickups[ii], riders[7], normScoresA[17])
            .addEdge(pickups[ii], riders[8], normScoresA[18])
         ;
      }
      
      initialCount = 5;
      partialOddsA = new double[27][initialCount+1];
      normScoresA = new double[27];
      partialOddsA[0][initialCount] = 1.0;

      populateOdds(initialCount, 5, partialOddsA, 0, 3, normScoresA);
      populateOdds(initialCount, 10, partialOddsA, 3, 4, normScoresA);
      populateOdds(initialCount, 11, partialOddsA, 7, 4, normScoresA);
      populateOdds(initialCount, 11, partialOddsA, 11, 6, normScoresA);
      populateOdds(initialCount, 9, partialOddsA, 17, 9, normScoresA);

//      System.out.println(Arrays.toString(normScoresA));

      for( ii = 7; ii<12; ii++ ) {
         builder
            // C
            .addEdge(pickups[ii], dropOffs[2], normScoresA[1])
            .addEdge(pickups[ii], dropOffs[3], normScoresA[2])
            .addEdge(pickups[ii], dropOffs[4], normScoresA[3])
            // D
            .addEdge(pickups[ii], dropOffs[5], normScoresA[4])
            .addEdge(pickups[ii], dropOffs[6], normScoresA[5])
            .addEdge(pickups[ii], dropOffs[7], normScoresA[6])
            .addEdge(pickups[ii], dropOffs[8], normScoresA[7])
            // E
            .addEdge(pickups[ii], dropOffs[9], normScoresA[8])
            .addEdge(pickups[ii], dropOffs[10], normScoresA[9])
            .addEdge(pickups[ii], dropOffs[11], normScoresA[10])
            .addEdge(pickups[ii], dropOffs[12], normScoresA[11])
            // F
            .addEdge(pickups[ii], dropOffs[13], normScoresA[12])
            .addEdge(pickups[ii], dropOffs[14], normScoresA[13])
            .addEdge(pickups[ii], dropOffs[15], normScoresA[14])
            .addEdge(pickups[ii], dropOffs[16], normScoresA[15])
            .addEdge(pickups[ii], dropOffs[17], normScoresA[16])
            .addEdge(pickups[ii], dropOffs[18], normScoresA[17])
            // G
            .addEdge(pickups[ii], riders[0], normScoresA[18])
            .addEdge(pickups[ii], riders[1], normScoresA[19])
            .addEdge(pickups[ii], riders[2], normScoresA[20])
            .addEdge(pickups[ii], riders[3], normScoresA[21])
            .addEdge(pickups[ii], riders[4], normScoresA[22])
            .addEdge(pickups[ii], riders[5], normScoresA[23])
            .addEdge(pickups[ii], riders[6], normScoresA[24])
            .addEdge(pickups[ii], riders[7], normScoresA[25])
            .addEdge(pickups[ii], riders[8], normScoresA[26])
         ;
      }
      
      initialCount = 5;
      partialOddsA = new double[24][initialCount+1];
      normScoresA = new double[24];
      partialOddsA[0][initialCount] = 1.0;

      populateOdds(initialCount, 10, partialOddsA, 0, 4, normScoresA);
      populateOdds(initialCount, 11, partialOddsA, 4, 4, normScoresA);
      populateOdds(initialCount, 11, partialOddsA, 8, 6, normScoresA);
      populateOdds(initialCount, 9, partialOddsA, 14, 9, normScoresA);

//      System.out.println(Arrays.toString(normScoresA));

      for( ii = 12; ii<17; ii++ ) {
         builder
            // D
            .addEdge(pickups[ii], dropOffs[5], normScoresA[1])
            .addEdge(pickups[ii], dropOffs[6], normScoresA[2])
            .addEdge(pickups[ii], dropOffs[7], normScoresA[3])
            .addEdge(pickups[ii], dropOffs[8], normScoresA[4])
            // E
            .addEdge(pickups[ii], dropOffs[9], normScoresA[5])
            .addEdge(pickups[ii], dropOffs[10], normScoresA[6])
            .addEdge(pickups[ii], dropOffs[11], normScoresA[7])
            .addEdge(pickups[ii], dropOffs[12], normScoresA[8])
            // F
            .addEdge(pickups[ii], dropOffs[13], normScoresA[9])
            .addEdge(pickups[ii], dropOffs[14], normScoresA[10])
            .addEdge(pickups[ii], dropOffs[15], normScoresA[11])
            .addEdge(pickups[ii], dropOffs[16], normScoresA[12])
            .addEdge(pickups[ii], dropOffs[17], normScoresA[13])
            .addEdge(pickups[ii], dropOffs[18], normScoresA[14])
            // G
            .addEdge(pickups[ii], riders[0], normScoresA[15])
            .addEdge(pickups[ii], riders[1], normScoresA[16])
            .addEdge(pickups[ii], riders[2], normScoresA[17])
            .addEdge(pickups[ii], riders[3], normScoresA[18])
            .addEdge(pickups[ii], riders[4], normScoresA[19])
            .addEdge(pickups[ii], riders[5], normScoresA[20])
            .addEdge(pickups[ii], riders[6], normScoresA[21])
            .addEdge(pickups[ii], riders[7], normScoresA[22])
            .addEdge(pickups[ii], riders[8], normScoresA[23])
         ;
      }
      
      initialCount = 3;
      partialOddsA = new double[20][initialCount+1];
      normScoresA = new double[20];
      partialOddsA[0][initialCount] = 1.0;

      populateOdds(initialCount, 11, partialOddsA, 0, 4, normScoresA);
      populateOdds(initialCount, 11, partialOddsA, 4, 6, normScoresA);
      populateOdds(initialCount, 9, partialOddsA, 10, 9, normScoresA);

//      System.out.println(Arrays.toString(normScoresA));

      for( ii = 17; ii<20; ii++ ) {
         builder
            // E
            .addEdge(pickups[ii], dropOffs[9], normScoresA[1])
            .addEdge(pickups[ii], dropOffs[10], normScoresA[2])
            .addEdge(pickups[ii], dropOffs[11], normScoresA[3])
            .addEdge(pickups[ii], dropOffs[12], normScoresA[4])
            // F
            .addEdge(pickups[ii], dropOffs[13], normScoresA[5])
            .addEdge(pickups[ii], dropOffs[14], normScoresA[6])
            .addEdge(pickups[ii], dropOffs[15], normScoresA[7])
            .addEdge(pickups[ii], dropOffs[16], normScoresA[8])
            .addEdge(pickups[ii], dropOffs[17], normScoresA[9])
            .addEdge(pickups[ii], dropOffs[18], normScoresA[10])
            // G
            .addEdge(pickups[ii], riders[0], normScoresA[11])
            .addEdge(pickups[ii], riders[1], normScoresA[12])
            .addEdge(pickups[ii], riders[2], normScoresA[13])
            .addEdge(pickups[ii], riders[3], normScoresA[14])
            .addEdge(pickups[ii], riders[4], normScoresA[15])
            .addEdge(pickups[ii], riders[5], normScoresA[16])
            .addEdge(pickups[ii], riders[6], normScoresA[17])
            .addEdge(pickups[ii], riders[7], normScoresA[18])
            .addEdge(pickups[ii], riders[8], normScoresA[19])
         ;
      }

      initialCount = 5;
      partialOddsA = new double[16][initialCount+1];
      normScoresA = new double[16];
      partialOddsA[0][initialCount] = 1.0;

      populateOdds(initialCount, 11, partialOddsA, 0, 6, normScoresA);
      populateOdds(initialCount, 9, partialOddsA, 6, 9, normScoresA);

//      System.out.println(Arrays.toString(normScoresA));

      for( ii = 20; ii<25; ii++ ) {
         builder
            // F
            .addEdge(pickups[ii], dropOffs[13], normScoresA[1])
            .addEdge(pickups[ii], dropOffs[14], normScoresA[2])
            .addEdge(pickups[ii], dropOffs[15], normScoresA[3])
            .addEdge(pickups[ii], dropOffs[16], normScoresA[4])
            .addEdge(pickups[ii], dropOffs[17], normScoresA[5])
            .addEdge(pickups[ii], dropOffs[18], normScoresA[6])
            // G
            .addEdge(pickups[ii], riders[0], normScoresA[7])
            .addEdge(pickups[ii], riders[1], normScoresA[8])
            .addEdge(pickups[ii], riders[2], normScoresA[9])
            .addEdge(pickups[ii], riders[3], normScoresA[10])
            .addEdge(pickups[ii], riders[4], normScoresA[11])
            .addEdge(pickups[ii], riders[5], normScoresA[12])
            .addEdge(pickups[ii], riders[6], normScoresA[13])
            .addEdge(pickups[ii], riders[7], normScoresA[14])
            .addEdge(pickups[ii], riders[8], normScoresA[15])
         ;
      }

      initialCount = 3;
      partialOddsA = new double[10][initialCount+1];
      normScoresA = new double[10];
      partialOddsA[0][initialCount] = 1.0;

      populateOdds(initialCount, 9, partialOddsA, 0, 9, normScoresA);

//      System.out.println(Arrays.toString(normScoresA));

      for( ii = 25; ii<28; ii++ ) {
         builder
            .addEdge(pickups[ii], riders[0], normScoresA[1])
            .addEdge(pickups[ii], riders[1], normScoresA[2])
            .addEdge(pickups[ii], riders[2], normScoresA[3])
            .addEdge(pickups[ii], riders[3], normScoresA[4])
            .addEdge(pickups[ii], riders[4], normScoresA[5])
            .addEdge(pickups[ii], riders[5], normScoresA[6])
            .addEdge(pickups[ii], riders[6], normScoresA[7])
            .addEdge(pickups[ii], riders[7], normScoresA[8])
            .addEdge(pickups[ii], riders[8], normScoresA[9])
         ;
      }
      
      final SimpleWeightedGraph<AbstractNode, DefaultWeightedEdge> myGraph = builder.build();
      final Set<AbstractNode> partitionOne = Arrays.<AbstractNode>stream(pickups).collect(Collectors.toSet());
      final Set<AbstractNode> partitionTwo =
         Arrays.<AbstractNode[]>stream(
            new AbstractNode[][] { dropOffs, riders }
         ).<AbstractNode>flatMap(nextArray -> {
            return Arrays.<AbstractNode>stream(nextArray);
         }).collect(
            Collectors.toSet());
      MatchingAlgorithm<AbstractNode, DefaultWeightedEdge> matchingAlg =
         new MaximumWeightBipartiteMatching<AbstractNode, DefaultWeightedEdge>(
            myGraph, partitionOne, partitionTwo);
      Matching<AbstractNode, DefaultWeightedEdge> matching = matchingAlg.getMatching();
      if (matching.isPerfect()) {
         System.out.println("Found a perfect maximum matching");
      } else {
         System.out.println("Found an imperfect maximum matching");
      }
      matching.getEdges().stream().forEach(entry -> {
         myGraph.getEdgeSource(entry).printMe(myGraph.getEdgeWeight(entry),
               myGraph.getEdgeTarget(entry));
      });
   }


   public static void populateOdds(final int ownInitial, final int totalOnBoard, final double[][] partialOdds, final int prevIndex, final int slotCount, double[] normalWeight )
   {
      int ii, total;
      final int finalIndex = prevIndex + slotCount;
      for (ii = prevIndex, total = totalOnBoard; ii < finalIndex; ii++, total--) {
         int jj, peers;
         final int iiNext = ii + 1;
         peers = total - ownInitial;
         System.out.println(
            String.format("[%d][%d] for peers(%d) is %f out of %d", iiNext, ownInitial, peers, (peers * partialOdds[ii][ownInitial] / total), total));
         partialOdds[iiNext][ownInitial] = (partialOdds[ii][ownInitial] * peers) / total;
         for (jj = ownInitial-1, peers++; jj >= 0; jj--, peers++) {
            final int jjNext = jj + 1;
            partialOdds[iiNext][jjNext] = ((partialOdds[ii][jjNext] * jj) + (partialOdds[ii][jj] * peers)) / total;
            System.out.println(
               String.format("[%d][%d] for peers(%d) is %f + %f out of %d -> %f", iiNext, jj, peers, (jj * partialOdds[ii][jjNext] / total), (peers * partialOdds[ii][jj] / total), total, partialOdds[iiNext][jj]));
         }
         partialOdds[iiNext][0] = (partialOdds[ii][0] * peers) / total;
         
         System.out.println(Arrays.toString(partialOdds[iiNext]));
         normalWeight[iiNext] = 0;
         for(jj=0; jj<=ownInitial; jj++) {
            normalWeight[iiNext] += (partialOdds[iiNext][jj]);
         }
      }
   }
}
