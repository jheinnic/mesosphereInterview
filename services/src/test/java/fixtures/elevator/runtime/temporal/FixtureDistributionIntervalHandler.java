package fixtures.elevator.runtime.temporal;

import info.jchein.mesosphere.elevator.runtime.IIntervalHandler;

public class FixtureDistributionIntervalHandler implements IIntervalHandler
{
   public FixtureDistributionIntervalHandler( )
   {
   }
   
   private int callCount = 0;

   @Override
   public void call(long interval)
   {
      this.callCount += 1;
   }
   
   public int getCallCount() { return this.callCount; }

}
