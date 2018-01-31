package info.jchein.mesosphere.elevator.domain.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.domain.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDispatcherPort;
import info.jchein.mesosphere.elevator.scheduler.tracking.HeuristicElevatorSchedulingStrategy;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
public class CommonElevatorControlConfiguration {
}