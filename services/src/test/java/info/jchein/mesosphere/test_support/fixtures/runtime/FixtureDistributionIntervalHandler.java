package info.jchein.mesosphere.test_support.fixtures.runtime;

import org.apache.commons.math3.distribution.RealDistribution;

import info.jchein.mesosphere.elevator.runtime.PoissonProcessFunction;

public class FixtureDistributionIntervalHandler
extends PoissonProcessFunction
{
   public FixtureDistributionIntervalHandler( RealDistribution distribution )
   {
      super(distribution);
   }
   
   private int callCount = 0;

   @Override
   public void call()
   {
      this.callCount += 1;
   }
   
   public int getCallCount() { return this.callCount; }

}
