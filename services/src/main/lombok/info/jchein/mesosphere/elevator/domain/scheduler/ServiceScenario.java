package info.jchein.mesosphere.elevator.domain.scheduler;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class ServiceScenario
{
   @Singular
   private final ImmutableList<ElevatorCarPlan> carPlans;
}
