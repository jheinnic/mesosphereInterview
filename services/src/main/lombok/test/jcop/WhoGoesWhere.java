package test.jcop;


import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cvut.felk.cig.jcop.problem.BaseProblem;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.GlobalSearchProblem;
import cz.cvut.felk.cig.jcop.problem.RandomConfigurationProblem;
import cz.cvut.felk.cig.jcop.util.JcopRandom;
import lombok.Getter;


/**
 */
public abstract class WhoGoesWhere
extends BaseProblem
implements RandomConfigurationProblem, GlobalSearchProblem, IWhoGoesWhereProblem
{
   /**
    * Id of problem instance if supplied in {@link #WhoGoesWhere(String, Building)}.
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
   private int attributeCount;


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
         int floorRelativeIndex = 0;
         for (final BoardingEvent nextEvent : nextFloor.getBoardingEvents()) {
            final PassengerExchange nextExchange =
               new PassengerExchange(
                  exchangeCount++,
                  nextFloor,
                  nextEvent,
                  arrivalCount,
                  departureCount,
                  weightLoad);
            weightLoad = nextExchange.getExpectedOutgoingWeight();
            exchangesBuilder.add(nextExchange);

            final int passengersIn = nextEvent.getPassengersIn();
            final int passengersOut = nextEvent.getPassengersOut();
            passengerCount += passengersIn - passengersOut;
            arrivalCount += passengersIn;

            // Allocate and index passenger exit vertices. Each node receives two values from zero-based indices, one
            // for its position relative to all other passenger exit vertices. ther other for its position relative to
            // all other passenger exit vertices on the same floor.
            for (int ii = 0; ii < passengersOut; ii++) {
               final PassengerExit nextExit =
                  new PassengerExit(departureCount++, nextExchange, floorRelativeIndex++);
               exitsBuilder.add(nextExit);
            }

            // Assert running count of on-board passengers is always non-negative.
            Preconditions.checkState(passengerCount >= 0);
         }
      }

      if (passengerCount > 0) {
         // We use only a single exchange with no assigned floor index. That Exchange represents the set of passengers
         // who are still on board
         // the elevator car because they did not disembark at any floor stop since boarding.
         final PassengerExchange pseudoExchange =
            new PassengerExchange(
               exchangeCount++,
               passengerCount,
               arrivalCount,
               departureCount,
               weightLoad);

         for (int ii = 0; ii < passengerCount; ii++) {
            final PassengerExit nextExit = new PassengerExit(departureCount++, pseudoExchange, ii);
            exitsBuilder.add(nextExit);
         }
         exchangesBuilder.add(pseudoExchange);
      }

      // We must iterate through the Exchanges in reverse order in order to determine the number of candidate passenger
      // departure vertices to
      // consider for each available passenger arrival. We'll invert the ImmutableList we've been building this far,
      // then reverse pack it back
      // into the array we'll use at the end.
      // TODO: We can probably leave this in a forward-ordered ImmutableList--only the arrivals and departures need to
      // be kept as arrays for the
      // sake of adding them to the matching graph.
      ImmutableList<PassengerExchange> tempExchanges = exchangesBuilder.build();
      this.departures =
         exitsBuilder.build()
            .toArray(new PassengerExit[departureCount]);
      this.exchanges = tempExchanges.toArray(new PassengerExchange[exchangeCount]);

      // Count backwards to identify the number of potential Exit candidates for each Entry based by
      // adding the Exit counts for every run that begins with all entries from a single exchange.
      int matchCandidateCount = 0;
      int passengersOut = 0;
      int passengersIn = 0;
      int nextArrivalIndex = departureCount - 1;
      int lastFloorIndex = -1;
      int floorRelativeIndex = 0;
      for (final PassengerExchange prevExchange : tempExchanges.reverse()) {
         passengersIn += prevExchange.getPassengersIn();
         passengersOut += prevExchange.getPassengersOut();

         // Reset the floor relative index to 0 when the current exchange targets a differnet floor than the previous.
         // Yes, we are indexing vertices on a given floor in the opposite order that we're giving their absolute index,
         // but important thing is that numbers are unique.
         final FloorLanding floor = prevExchange.getFloor();
         if ((floor != null) && (floor.getFloorIndex() != lastFloorIndex)) {
            matchCandidateCount += passengersOut;
            passengerCount += passengersIn - passengersOut;
            floorRelativeIndex = 0;
         }

         for (int ii = 0; ii < passengersIn; ii++) {
            entriesBuilder.add(
               PassengerEntry.builder()
                  .index(nextArrivalIndex--)
                  .exchange(prevExchange)
                  .relativeIndex(floorRelativeIndex++)
                  .matchCandidateCount(matchCandidateCount)
                  .build());
         }

         if (passengerCount == 0) {
            matchCandidateCount = 0;
         }
      }

      this.arrivals =
         entriesBuilder.build()
            .toArray(new PassengerEntry[departureCount]);
      this.attributeCount = this.arrivals.length * 4;
      this.dimension = this.attributeCount;

      /*
       * // Calculate the match attribute index offsets int nextEntryIndex = 0; int nextMatchIndex =
       * this.firstMatchAttributeIndex = this.departures.length + this.departures.length - 1; for (final PassengerEntry
       * nextEntry : tempArrivals) { matchCandidateCount = nextEntry.getMatchCandidateCount();
       * this.arrivals[nextEntryIndex++] = nextEntry.withFirstMatchIndex(nextMatchIndex); nextMatchIndex +=
       * matchCandidateCount; }
       */
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
         tmp.add(JcopRandom.nextInt(Integer.MAX_VALUE));
      return new Configuration(tmp);
   }


   /* GlobalSearchProblem interface */

   public Integer getMaximum(int index)
   {
      return Integer.MAX_VALUE;
   }
}
