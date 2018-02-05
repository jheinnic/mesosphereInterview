package info.jchein.mesosphere.elevator.runtime.event;

public interface IEventBus
{
   void post(Object event);
   
   void registerListener(Object listener);
   
   void unregisterListener(Object listener);
}
