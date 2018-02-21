package info.jchein.mesosphere.elevator.control.model;

import info.jchein.mesosphere.elevator.control.sdk.IDispatchStrategy;

public interface IDispatchStrategyLocator
{
   IDispatchStrategy locateStrategy();
}
