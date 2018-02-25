package info.jchein.mesosphere.elevator.control;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.statefulj.framework.core.model.Factory;

@Component(IElevatorCar.FACTORY_BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ElevatorCarFactory<T, CT> implements Factory<T, CT>, BeanFactoryAware {

   private BeanFactory beanFactory;

   @Override
   public T create(Class<T> clazz, String event, CT context) {
      return this.beanFactory.getBean(clazz);
   }

   @Override
   public void setBeanFactory(BeanFactory beanFactory) throws BeansException
   {
      this.beanFactory = beanFactory;
   }
}
