package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.PoissonProcessFunction;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;
import lombok.SneakyThrows;


public class SimulatedPopulation<V extends IRandomVariables, T extends AbstractTraveller<V, T>>
implements ISimulatedPopulation
{
   private final BiFunction<V, FSM<T>, T> travellerFactory;
   private final FSM<T> stateMachine;
   private final Supplier<V> randomGenerator;
   private final PoissonProcessFunction poissonProcess;


   public SimulatedPopulation( BiFunction<V, FSM<T>, T> travellerFactory, FSM<T> stateMachine,
      Supplier<V> randomGenerator, ExponentialDistribution arrivalRate )
   {
      this.travellerFactory = travellerFactory;
      this.stateMachine = stateMachine;
      this.randomGenerator = randomGenerator;
      this.poissonProcess = new PoissonProcessFunction(arrivalRate, this::injectNextArrival);
   }


   public String getPopulationName()
   {
      return this.stateMachine.getName();
   }


   @Override
   public void initPoissonProcess(IRuntimeScheduler scheduler)
   {
      scheduler.scheduleVariable(
         this.poissonProcess.sample(),
         TimeUnit.MILLISECONDS,
         Priorities.SIMULATE_ARRIVALS.getValue(),
         poissonProcess);
   }


   @SneakyThrows
   void injectNextArrival(long interval)
   {
      final V randomVariables = this.randomGenerator.get();
      final T nextArrival = this.travellerFactory.apply(randomVariables, this.stateMachine);

      this.stateMachine.onEvent(nextArrival, CommonEvents.ENTERED_SIMULATION.asEventName());
   }
}
