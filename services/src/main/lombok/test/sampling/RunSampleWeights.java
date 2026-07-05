package test.sampling;


import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({ RunSampleWeightsConfiguration.class })
public class RunSampleWeights
{
   public static void main(final String[] args)
   {
      final SpringApplication app = new SpringApplication(RunSampleWeights.class);
      app.setHeadless(true);
      app.setLogStartupInfo(false);
      app.setBannerMode(Mode.OFF);
      app.setAdditionalProfiles("elevator.runtime.virtual", "workload.toy");

      app.run(args);
   }
}
