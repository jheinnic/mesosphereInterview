package info.jchein.mesosphere.elevator.runtime.event;

public interface IRuntimeEventBusLocator
{
   IRuntimeEventBus locate(String beanName);
}
