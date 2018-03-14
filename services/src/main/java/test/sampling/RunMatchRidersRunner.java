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


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RunMatchRidersRunner
implements ApplicationRunner
{
   private final IWhoGoesWhereFactory problemFactory;


   @Autowired
   RunMatchRidersRunner( IWhoGoesWhereFactory problemFactory )
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
                     eventBuilder.passengersIn(2)
                        .weightChange(136.838);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(2)
                        .weightChange(138.958);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(3)
                        .weightChange(196.304);
                  });
            })
            .floorLanding((floorBuilder) -> {
               floorBuilder.floorIndex(1)
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(3)
                        .passengersOut(1)
                        .weightChange(117.703);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersOut(1)
                        .passengersIn(2)
                        .weightChange(78.061);
                  });
            })
            .floorLanding((floorBuilder) -> {
               floorBuilder.floorIndex(2)
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(2)
                        .passengersOut(2)
                        .weightChange(-11.965);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersOut(1)
                        .passengersIn(3)
                        .weightChange(136.57);
                  });
            })
            .floorLanding((floorBuilder) -> {
               floorBuilder.floorIndex(3)
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(2)
                        .passengersOut(1)
                        .weightChange(72.245);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(1)
                        .passengersOut(3)
                        .weightChange(-138.699);
                  });
            })
            .floorLanding((floorBuilder) -> {
               floorBuilder.floorIndex(4)
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(4)
                        .passengersOut(2)
                        .weightChange(154.704);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(1)
                        .passengersOut(3)
                        .weightChange(-129.183);
                  });
            })
            .floorLanding((floorBuilder) -> {
               floorBuilder.floorIndex(5)
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(1)
                        .passengersOut(2)
                        .weightChange(-74.733);
                  })
                  .boardingEvent((eventBuilder) -> {
                     eventBuilder.passengersIn(2)
                        .passengersOut(3)
                        .weightChange(-71.212);
                  });
            });
      });
      WhoGoesWhere problem = this.problemFactory.allocateProblem("Testing", problemData);
      Solver solver = new SimpleSolver(new GeneticAlgorithm(128, 0.075), problem);
      StopCondition timeStopCondition = new TimeoutCondition(5000);
      StopCondition iterStopCondition = new IterationCondition(150);
//      solver.addStopCondition(timeStopCondition);
      solver.addStopCondition(iterStopCondition);

      solver.run();

      Result result = solver.getResult();
      WhoGoesWhereFitness fitness = problem.getDefaultFitness();
      ResultEntry resultEntry =
         result.getResultEntries()
            .get(0);
      Configuration bestConfiguration = resultEntry.getBestConfiguration();
      CandidateSolution solution = fitness.transformConfiguration(bestConfiguration);
      System.out.println(solution.toString());
      System.out.println(
         String.format(
            "Solution in %d iterations over %d ms",
            resultEntry.getOptimizeCounter(),
            resultEntry.getStartTimestamp()
               .getClockTimeSpent(resultEntry.getStopTimestamp())));
   }
   // 63.2 + 64.3 + 72.1
   // -64.3 + 70.9 = 6.6
}
