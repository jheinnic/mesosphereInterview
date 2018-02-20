package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.Persister;
import org.statefulj.fsm.model.Action;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.StateActionPair;
import org.statefulj.fsm.model.impl.CompositeActionImpl;
import org.statefulj.fsm.model.impl.StateActionPairImpl;
import org.statefulj.fsm.model.impl.StateImpl;
import org.statefulj.persistence.memory.MemoryPersisterImpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonEvents.CommonEventNames;
import info.jchein.mesosphere.elevator.simulator.passengers.CommonStates.CommonStateNames;


public abstract class AbstractPopulationFactory<V extends IRandomVariables, T extends AbstractTraveller<V, T>, P extends AbstractPopulation<V, T, P>>
extends AbstractFactoryBean<P>
//implements BeanFactoryAware
{
   private final State<T> beforeArrival;
   private final State<T> ridingElevator;

   protected final State<T> queuedForPickup;
   protected final State<T> afterDeparture;
   protected final Builder<State<T>> listBuilder = ImmutableList.<State<T>> builder();

   private final Action<T> enterSimulationAction = (T stateful, String event, Object... args) -> {
      stateful.onEnteredSimulation();
   };
   protected final Action<T> queueForPickupAction = (T stateful, String event, Object... args) -> {
      stateful.onQueuedForPickup();
   };
   private final Action<T> boardElevatorAction = (T stateful, String event, Object... args) -> {
      stateful.onBoardedElevator((int) args[0]);
   };
   protected final Action<T> disembarkElevatorAction = (T stateful, String event, Object... args) -> {
      stateful.onDisembarkedElevator();
   };
   protected final Action<T> exitSimulationAction = (T stateful, String event, Object... args) -> {
      stateful.onExitedSimulation();
   };

   private final String populationName;
   private final Supplier<V> randomGenerator;
   private final ExponentialDistribution arrivalRate;
   private Persister<T> persister = null;
//   private BeanFactory beanFactory;
   private final Supplier<PassengerId> idFactory;


   protected AbstractPopulationFactory( String populationName, Supplier<PassengerId> idFactory, 
      Supplier<V> randomGenerator, ExponentialDistribution arrivalRate )
   {
      this.populationName = populationName;
      this.idFactory = idFactory;
      this.randomGenerator = randomGenerator;
      this.arrivalRate = arrivalRate;

      this.beforeArrival = new StateImpl<T>(CommonStateNames.BEFORE_ARRIVAL);
      this.queuedForPickup = new StateImpl<T>(CommonStateNames.QUEUED_FOR_PICKUP);
      this.ridingElevator = new StateImpl<T>(CommonStateNames.RIDING_ELEVATOR);
      this.afterDeparture = new StateImpl<T>(CommonStateNames.AFTER_DEPARTURE, true);

      this.beforeArrival.addTransition(
         CommonEventNames.ENTERED_SIMULATION,
         (T stateful, String event, Object... args) -> {
            final State<T> nextState = stateful.getInitialState();
            final StateActionPair<T> retVal;
            if ((nextState != null) && (nextState != this.queuedForPickup)) {
               retVal = new StateActionPairImpl<T>(nextState, this.enterSimulationAction);
            } else {
               retVal =
                  new StateActionPairImpl<T>(
                     this.queuedForPickup,
                     new CompositeActionImpl<T>(
                        ImmutableList.of(this.enterSimulationAction, this.queueForPickupAction)));
            }

            return retVal;
         });

      this.queuedForPickup.addTransition(
         CommonEventNames.BOARDED_ELEVATOR,
         this.ridingElevator,
         this.boardElevatorAction);

      this.ridingElevator.addTransition(
         CommonEventNames.DISEMBARKED_ELEVATOR,
         (T stateful, String event, Object... args) -> {
            final State<T> nextState = stateful.getDestinationState();
            final StateActionPair<T> retVal;
            if ((nextState != null) && (nextState != this.afterDeparture)) {
               retVal = new StateActionPairImpl<T>(nextState, this.disembarkElevatorAction);
            } else {
               retVal =
                  new StateActionPairImpl<T>(
                     this.afterDeparture,
                     new CompositeActionImpl<T>(
                        ImmutableList.of(this.disembarkElevatorAction, this.exitSimulationAction)));
            }

            return retVal;
         });

      this.listBuilder.add(this.beforeArrival)
         .add(this.queuedForPickup)
         .add(this.ridingElevator)
         .add(this.afterDeparture);
   }


   @PostConstruct
   void constructFsm()
   {
      if (this.persister != null) { throw new IllegalStateException(
         "The post-creation constructFsm() method may only be called one time."); }
      this.appendAdditionalStates();
      this.persister = new MemoryPersisterImpl<>(this.listBuilder.build(), this.beforeArrival);
   }


//   public void setBeanFactory(BeanFactory beanFactory) throws BeansException
//   {
//      super.setBeanFactory(beanFactory);
//      this.beanFactory = beanFactory;
//   }


   @Override
   public P createInstance()
   {
      final FSM<T> stateMachine = new FSM<T>(this.populationName, this.persister);

      return this.createPopulation(this.idFactory, stateMachine, this.randomGenerator, this.arrivalRate);
   }


   @Override
   public abstract Class<P> getObjectType();


   protected abstract void appendAdditionalStates();


   protected abstract P createPopulation(Supplier<PassengerId> idFactory,
      FSM<T> stateMachine, Supplier<V> variablesSupplier, ExponentialDistribution arrivalRate);
}
