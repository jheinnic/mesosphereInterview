package info.jchein.mesosphere.elevator.runtime;

import java.util.function.Function;

import org.apache.commons.math3.distribution.RealDistribution;

import rx.functions.Action0;

public abstract class AbstractDistributionBasedIntervalFunction implements IVariableIntervalFunction, Action0 {
   private final RealDistribution distribution;

   protected AbstractDistributionBasedIntervalFunction(RealDistribution distribution) {
      this.distribution = distribution;
   }

   @Override
   public Long apply(Long t)
   {
      this.call();
      return Math.round(
         this.distribution.sample());
   }
}
