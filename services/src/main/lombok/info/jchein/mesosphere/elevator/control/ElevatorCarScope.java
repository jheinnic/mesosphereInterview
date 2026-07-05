package info.jchein.mesosphere.elevator.control;


import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.google.common.collect.ImmutableMap;

import info.jchein.mesosphere.elevator.common.CarIndex;
import info.jchein.mesosphere.elevator.common.ICarContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ElevatorCarScope
implements Scope, IElevatorCarScope
{
   public static final int MAX_ELEVATORS = 100;

   private final AtomicReferenceArray<ImmutableMap<String, Object>> carScopeMaps =
      new AtomicReferenceArray<ImmutableMap<String, Object>>(MAX_ELEVATORS);
   private final ICarContext carIndexContext;


   public ElevatorCarScope( ICarContext carIndexContext )
   {
      this.carIndexContext = carIndexContext;
   }


   @Override
   public <R> R evalForCar(
      @Min(0) @Max(MAX_ELEVATORS - 1) int carIndex, Supplier<R> director)
   {
      final CarIndex previous = this.carIndexContext.swapCarIndex(carIndex);
      final R retVal = director.get();
      this.carIndexContext.apply(previous);
      return retVal;
   }


   @Override
   public Object get(String name, ObjectFactory<?> objectFactory)
   {
      final CarIndex carIndex = this.carIndexContext.get();
      if (carIndex == null) { return null; }

      final int carIdx = carIndex.getCarIndex();
      ImmutableMap<String, Object> scope = getScopeMap(carIndex);
      Object scopedObject = null;

      while (scopedObject == null) {
         scopedObject = scope.get(name);
         if (scopedObject == null) {
            synchronized (this.carScopeMaps) {
               ImmutableMap<String, Object> scope2 = this.carScopeMaps.get(carIdx);
               if (scope == scope2) {
                  scopedObject = objectFactory.getObject();
                  ImmutableMap<String, Object> newScope =
                     ImmutableMap.<String, Object> builder()
                        .putAll(scope)
                        .put(name, scopedObject)
                        .build();
                  this.carScopeMaps.set(carIdx, newScope);
               } else {
                  scope = scope2;
               }
            }
         }
      }

      return scopedObject;
   }


   public ImmutableMap<String, Object> getScopeMap(CarIndex carIndex)
   {
      final int carIdx = carIndex.getCarIndex();
      ImmutableMap<String, Object> scope = this.carScopeMaps.get(carIdx);
      while (scope == null) {
         scope =
            ImmutableMap.<String, Object> builder()
               .build();
         if (!this.carScopeMaps.compareAndSet(carIdx, null, scope)) {
            scope = this.carScopeMaps.get(carIdx);
         }
      }
      return scope;
   }


   @Override
   public Object remove(String name)
   {
      CarIndex carIndex = this.carIndexContext.get();
      if (carIndex == null) { return null; }

      final int carIdx = carIndex.getCarIndex();
      ImmutableMap<String, Object> scope = this.carScopeMaps.get(carIdx);
      Object retVal = scope.get(name);
      if (retVal != null) {
         synchronized (this.carScopeMaps) {
            scope = this.carScopeMaps.get(carIdx);
            retVal = scope.get(name);
            if (retVal != null) {
               final Iterable<Map.Entry<String, Object>> iter =
                  scope.entrySet()
                     .stream()
                     .filter(entry -> {
                        return !entry.getKey()
                           .equals(name);
                     })::iterator;
               scope =
                  ImmutableMap.<String, Object> builder()
                     .putAll(iter)
                     .build();
               this.carScopeMaps.set(carIdx, scope);
            }
         }
      }

      return retVal;
   }


   @Override
   public void registerDestructionCallback(String name, Runnable callback)
   {
      log.warn("ElevatorCarScope does not support destruction callbacks. ");
   }


   @Override
   public Object resolveContextualObject(String key)
   {
      return null;
   }


   @Override
   public String getConversationId()
   {
      CarIndex retVal = this.carIndexContext.get();
      if (retVal == null) {
         return null;
      }
      
      return retVal.toString();
   }

}
