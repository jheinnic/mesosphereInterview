package info.jchein.mesosphere.elevator.control.manifest;


import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.model.Action;
import org.statefulj.fsm.model.State;
import org.statefulj.fsm.model.Transition;
import org.statefulj.fsm.model.impl.StateActionPairImpl;
import org.statefulj.fsm.model.impl.StateImpl;
import org.statefulj.persistence.memory.MemoryPersisterImpl;

import com.google.common.collect.ImmutableList;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TravelGraphFactory
extends AbstractFactoryBean<FSM<FloorOfOrigin>>
{
   private final StateImpl<FloorOfOrigin> beforePickup =
      new StateImpl<>(FloorOfOriginStates.Names.BEFORE_PICKUP);
   private final StateImpl<FloorOfOrigin> duringPickup =
      new StateImpl<>(FloorOfOriginStates.Names.DURING_PICKUP);
   private final StateImpl<FloorOfOrigin> makingStops =
      new StateImpl<>(FloorOfOriginStates.Names.MAKING_STOPS);
   private final StateImpl<FloorOfOrigin> finished =
      new StateImpl<>(FloorOfOriginStates.Names.FINISHED);

   @Override
   protected FSM<FloorOfOrigin> createInstance() throws Exception
   {
      Action<FloorOfOrigin> onDropRequested =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Integer floorIndex = (Integer) args[0];
            stateful.trackDropRequest(floorIndex);
         };
      Action<FloorOfOrigin> onDoorsOpenedForPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Long clockTime = (Long) args[1];
            stateful.trackBeginPickup(clockTime);
         };
      Action<FloorOfOrigin> onDoorsOpenedForVisit =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Long clockTime = (Long) args[1];
            stateful.trackBeginVisit(clockTime);
         };
      Action<FloorOfOrigin> onOwnWeightIncreased =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Double weightIncrease = (Double) args[0];
            stateful.trackOwnBoardingWeight(weightIncrease);
         };
      Action<FloorOfOrigin> onPeerWeightIncreased =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Double weightIncrease = (Double) args[0];
            stateful.trackPeerBoardingWeight(weightIncrease);
         };
      Action<FloorOfOrigin> onWeightDecreased =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            Double weightDecrease = (Double) args[0];
            stateful.trackDisembarkingWeight(weightDecrease);
         };
      Action<FloorOfOrigin> onDoorsClosedForPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            stateful.trackCompletePickup();
         };
      Action<FloorOfOrigin> onDoorsClosedForVisit =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            stateful.trackCompleteVisit();
         };

      Transition<FloorOfOrigin> onDoorsOpenedBeforePickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            int floorIndex = ((Integer) args[0]).intValue();

            if (floorIndex == stateful.getFloorIndex()) {
               return new StateActionPairImpl<FloorOfOrigin>(this.duringPickup, onDoorsOpenedForPickup);
            }

            return new StateActionPairImpl<FloorOfOrigin>(this.beforePickup, null);
         };
      Transition<FloorOfOrigin> onDoorsClosedDuringPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            if (stateful.getOwnWeightAtPickup() == 0) {
               return new StateActionPairImpl<FloorOfOrigin>(this.finished, null);
            }
            
            return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, onDoorsClosedForPickup);
         };


      Transition<FloorOfOrigin> onDoorsOpenedAfterPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            if (stateful.isPotentialStop()) {
               return new StateActionPairImpl<FloorOfOrigin>( this.makingStops, onDoorsOpenedForVisit);
            }

            return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, null);
         };
      Transition<FloorOfOrigin> onDoorsClosedAfterPickup =
         (FloorOfOrigin stateful, String event, Object... args) -> {
            if (stateful.isPotentialStop()) {
               if (stateful.isLastStop() || (stateful.getOwnWeightRemaining() == 0)) {
                  return new StateActionPairImpl<FloorOfOrigin>(this.finished, onDoorsClosedForVisit);
               }

               return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, onDoorsClosedForVisit);
            }

            return new StateActionPairImpl<FloorOfOrigin>(this.makingStops, null);
         };

      this.beforePickup.addTransition(FloorOfOriginEvents.Names.DOORS_OPENED, onDoorsOpenedBeforePickup);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.beforePickup, onPeerWeightIncreased);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.beforePickup, onWeightDecreased);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, this.beforePickup, null);
      this.beforePickup.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.beforePickup, onDropRequested);

      this.duringPickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.duringPickup, onOwnWeightIncreased);
      this.duringPickup.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.duringPickup, onWeightDecreased);
      this.duringPickup.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, onDoorsClosedDuringPickup);
      this.duringPickup.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.duringPickup, onDropRequested);

      this.makingStops.addTransition(FloorOfOriginEvents.Names.DOORS_OPENED, onDoorsOpenedAfterPickup);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.makingStops, onPeerWeightIncreased);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.makingStops, onWeightDecreased);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.makingStops, null);
      this.makingStops.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, onDoorsClosedAfterPickup);
      
      this.finished.addTransition(FloorOfOriginEvents.Names.DOORS_OPENED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.WEIGHT_INCREASED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.WEIGHT_DECREASED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.DOORS_CLOSED, this.finished, null);
      this.finished.addTransition(FloorOfOriginEvents.Names.DROP_REQUESTED, this.finished, null);
      
      ImmutableList.Builder<State<FloorOfOrigin>> listBuilder = ImmutableList.<State<FloorOfOrigin>>builder();
      listBuilder.add(this.beforePickup)
         .add(this.duringPickup)
         .add(this.makingStops)
         .add(this.finished);
      MemoryPersisterImpl<FloorOfOrigin> persister = new MemoryPersisterImpl<FloorOfOrigin>(listBuilder.build(), this.beforePickup);
      return new FSM<FloorOfOrigin>("FloorOfOrigin", persister);
   }


   @Override
   public Class<FSM> getObjectType()
   {
      return FSM.class;
   }
}
