package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.IValueFactory;
import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.PoissonProcessFunction;
import info.jchein.mesosphere.elevator.simulator.event.PopulationDefined;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;
import info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return.TravellerWithLobbyReturn;

import lombok.SneakyThrows;


public abstract class AbstractPopulation<V extends IRandomVariables, T extends AbstractTraveller<V>>
implements ISimulatedPopulation
{
   protected final IRuntimeScheduler scheduler;
   protected final IRuntimeClock clock;
   protected final IRuntimeEventBus eventBus;
   protected final FSM<T> stateMachine;
   protected final IValueFactory valueFactory;
   protected final Supplier<V> randomVariablesGenerator;
   private final ExponentialDistribution expoDist;


   protected AbstractPopulation( IRuntimeScheduler scheduler, IRuntimeClock clock,
      IRuntimeEventBus eventBus, IValueFactory valueFactory, Supplier<V> randomVariablesGenerator,
      ExponentialDistribution expoDist )
   {
      this.clock = clock;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
      this.valueFactory = valueFactory;
      this.randomVariablesGenerator = randomVariablesGenerator;
      this.expoDist = expoDist;
      this.stateMachine = this.constructStateMachine();
   }


   @Override
   public void initPoissonProcess()
   {
      final PoissonProcessFunction poissonProcess =
         new PoissonProcessFunction(this.expoDist, this::injectNextArrival);
      final long interval = Math.round(expoDist.sample() * 1000);
      this.scheduler.scheduleVariable(
         interval,
         TimeUnit.MILLISECONDS,
         Priorities.SIMULATE_ARRIVALS.getValue(),
         poissonProcess);
      this.eventBus.post(PopulationDefined.build(bldr -> {
         bldr.clockTime(this.clock.now())
            .populationName(this.stateMachine.getName());
      }));
   }


   @SneakyThrows
   public void injectNextArrival(long interval)
   {
      final PassengerId passengerId = this.valueFactory.getNextPassengerId();
      final V nextVariables = this.randomVariablesGenerator.get();
      final T nextArrival = this.allocateNextArrival(passengerId, nextVariables);
      this.stateMachine
         .onEvent(nextArrival, TravellerWithLobbyReturn.ENTERED_SIMULATION, nextVariables);
   }


   protected abstract T allocateNextArrival(PassengerId passengerId, V randomValues);

   protected abstract FSM<T> constructStateMachine();
}
