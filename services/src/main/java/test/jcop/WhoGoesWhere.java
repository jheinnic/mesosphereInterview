package test.jcop;


import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/*
 * Copyright © 2010 by Ondrej Skalicka. All Rights Reserved
 */
import cz.cvut.felk.cig.jcop.problem.BaseProblem;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.Fitness;
import cz.cvut.felk.cig.jcop.problem.GlobalSearchProblem;
import cz.cvut.felk.cig.jcop.problem.RandomConfigurationProblem;
import cz.cvut.felk.cig.jcop.util.JcopRandom;
import lombok.Getter;


/**
 * Knapsack problem definition consists of having a knapsack of given capacity and a list of items, each having weight
 * and price. The goal is to fit in the knapsack items worth the most possible amount while not exceeding the knapsack
 * capacity.
 *
 * @author Ondrej Skalicka
 * @see <a href="http://service.felk.cvut.cz/courses/X36PAA/knapsack.html"> Knapsack problem on felk.cvut.fel</a>
 */
public abstract class WhoGoesWhere
extends BaseProblem
implements RandomConfigurationProblem, GlobalSearchProblem, IWhoGoesWhereProblem
{
   /**
    * Id of problem instance if supplied in {@link #Knapsack(java.io.File, String)}.
    */
   @Getter
   protected final String id;
   @Getter
   private final PassengerExchange[] exchanges;
   @Getter
   private final PassengerExit[] departures;
   @Getter
   private final PassengerEntry[] arrivals;
   @Getter
   private final int attributeCount;
   @Getter
   private final int firstMatchAttributeIndex;


   WhoGoesWhere( String id, Building building )
   {
      this.id = id;
      ImmutableList.Builder<PassengerExchange> exchangesBuilder = ImmutableList.<PassengerExchange> builder();
      ImmutableList.Builder<PassengerEntry> entriesBuilder = ImmutableList.<PassengerEntry> builder();
      ImmutableList.Builder<PassengerExit> exitsBuilder = ImmutableList.<PassengerExit> builder();
      
      int exchangeCount = 0;
      int passengerCount = 0;
      int arrivalCount = 0;
      int departureCount = 0;
      double weightLoad = 0.0;
      for (final FloorLanding nextFloor : building.getFloorLandings()) {
         for (final BoardingEvent nextEvent : nextFloor.getBoardingEvents()) {
            final PassengerExchange nextExchange =
               new PassengerExchange(exchangeCount++, nextFloor, nextEvent, arrivalCount, departureCount, weightLoad);
            weightLoad = nextExchange.getExpectedOutgoingWeight();
            exchangesBuilder.add(nextExchange);

            final int passengersIn = nextEvent.getPassengersIn();
            final int passengersOut = nextEvent.getPassengersOut();
            passengerCount += passengersIn - passengersOut;
            arrivalCount += passengersIn;

            for (int ii = 0; ii < passengersOut; ii++) {
               final PassengerExit nextExit = new PassengerExit(departureCount++, nextExchange, ii);
               exitsBuilder.add(nextExit);
            }

            Preconditions.checkState(passengerCount >= 0);
         }
      }

      if (passengerCount > 0) {
         final PassengerExchange pseudoExchange = new PassengerExchange(exchangeCount++, passengerCount, arrivalCount, departureCount, weightLoad);
         for (int ii = 0; ii < passengerCount; ii++) {
            final PassengerExit nextExit = new PassengerExit(departureCount++, pseudoExchange, ii);
            exitsBuilder.add(nextExit);
         }
         exchangesBuilder.add(pseudoExchange);
      }

      ImmutableList<PassengerExchange> tempExchanges = exchangesBuilder.build();
      this.departures = exitsBuilder.build().toArray(new PassengerExit[departureCount]);
      this.exchanges = new PassengerExchange[exchangeCount];

      // Count backwards to identify the number of potential Exit candidates for each Entry based by
      // adding the Exit counts for every run that begins with all entries from a single exchange.
      int matchCandidateCount = 0;
      int nextWeightIndex = 2 * (this.departures.length - 1);
      int nextArrivalIndex = departureCount - 1;
      for (final PassengerExchange prevExchange : tempExchanges.reverse()) {
         this.exchanges[--exchangeCount] = prevExchange;
         final int passengersIn = prevExchange.getPassengersIn();
         final int passengersOut = prevExchange.getPassengersOut();
         matchCandidateCount += passengersOut;
         passengerCount += passengersOut - passengersIn;

         for (int ii = 0; ii < passengersIn; ii++) {
            final PassengerEntry nextEntry = new PassengerEntry(nextArrivalIndex--, prevExchange, ii, nextWeightIndex, matchCandidateCount);
            nextWeightIndex -= 2;
            entriesBuilder.add(nextEntry);
         }
         
         if (passengerCount == 0) {
            matchCandidateCount = 0;
         }
      }

      this.arrivals = new PassengerEntry[departureCount];
      final ImmutableList<PassengerEntry> tempArrivals = entriesBuilder.build().reverse();

      // Calculate the match attribute index offsets
      int nextEntryIndex = 0;
      int nextMatchIndex = this.firstMatchAttributeIndex = this.departures.length + this.departures.length - 1;
      for (final PassengerEntry nextEntry : tempArrivals) {
         matchCandidateCount = nextEntry.getMatchCandidateCount();
         this.arrivals[nextEntryIndex++] = nextEntry.withFirstMatchIndex(nextMatchIndex);
         nextMatchIndex += matchCandidateCount;
      }

      this.attributeCount = nextMatchIndex;
   }


   public boolean isSolution(Configuration configuration)
   {
      return true;
   }


   @Override
   public WhoGoesWhereIterator getOperationIterator(Configuration configuration)
   {
      return new WhoGoesWhereIterator(configuration, this);
   }


   public abstract WhoGoesWhereFitness getDefaultFitness();


   /* required for fitness calculations */

   /* RandomConfigurationProblem interface */

   public Configuration getRandomConfiguration()
   {
      List<Integer> tmp = new ArrayList<Integer>(this.attributeCount);
      for (int i = 0; i < this.attributeCount; ++i)
         tmp.add(JcopRandom.nextInt() & Integer.MAX_VALUE);
      return new Configuration(tmp);
   }


   /* GlobalSearchProblem interface */

   public Integer getMaximum(int index)
   {
      return Integer.MAX_VALUE;
   }
}
