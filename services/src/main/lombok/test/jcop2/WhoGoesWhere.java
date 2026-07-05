package test.jcop2;


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
implements RandomConfigurationProblem, IWhoGoesWhereProblem, GlobalSearchProblem
{
   /**
    * Id of problem instance if supplied in {@link #WhoGoesWhere(String, Building)}.
    */
   @Getter
   private final ImmutableList<PassengerExchange> exchanges;
   @Getter
   private final int[] departureLottery;
   @Getter
   private final int travellerCount;
   @Getter
   private final int passengersOnBoard;
   @Getter
   private int tripsCompleted;
   @Getter
   private int firstLotteryIndex;


   WhoGoesWhere( String id, Building building )
   {
      this.label = id;
      ImmutableList.Builder<PassengerExchange> exchangesBuilder =
         ImmutableList.<PassengerExchange> builder();

      int exchangeCount = 0;
      int passengersOnBoard = 0;
      int travellerCount = 0;

      for (final FloorLanding currentFloor : building.getFloorLandings()) {
         int floorRelativeIndex = 0;

         for (final BoardingEvent nextEvent : currentFloor.getBoardingEvents()) {
            final int passengersIn = nextEvent.getPassengersIn();

            // Assert running count of on-board passengers is always non-negative.
            passengersOnBoard -= nextEvent.getPassengersOut();
            Preconditions.checkState(passengersOnBoard >= 0);
            passengersOnBoard += passengersIn;

            final PassengerExchange nextExchange =
               new PassengerExchange(exchangeCount++, currentFloor, floorRelativeIndex, nextEvent, travellerCount);

            floorRelativeIndex += passengersIn;
            travellerCount += passengersIn;

            exchangesBuilder.add(nextExchange);
         }
      }

      this.exchanges = exchangesBuilder.build();
      this.departureLottery = new int[travellerCount];
      this.travellerCount = travellerCount;
      this.tripsCompleted = travellerCount - passengersOnBoard;
      this.passengersOnBoard = passengersOnBoard;
      this.firstLotteryIndex = travellerCount + 1;

      passengersOnBoard = 0;
      travellerCount = 0;
      for (final PassengerExchange nextExchange : this.exchanges) {
         final int passengersOut = nextExchange.getPassengersOut();
         for (int ii = 0; ii < passengersOut; ii++) {
            this.departureLottery[travellerCount++] = passengersOnBoard--;
         }
         passengersOnBoard += nextExchange.getPassengersIn();
      }

      // Two attributes are used to seed the weight randomizer
      // travellerCount - 1 attributes are used to shuffle assignment of weights to arrivals
      // tripsCompleted attributes are used to shuffle assignment of arrivals to departures 
      this.dimension = this.travellerCount + this.tripsCompleted + 1;
   }


   public boolean isSolution(Configuration configuration)
   {
      if ((configuration.valueAt(0) < 0) || (configuration.valueAt(1) < 0)) {
         return false;
      }

      for (int ii=2; ii<this.firstLotteryIndex; ii++) {
         if (configuration.valueAt(ii) > (this.firstLotteryIndex - ii)) {
            return false;
         }
      }
      
      for (int ii=0; ii < this.tripsCompleted; ii++ ) {
         if (configuration.valueAt(ii + this.firstLotteryIndex) >= this.departureLottery[ii]) {
            return false;
         }
      }

      return true;
   }


   @Override
   public WhoGoesWhereIterator getOperationIterator(Configuration configuration)
   {
      return new WhoGoesWhereIterator(configuration, this);
   }


   public abstract WhoGoesWhereFitness getDefaultFitness();

   /* RandomConfigurationProblem interface */

   public Configuration getRandomConfiguration()
   {
      List<Integer> tmp = new ArrayList<Integer>(this.dimension);
      tmp.add(JcopRandom.nextInt(Integer.MAX_VALUE));
      tmp.add(JcopRandom.nextInt(Integer.MAX_VALUE));

      for (int ii = 2; ii < this.firstLotteryIndex; ii++)
         tmp.add(JcopRandom.nextInt(this.firstLotteryIndex - ii));

      for (int ii = 0; ii < this.tripsCompleted; ii++)
         tmp.add(JcopRandom.nextInt(this.departureLottery[ii]));
      
      // JcopRandom.setSeed(System.currentTimeMillis());

      return new Configuration(tmp);
   }

   /* GlobalSearchProblem interface */

   public Integer getMaximum(int index)
   {
      return (index >= this.firstLotteryIndex)
         ? this.departureLottery[index - this.firstLotteryIndex]
         : (index < 2)
            ? Integer.MAX_VALUE
            : this.firstLotteryIndex - index;
   }
}
