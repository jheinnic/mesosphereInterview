package info.jchein.mesosphere.elevator.simulator.traveller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import org.javasim.streams.NormalStream;
import org.javasim.streams.UniformStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;

@Component
public class BehaviorStrategyFactory implements IBehaviorStrategyFactory {
	private final IClock systemClock;
	private final EventBus eventBus;

	@Autowired
	BehaviorStrategyFactory(final IClock systemClock, final EventBus eventBus) {
		this.systemClock = systemClock;
		this.eventBus = eventBus;
	}

	private static class CdfItem<T> implements Comparable<CdfItem<T>> {
		double cdfMax;
		T item;
		
		CdfItem(double cdfMax, T item) {
			this.cdfMax = cdfMax;
			this.item = item;
		}

		@Override
		public int compareTo(CdfItem<T> o) {
			if (this.cdfMax < o.cdfMax) {
				return -1;
			} else if( this.cdfMax > o.cdfMax ) {
				return 1;
			}
			
			return 0;
		}
	}

	@Override
	public IProbabilityResolver<String> parseCdfString(String activityCdf) {
		Stream<CdfItem<String>> sortedPairs = Stream.of(activityCdf.split(";")).map(item -> {
			String[] pair = item.split(":");
			return new CdfItem<String>(Double.parseDouble(pair[0]), pair[1]);
		}).sorted();
		double[] cdfLimits = sortedPairs.mapToDouble(itemPair -> itemPair.cdfMax).toArray();
		String[] activityNames = sortedPairs.map(itemPair -> itemPair.item).toArray(String[]::new);
		
		return new ProbabilityResolver<String>(cdfLimits, activityNames);
	}

	@Override
	public IProbabilityResolver<ActivityLocation> compile(LocationCdf locationCdf) {
		Stream<CdfItem<ActivityLocation>> sortedPairs = Stream.of(
			new CdfItem<ActivityLocation>(locationCdf.getCdfMaxFloorA(), ActivityLocation.WORKFLOOR_A),
			new CdfItem<ActivityLocation>(locationCdf.getCdfMaxFloorB(), ActivityLocation.WORKFLOOR_B),
			new CdfItem<ActivityLocation>(locationCdf.getCdfMaxFloorC(), ActivityLocation.WORKFLOOR_C),
			new CdfItem<ActivityLocation>(locationCdf.getCdfMaxLobby(), ActivityLocation.LOBBY)
		).sorted();
		
		double[] cdfLimits = sortedPairs.mapToDouble(itemPair -> itemPair.cdfMax).toArray();
		ActivityLocation[] locations = sortedPairs.map(itemPair -> itemPair.item).toArray(ActivityLocation[]::new);
		
		return new ProbabilityResolver<ActivityLocation>(cdfLimits, locations);
	}

	@Override
	public IBehaviorStrategy initializeBehaviorStrategy(BehaviorModel model, int floorA, int floorB, int floorC) {
		final ArrayList<DayPhase> dayPhases =
			new ArrayList<DayPhase>(model.getTimesOfDay().size());
		final UniformStream probabilityStream = new UniformStream(0, 1);
		final IProbabilityResolver<String> initActivity =
			this.parseCdfString(model.getInitialActivityCdf());
		final IProbabilityResolver<ActivityLocation> initLocation =
			this.compile(model.getInitialLocation());

		for (final TimeOfDay nextTod: model.getTimesOfDay()) {
			final HashMap<String, Activity> activitiesByName = new HashMap<String, Activity>();
			final NormalStream phaseDuration = 
				new NormalStream(nextTod.getInSystemMean(), nextTod.getInSystemStdDev());

			for (final ActivityDescriptor nextActivity: nextTod.getActivities()) {
				final NormalStream activityDuration =
					new NormalStream(nextActivity.getDurationMean(), nextActivity.getDurationStdDev());
				final IProbabilityResolver<String> nextFn =
					this.parseCdfString(nextActivity.getNextActivityCdf());
				final Activity activityImpl = new Activity(nextActivity.getName(), activityDuration,
					nextFn, nextActivity.getLocation(), this.systemClock, this.eventBus);
				activitiesByName.put(
					nextActivity.getName(), activityImpl);
				
				final String aliases = nextActivity.getAliases();
				if (aliases != null && aliases.isEmpty() == false) {
					for (final String nextAlias : aliases.split("\\s*,\\s*")) {
						activitiesByName.put(nextAlias, activityImpl);
					}
				}
			}
			
			dayPhases.add(
				new DayPhase(activitiesByName, phaseDuration));
		}
		
		return new BehaviorStrategy(this.systemClock, this.eventBus, dayPhases, probabilityStream, initActivity, initLocation, floorA, floorB, floorC);
	}

}
