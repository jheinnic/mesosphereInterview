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
import cz.cvut.felk.cig.jcop.solver.condition.StopCondition;
import cz.cvut.felk.cig.jcop.solver.condition.TimeoutCondition;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import test.jcop.Building;
import test.jcop.CandidateSolution;
import test.jcop.IWhoGoesWhereFactory;
import test.jcop.WhoGoesWhere;
import test.jcop.WhoGoesWhereFitness;


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
      Building problemData = Building.build((builder) -> {
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
      WhoGoesWhere problem = this.problemFactory.allocateProblem("Testing", problemData);
      Solver solver = new SimpleSolver(
         new GeneticAlgorithm(16, 0.025), problem);
      StopCondition timeStopCondition = new TimeoutCondition(500);
      StopCondition iterStopCondition = new IterationCondition(2000);
      solver.addStopCondition(timeStopCondition);
      solver.addStopCondition(iterStopCondition);
      
      solver.run();
      
      Result result = solver.getResult();
      WhoGoesWhereFitness fitness = problem.getDefaultFitness();
      ResultEntry resultEntry = result.getResultEntries().get(0);
      Configuration bestConfiguration = resultEntry.getBestConfiguration();
      CandidateSolution solution = fitness.transformConfiguration(bestConfiguration);
      System.out.println(solution.toString());
      System.out.println(String.format("Solution in %d iterations over %d ms", resultEntry.getOptimizeCounter(), resultEntry.getStartTimestamp().getClockTimeSpent(resultEntry.getStopTimestamp())));
   }
   // 63.2 + 64.3 + 72.1
   // -64.3 + 70.9 = 6.6 
}
