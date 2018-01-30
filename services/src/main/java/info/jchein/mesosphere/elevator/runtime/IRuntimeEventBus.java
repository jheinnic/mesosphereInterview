package info.jchein.mesosphere.elevator.runtime;

public interface IRuntimeEventBus
{
   void post(Object event);
   
   void registerListener(Object listener);
   
   void unregisterListener(Object listener);
}
