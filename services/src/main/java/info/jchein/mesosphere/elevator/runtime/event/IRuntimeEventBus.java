package info.jchein.mesosphere.elevator.runtime.event;

public interface IRuntimeEventBus
{
   void post(Object event);
   
   void registerListener(Object listener);
   
   void unregisterListener(Object listener);
}
