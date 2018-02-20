package info.jchein.mesosphere.elevator.common;


import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
//@ComponentScan({ "info.jchein.mesosphere.elevator.common" })
public class IdentityConfiguration
{
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   PassengerIdFactory idFactory() {
      return new PassengerIdFactory();
   }
}
