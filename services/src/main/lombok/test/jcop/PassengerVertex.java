package test.jcop;

public interface PassengerVertex
{
   int getIndex();
   int getRelativeIndex();
   PassengerExchange getExchange();

   boolean isEntry();
   boolean isExit();

   String getLabel();
}
