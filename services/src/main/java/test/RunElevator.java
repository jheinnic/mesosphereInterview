// package test;
// 
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Import;
// 
// import info.jchein.mesosphere.elevator.configuration.ExternalConfigurationProperties;
// import info.jchein.mesosphere.elevator.configuration.ElevatorSchedulerConfiguration;
// import info.jchein.mesosphere.elevator.configuration.ElevatorGroupControlConfiguration;
// import info.jchein.mesosphere.elevator.configuration.ElevatorSimulationConfiguration;
// import info.jchein.mesosphere.elevator.simulator.IElevatorSimulation;
// 
// @SpringBootApplication
// @Import({ ElevatorSchedulerConfiguration.class, ElevatorGroupControlConfiguration.class,
// 		ElevatorSimulationConfiguration.class, ExternalConfigurationProperties.class })
// @ComponentScan(basePackages={
//     "info.jchein.mesosphere.elevator.configuration",
//     "info.jchein.mesosphere.elevator.configuration.workloads",
// })
// @Configuration
// public class RunElevator {
// 	public static void main(final String[] args) {
// 		final SpringApplication app = new SpringApplication(RunElevator.class);
// 		app.setHeadless(true);
// 		app.setLogStartupInfo(true);
// 		// app.setWebEnvironment(true);
// 		// app.setBannerMode(Banner.Mode.OFF);
// 		final ConfigurableApplicationContext context = app.run(args);
// 
// 		IElevatorSimulation simulation = context.getBean(IElevatorSimulation.class);
// 		simulation.start();
// 		System.out.println("aaaa");
// 	}
// }
