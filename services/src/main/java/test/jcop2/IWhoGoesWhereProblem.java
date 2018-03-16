package test.jcop2;

import com.google.common.collect.ImmutableList;

public interface IWhoGoesWhereProblem
{
    ImmutableList<PassengerExchange> getExchanges();
    int getTravellerCount();
    int getTripsCompleted();
    int getPassengersOnBoard();
    int getFirstLotteryIndex();
}

