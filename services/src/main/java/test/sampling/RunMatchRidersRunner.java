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
import test.jcop2.Building;
import test.jcop2.CandidateSolution;
import test.jcop2.IWhoGoesWhereFactory;
import test.jcop2.SufficientFitnessCondition;
import test.jcop2.WhoGoesWhere;
import test.jcop2.WhoGoesWhereFitness;
import test.jcop2.WhoGoesWhereRender;


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
         builder.floorLanding((floorBuilder) -> {
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

      final WhoGoesWhere problem = this.problemFactory.allocateProblem("Testing", problemData);
      final WhoGoesWhereFitness fitness = problem.getDefaultFitness();
      final WhoGoesWhereRender render = new WhoGoesWhereRender(fitness);
      final Solver solver = new SimpleSolver(new GeneticAlgorithm(20, 0.15), problem);

      solver.addStopCondition(
         new TimeoutCondition(5000));
//      solver.addStopCondition(
//         new IterationCondition(50));
      solver.addStopCondition(
         new SufficientFitnessCondition(fitness, 0.9));
      solver.addRender(render);

      solver.run();
      solver.render();
   }
}
