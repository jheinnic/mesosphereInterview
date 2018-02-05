package info.jchein.mesosphere.elevator.runtime;

import org.apache.commons.math3.distribution.RealDistribution;

public class FixtureDistributionIntervalHandler
extends AbstractDistributionBasedIntervalFunction
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
