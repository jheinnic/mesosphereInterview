package info.jchein.mesosphere.elevator.runtime;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import rx.functions.Action0;

public final class PoissonProcessFunction implements IVariableIntervalFunction {
   private final ExponentialDistribution distribution;
   private final IIntervalHandler handler;

   public PoissonProcessFunction(ExponentialDistribution distribution, IIntervalHandler handler) {
      this.distribution = distribution;
      this.handler = handler;
   }

   @Override
   public final long apply(long interval)
   {
      this.handler.call(interval);
      return Math.round(
         this.distribution.sample());
   }
}
