package test.jcop2;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoreComponent
{
   double partialScore;
   double actualWeightChange;
   double expectedWeightChange;
   
   static ScoreComponent build(Consumer<ScoreComponentBuilder> director) {
      final ScoreComponentBuilder factoryBuilder = ScoreComponent.builder();
      director.accept(factoryBuilder);
      return factoryBuilder.build();
   }
}
