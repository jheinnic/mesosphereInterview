package test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.configuration.ElevatorConfigurationProperties;
import info.jchein.mesosphere.elevator.configuration.ElevatorSchedulerConfiguration;
import info.jchein.mesosphere.elevator.configuration.ElevatorGroupControlConfiguration;
import info.jchein.mesosphere.elevator.configuration.ElevatorSimulationConfiguration;
import info.jchein.mesosphere.elevator.simulator.IElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulatedTravellingPassengerSource;

@SpringBootApplication
@Import({ ElevatorSchedulerConfiguration.class, ElevatorGroupControlConfiguration.class,
		ElevatorSimulationConfiguration.class, ElevatorConfigurationProperties.class })
@ComponentScan(basePackages={
    "info.jchein.mesosphere.elevator.configuration",
    "info.jchein.mesosphere.elevator.configuration.workloads",
})
@Configuration
public class RunElevator2 {
	public static void main(final String[] args) {
		final SpringApplication app = new SpringApplication(RunElevator2.class);
		app.setHeadless(true);
		app.setLogStartupInfo(true);
		// app.setWebEnvironment(true);
		// app.setBannerMode(Banner.Mode.OFF);
		final ConfigurableApplicationContext context = app.run(args);

		SimulatedTravellingPassengerSource source = context.getBean(SimulatedTravellingPassengerSource.class);
			
		IElevatorSimulation simulation = context.getBean(IElevatorSimulation.class);
		simulation.start();
		System.out.println("aaaa");
	}
}
