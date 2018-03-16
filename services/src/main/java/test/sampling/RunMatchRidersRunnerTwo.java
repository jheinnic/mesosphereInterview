package test.sampling;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import cz.cvut.felk.cig.jcop.algorithm.geneticalgorithm.GeneticAlgorithm;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.result.Result;
import cz.cvut.felk.cig.jcop.result.ResultEntry;
import cz.cvut.felk.cig.jcop.solver.SimpleSolver;
import cz.cvut.felk.cig.jcop.solver.Solver;
import cz.cvut.felk.cig.jcop.solver.condition.IterationCondition;
import cz.cvut.felk.cig.jcop.solver.condition.TimeoutCondition;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import test.jcop2.Building;
import test.jcop2.CandidateSolution;
import test.jcop2.IWhoGoesWhereFactory;
import test.jcop2.WhoGoesWhere;
import test.jcop2.WhoGoesWhereFitness;


//@Component
//@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RunMatchRidersRunnerTwo
implements ApplicationRunner
{
   private final IWhoGoesWhereFactory problemFactory;

   @Autowired
   RunMatchRidersRunnerTwo( IWhoGoesWhereFactory problemFactory )
   {
      this.problemFactory = problemFactory;
   }

   @Override
   public void run(ApplicationArguments args) throws Exception
   {
      final Building problemData = Building.build((builder) -> {
         builder.currentDirection(DirectionOfTravel.GOING_UP)
         .floorLanding((floorBuilder) -> {
            floorBuilder.floorIndex(0)
            .boardingEvent((eventBuilder) -> {
               eventBuilder.passengersIn(2).weightChange(127.5);
            }).boardingEvent((eventBuilder) -> {
               eventBuilder.passengersIn(1).weightChange(72.1);
            });
         }).floorLanding((floorBuilder) -> {
            floorBuilder.floorIndex(2)
            .boardingEvent((eventBuilder) -> {
               eventBuilder.passengersIn(1).passengersOut(1).weightChange(6.6);
            }).boardingEvent((eventBuilder) -> {
               eventBuilder.passengersOut(1).weightChange(-63.2);
            });
         }).floorLanding((floorBuilder) -> {
            floorBuilder.floorIndex(5)
            .boardingEvent((eventBuilder) -> {
               eventBuilder.passengersOut(2).weightChange(-143.0);
            });
         });
      });

      final WhoGoesWhere problem = this.problemFactory.allocateProblem("Testing", problemData);
      final WhoGoesWhereFitness fitness = problem.getDefaultFitness();

      final Solver solver = new SimpleSolver(
         new GeneticAlgorithm(16, 0.025), problem);
      solver.addStopCondition(
         new TimeoutCondition(500));
      solver.addStopCondition(
         new IterationCondition(2000));
      solver.run();
      
      final Result result = solver.getResult();
      final ResultEntry resultEntry = result.getResultEntries().get(0);
      final Configuration bestConfiguration = resultEntry.getBestConfiguration();
      final CandidateSolution solution = fitness.transformConfiguration(bestConfiguration);

      System.out.println(solution.toString());
      System.out.println(
         String.format("Solution in %d iterations over %d ms",
            resultEntry.getOptimizeCounter(),
            resultEntry.getStartTimestamp()
               .getClockTimeSpent(
                  resultEntry.getStopTimestamp())));
   }
   // 63.2 + 64.3 + 72.1
   // -64.3 + 70.9 = 6.6 
}
