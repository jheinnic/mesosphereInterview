package info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return;


import java.util.function.Supplier;

import org.apache.commons.math3.distribution.RealDistribution;

import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;


public class WithLobbyReturnRandomVariableSupplier
implements Supplier<VariablesWithLobbyReturn>
{
   private IPopulationSampler weightSampler;
   private int activityFloorIndex;
   private RealDistribution activityDurationDist;


   public WithLobbyReturnRandomVariableSupplier( IPopulationSampler weightSampler,
      int activityFloorIndex, RealDistribution activityDurationDist )
   {
      this.weightSampler = weightSampler;
      this.activityFloorIndex = activityFloorIndex;
      this.activityDurationDist = activityDurationDist;
   }


   @Override
   public VariablesWithLobbyReturn get()
   {
      return VariablesWithLobbyReturn.build(bldr -> {
         bldr.activitySeconds(activityDurationDist.sample())
         .activityFloor(this.activityFloorIndex)
         .weight(this.weightSampler.sample());
      });
   }
}
