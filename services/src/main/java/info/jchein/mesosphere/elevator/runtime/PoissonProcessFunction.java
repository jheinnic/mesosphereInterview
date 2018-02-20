package info.jchein.mesosphere.elevator.runtime;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public final class PoissonProcessFunction implements IVariableIntervalFunction {
   private final ExponentialDistribution distribution;
   private final IIntervalHandler handler;

   /**
    * 
    * @param distribution A distribution for the next arrival interval in seconds
    * @param handler
    */
   public PoissonProcessFunction(ExponentialDistribution distribution, IIntervalHandler handler) {
      this.distribution = distribution;
      this.handler = handler;
   }

   @Override
   public long apply(long interval)
   {
      this.handler.call(interval);
      return this.sample();
   }
   
   /**
    * @return The next arrival interval sampled from {@code distribution} and converted to milliseconds.
    */
   public long sample() {
      return Math.round(
         this.distribution.sample() * 1000);
   }
}
