package info.jchein.mesosphere.elevator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class RideOn
{
    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(RideOn.class);
        app.setHeadless(true);
        app.setLogStartupInfo(true);
        app.setAdditionalProfiles("elevator.runtime.virtual", "workload.toy");
        // app.setWebEnvironment(true);
        // app.setBannerMode(Banner.Mode.OFF);

        app.run(args);
        
    }
}
