package info.jchein.mesosphere.elevator.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;

@SpringBootApplication
@EnableVirtualRuntime
public class RideOn
{
    public static void main(String[] args) {
        SpringApplication.run(RideOn.class, args);
    }
}
