package test.jcop;

public interface IWhoGoesWhereProblem
{
    PassengerExchange[] getExchanges();
    PassengerEntry[] getArrivals();
    PassengerExit[] getDepartures();
    int getAttributeCount();
}

