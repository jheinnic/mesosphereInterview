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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.application.RideOnConfiguration;
import info.jchein.mesosphere.elevator.application.RideOnRunner;
import info.jchein.mesosphere.elevator.control.model.ElevatorCarScope;
import info.jchein.mesosphere.elevator.control.model.IElevatorCarScope;
import lombok.SneakyThrows;

@SpringBootApplication
@EnableAutoConfiguration
@Import(RideOnConfiguration.class)
@ComponentScan
public class RideOn
{
   private static final ElevatorCarScope ELEVATOR_CAR_SCOPE = new ElevatorCarScope();
   
   @SneakyThrows
    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(RideOn.class);
        app.setHeadless(true);
        app.setLogStartupInfo(true);
        app.setAdditionalProfiles("elevator.runtime.virtual", "workload.toy");
        // app.setWebEnvironment(true);
        // app.setBannerMode(Banner.Mode.OFF);
        
      app.setInitializers(
         ImmutableList.of(new ApplicationContextInitializer<ConfigurableApplicationContext>() {

         @Override
         public void initialize(ConfigurableApplicationContext applicationContext)
         {
            final CustomScopeConfigurer addElevatorCarScopePostProcessor = customScopeConfigurer();
            applicationContext.addBeanFactoryPostProcessor(addElevatorCarScopePostProcessor);
         }
        }));
        ConfigurableApplicationContext context = app.run(args);
        RideOnRunner runner = context.getBean(RideOnRunner.class);
        runner.run(null);
    }
    
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
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
}
