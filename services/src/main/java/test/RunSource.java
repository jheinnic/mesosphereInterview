package test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ RunSourceConfiguration.class })
public class RunSource {
	public static void main(final String[] args) {
		final SpringApplication app = new SpringApplication(RunSource.class);
		app.setHeadless(true);
		app.setLogStartupInfo(true);
		app.setAdditionalProfiles("elevator.runtime.virtual", "workload.toy");
		// app.setWebEnvironment(true);
		// app.setBannerMode(Banner.Mode.OFF);

		app.run(args);
		System.out.println("aaaa");
	}
}
