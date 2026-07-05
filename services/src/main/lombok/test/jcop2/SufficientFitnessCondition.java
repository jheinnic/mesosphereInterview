package test.jcop2;

import cz.cvut.felk.cig.jcop.solver.condition.BaseCondition;
import cz.cvut.felk.cig.jcop.solver.message.Message;
import cz.cvut.felk.cig.jcop.solver.message.MessageBetterConfigurationFound;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SufficientFitnessCondition extends BaseCondition
{
   private final WhoGoesWhereFitness fitness;
   private final double fitnessGoal;
   private boolean conditionMet;


   public SufficientFitnessCondition(WhoGoesWhereFitness fitness, double fitnessGoal) {
      this.fitness = fitness;
      this.fitnessGoal = fitnessGoal;
      this.conditionMet = false;
   }

   @Override
   public void onMessage(Message message) {
      if (message instanceof MessageBetterConfigurationFound) {
         final MessageBetterConfigurationFound betterMessage =
            (MessageBetterConfigurationFound) message;
         final CandidateSolution solution =
            fitness.transformConfiguration(
               betterMessage.getConfiguration());
         final double nextFitness = betterMessage.getFitness();
         this.conditionMet = this.conditionMet || (nextFitness >= this.fitnessGoal);

         if (this.conditionMet == false) {
            log.info("Best case fitness has improved, but not enough to halt search: {}", solution.toString());
         } else {
            log.info("At {}, fitness goal has been met!", nextFitness);
         }
      }
   }


   @Override
   public boolean isConditionMet()
   {
      return this.conditionMet;
   }

}
