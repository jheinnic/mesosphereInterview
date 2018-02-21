package info.jchein.mesosphere.elevator.control.model;


import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;


@Slf4j
//@Component
//@org.springframework.context.annotation.Scope(BeanDefinition.SCOPE_SINGLETON)
public class ElevatorCarScope
implements Scope, IElevatorCarScope
{
   public static final int MAX_ELEVATORS = 100;

   private ThreadLocal<Integer> threadCarContext = new ThreadLocal<Integer>();

   private AtomicReferenceArray<ImmutableMap<String, Object>> carScopeMaps =
      new AtomicReferenceArray<ImmutableMap<String, Object>>(MAX_ELEVATORS);


   @Override
   public <Locator> void evalForCar(int carIndex, Locator locator, Consumer<Locator> director)
   {
      final Integer previous = this.threadCarContext.get();
      this.threadCarContext.set(carIndex);
      director.accept(locator);
      this.threadCarContext.set(previous);
   }


   @Override
   public Object get(String name, ObjectFactory<?> objectFactory)
   {
      Integer carIndex = this.threadCarContext.get();
      if (carIndex == null) { return null; }

      ImmutableMap<String, Object> scope = getScopeMap(carIndex);
      Object scopedObject = null;

      while (scopedObject == null) {
         scopedObject = scope.get(name);
         if (scopedObject == null) {
            synchronized (this.carScopeMaps) {
               ImmutableMap<String, Object> scope2 = this.carScopeMaps.get(carIndex);
               if (scope == scope2) {
                  scopedObject = objectFactory.getObject();
                  ImmutableMap<String, Object> newScope =
                     ImmutableMap.<String, Object> builder()
                        .putAll(scope)
                        .put(name, scopedObject)
                        .build();
                  this.carScopeMaps.set(carIndex, newScope);
               } else {
                  scope = scope2;
               }
            }
         }
      }

      return scopedObject;
   }


   public ImmutableMap<String, Object> getScopeMap(Integer carIndex)
   {
      ImmutableMap<String, Object> scope = this.carScopeMaps.get(carIndex);
      while (scope == null) {
         scope =
            ImmutableMap.<String, Object> builder()
               .build();
         if (!this.carScopeMaps.compareAndSet(carIndex, null, scope)) {
            scope = this.carScopeMaps.get(carIndex);
         }
      }
      return scope;
   }


   @Override
   public Object remove(String name)
   {
      Integer carIndex = this.threadCarContext.get();
      if (carIndex == null) { return null; }

      ImmutableMap<String, Object> scope = this.carScopeMaps.get(carIndex);
      Object retVal = scope.get(name);
      if (retVal != null) {
         synchronized (this.carScopeMaps) {
            scope = this.carScopeMaps.get(carIndex);
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
               this.carScopeMaps.set(carIndex, scope);
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
      Integer retVal = this.threadCarContext.get();
      if (retVal == null) {
         return null;
      }
      
      return retVal.toString();
   }

}
