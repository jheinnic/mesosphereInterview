package info.jchein.mesosphere.elevator;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.CarIndexContext;
import info.jchein.mesosphere.elevator.control.ElevatorCarScope;
import info.jchein.mesosphere.elevator.control.IElevatorCarScope;
import lombok.SneakyThrows;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class RideOn
{
   private static final CarIndexContext CAR_INDEX_CONTEXT = new CarIndexContext();
   private static final ElevatorCarScope ELEVATOR_CAR_SCOPE = new ElevatorCarScope(CAR_INDEX_CONTEXT);
   
   @SneakyThrows
    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(RideOn.class);
        app.setHeadless(true);
        app.setLogStartupInfo(true);
        app.setAdditionalProfiles("elevator.runtime.virtual", "workload.toy");
        
      app.setInitializers(
         ImmutableList.of(new ApplicationContextInitializer<ConfigurableApplicationContext>() {

         @Override
         public void initialize(ConfigurableApplicationContext applicationContext)
         {
            final CustomScopeConfigurer addElevatorCarScopePostProcessor = customScopeConfigurer();
            applicationContext.addBeanFactoryPostProcessor(addElevatorCarScopePostProcessor);
         }
        }));
      app.run(args);
    }
    
//    @Bean
//    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static CustomScopeConfigurer customScopeConfigurer()
    {
       CustomScopeConfigurer retVal = new CustomScopeConfigurer();
       retVal.addScope(IElevatorCarScope.SCOPE_NAME, elevatorCarScope());
       return retVal;
    }
    
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static ElevatorCarScope elevatorCarScope()
    {
       return ELEVATOR_CAR_SCOPE;
    }
    
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static CarIndexContext elevatorCarIndexContext()
    {
       return CAR_INDEX_CONTEXT;
    }
}
