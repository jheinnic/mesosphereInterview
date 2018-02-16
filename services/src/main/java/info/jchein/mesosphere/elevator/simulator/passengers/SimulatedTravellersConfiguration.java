package info.jchein.mesosphere.elevator.simulator.passengers;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return.WithLobbyReturnPathStyleConfiguration;


@Configuration
@Import(WithLobbyReturnPathStyleConfiguration.class)
public class SimulatedTravellersConfiguration
{
   
}
