package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.PoissonProcessFunction;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;
import lombok.SneakyThrows;


public abstract class AbstractPopulation<V extends IRandomVariables, T extends AbstractTraveller<V, T>, P extends AbstractPopulation<V, T, P>>
implements ISimulatedPopulation
{
   private final Supplier<PassengerId> idFactory;
   private final FSM<T> stateMachine;
   private final Supplier<V> randomGenerator;
   private final PoissonProcessFunction poissonProcess;

   private IRuntimeScheduler scheduler;
   private IRuntimeClock clock;
   private IRuntimeEventBus eventBus;


   protected AbstractPopulation( Supplier<PassengerId> idFactory, FSM<T> stateMachine,
      Supplier<V> randomGenerator, ExponentialDistribution arrivalRate )
   {
      this.idFactory = idFactory;
      this.stateMachine = stateMachine;
      this.randomGenerator = randomGenerator;
      this.poissonProcess = new PoissonProcessFunction(arrivalRate, this::injectNextArrival);
      this.scheduler = null;
      this.clock = null;
      this.eventBus = null;
   }


   public String getPopulationName()
   {
      return this.stateMachine.getName();
   }


   @Override
   public void
   initPoissonProcess(IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus)
   {
      this.scheduler = scheduler;
      this.clock = clock;
      this.eventBus = eventBus;

      this.scheduler.scheduleVariable(
         this.poissonProcess.sample(),
         TimeUnit.MILLISECONDS,
         Priorities.SIMULATE_ARRIVALS.getValue(),
         this.poissonProcess);
   }


   @SneakyThrows
   void injectNextArrival(long interval)
   {
      final PassengerId id = this.idFactory.get();
      final V randomVariables = this.randomGenerator.get();
      final T nextArrival = this.createTraveller(
         id, randomVariables, this.stateMachine, this.clock, this.scheduler, this.eventBus);

      this.stateMachine.onEvent(nextArrival, CommonEvents.ENTERED_SIMULATION.asEventName());
   }

   // TODO: Passenger ID cleanly
   abstract protected T createTraveller(PassengerId id, V randomVariables, FSM<T> stateMachine,
      IRuntimeClock clock, IRuntimeScheduler scheduler, IRuntimeEventBus eventBus);
}
