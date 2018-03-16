package test.jcop2;

import java.io.IOException;

import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.result.Result;
import cz.cvut.felk.cig.jcop.result.ResultEntry;
import cz.cvut.felk.cig.jcop.result.render.Render;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WhoGoesWhereRender implements Render
{
   private final WhoGoesWhereFitness fitness;

   public WhoGoesWhereRender( final WhoGoesWhereFitness fitness )
   {
      this.fitness = fitness;
   }

   @Override
   public void render(Result result) throws IOException
   {
      final ResultEntry resultEntry = result.getResultEntries().get(0);
      final Configuration bestConfiguration = resultEntry.getBestConfiguration();
      final CandidateSolution solution = fitness.transformConfiguration(bestConfiguration);

      log.info(
         "Who went where problem solved in {} iterations over {} milliseconds\n** Solution follows:\n{}",
         resultEntry.getOptimizeCounter(),
         resultEntry.getStartTimestamp()
            .getClockTimeSpent(
               resultEntry.getStopTimestamp()),
         solution);
   }

}
