package test.jcop;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoreComponent
{
   double partialScore;
   double actualWeightChange;
   double expectedWeightChange;
}
