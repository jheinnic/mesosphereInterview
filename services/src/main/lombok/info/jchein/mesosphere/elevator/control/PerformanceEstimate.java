package info.jchein.mesosphere.elevator.control;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PerformanceEstimate
{
   ImmutableList<TripAssessment> tripAssessments;
   
   ImmutableList<SegmentWeightCost> weightCosts;
}
