package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.RuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.model.IElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;

@Configuration
public class SimulatedTravellersConfiguration // <V extends ITravellerRandomVariables, T extends AbstractSimulatedTraveller<V>> implements ISimulatedTravellerSource
{
 }
