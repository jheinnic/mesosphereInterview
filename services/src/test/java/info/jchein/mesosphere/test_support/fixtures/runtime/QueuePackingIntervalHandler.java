package info.jchein.mesosphere.test_support.fixtures.runtime;

import java.util.Queue;

import org.apache.commons.math3.distribution.RealDistribution;

import info.jchein.mesosphere.elevator.runtime.AbstractDistributionBasedIntervalFunction;
import info.jchein.mesosphere.elevator.runtime.IIntervalHandler;

public class QueuePackingIntervalHandler
implements IIntervalHandler
{
   private Queue<String> queue;
   private String token;

   public QueuePackingIntervalHandler( Queue<String> queue, String token ) {
      this.queue = queue;
      this.token = token;
   }
   
   private int callCount = 0;

   public int getCallCount() { return this.callCount; }

   @Override
   public void call(long arg0)
   {
      this.queue.offer(this.token + arg0);
      this.callCount ++;
   }
}
