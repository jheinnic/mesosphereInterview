package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.javasim.streams.NormalStream;
import org.javasim.streams.RandomStream;
import org.javasim.streams.UniformStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.model.impl.FactoryImpl;
import org.statefulj.persistence.annotations.State.AccessorType;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.simulator.traveller.IBehaviorStrategy;
import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext;

import org.statefulj.persistence.annotations.State;

public class TravelPathFSM {
	@State(accessorType = AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
	private String state;

	// private final UniformStream probabilityStream;
	// private NormalStream durationStream;

	private final IClock systemClock;
	private final IBehaviorStrategy behaviorStrategy;
	private final EventBus eventBus;

	private TravellerContext travellerContext;

	public final static String BEAN_ID = "TravelPathFSM";

	public final static String START_STATE = "OUTSIDE_SIMULATION";
	public final static String GO_TO_ACTIVITY = "GO_TO_ACTIVITY";

	public final static String LOBBY = "LOBBY";
	public final static String WORKFLOOR_A = "WORKFLOOR_A";
	public final static String WORKFLOOR_B = "WORKFLOOR_B";
	public final static String WORKFLOOR_C = "WORKFLOOR_C";

	public final static String ENTER_SIMULATION = "enterSimulation";
	public final static String BEGIN_FROM_LOBBY = "beginFromLobby";
	public final static String BEGIN_FROM_A = "beginFromFloorA";
	public final static String BEGIN_FROM_B = "beginFromFloorB";
	public final static String BEGIN_FROM_C = "beginFromFloorC";

	@Autowired
	public TravelPathFSM(IClock systemClock, IBehaviorStrategy behaviorStrategy, EventBus eventBus) {
		this.systemClock = systemClock;
		this.behaviorStrategy = behaviorStrategy;
		this.eventBus = eventBus;
		this.travellerContext = behaviorStrategy.allocateNewTraveller();
	}

	/*
	 * public TravelPathFSM() { List<State<TravelPathFSM>> states = new
	 * ArrayList<State<TravelPathFSM>>(3); states.add(stateA); states.add(stateB);
	 * states.add(stateC);
	 * 
	 * MemoryPersisterImpl<Foo> persister = new MemoryPersisterImpl<Foo>( states, //
	 * Set of States stateA); // Start State
	 * 
	 * stateB.addTransition(eventA, new Transition<Foo>() {
	 * 
	 * public StateActionPair<Foo> getStateActionPair(Foo stateful) { State<Foo>
	 * next = null; if (stateful.isBar()) { next = stateB; } else { next = stateC; }
	 * 
	 * // Move to the next state without taking any action // return new
	 * StateActionPairImpl<Foo>(next, null); } });
	 */

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}