package info.jchein.mesosphere.elevator.control;

import info.jchein.mesosphere.elevator.control.sdk.IDispatchStrategy;

public interface IDispatchStrategyLocator
{
   IDispatchStrategy locateStrategy();
}
