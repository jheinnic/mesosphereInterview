package info.jchein.mesosphere.elevator.simulator.passengers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import org.javasim.streams.UniformStream;
import org.springframework.stereotype.Component;

@Component
public class RandomPathSelector implements IPathSelector {

	private int floorCount;
	private UniformStream prng;
	private ArrayList<int[]> pathContent;
	private Iterator<int[]> pathIter;

	public enum PathStyle {
		FROM_LOBBY_AND_BACK, TO_LOBBY_AND_BACK, FROM_LOBBY_JUST_ONCE, UP_ONCE_AND_BACK, TOP_FLOOR_RESTAURANT, CONFERENCE_ROOMS_FLOOR, OOPS_WRONG_FLOOR
	}

	public RandomPathSelector(int floorCount, UniformStream prng) {
		this.pathContent = new ArrayList<int[]>();
		this.floorCount = floorCount;
		this.prng = prng;
	}

	public void addPaths(Collection<int[]> presetPaths) {
		presetPaths.parallelStream().<int[]>forEach(input -> {
			this.pathContent.add(Arrays.copyOf(input, input.length));
		});
	}

	public void generatePaths(int pathCount, int pathLen, boolean fromLobby, boolean toLobby, int minTravel,
			int maxTravel) {
		for (int ii = 0; ii < pathCount; ii++) {
			int[] path = new int[pathLen];
			int jj = 0;
			int kk = pathLen;

			if (fromLobby) {
				path[0] = 0;
				jj = 1;
			}
			if (toLobby) {
				kk = kk - 1;
				path[kk] = 0;
			}
			int previous = 0;
			try {
				for (; jj < kk; jj++) {
					int next = previous;
					int delta = -1;
					while ((delta < minTravel) || (delta > maxTravel)) {
						next = (int) Math.round(this.prng.getNumber() * this.floorCount);
						delta = previous - next;
						if (delta < 0) {
							delta = delta * -1;
						}
					}
					path[jj] = next;
				}
			} catch (ArithmeticException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void randomize() {
		Collections.shuffle(this.pathContent);
		this.pathIter = this.pathContent.iterator();
	}

	@Override
	public int[] selectFloorVisitOrder() {
		if (this.pathIter.hasNext() == false) {
			this.randomize();
		}
		return this.pathIter.next();
	}

}
