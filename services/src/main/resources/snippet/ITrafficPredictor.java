package info.jchein.mesosphere.elevator.scheduler.tracking;

import java.util.List;
import java.util.concurrent.TimeUnit;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

public interface ITrafficPredictor {
	/**
	 * Updates prediction model to account for a landing call received at a given call time from a given floor and returns 
	 * the current prediction for passenger arrival times since the last call to this method, which may include additional
	 * passenger arrivals at floors with an outstanding previous landing call.  The return will at minimum include a 
	 * prediction for the current landing call--it will never be an empty list.
	 * 
	 * @param callTime 
	 * @param floorIndex
	 * @throw IllegalArgumentException If callTime is earlier than any value previously provided to this method or if
	 * floorIndex is less than 0 or exceeds the largest floorIndex used in the current execution.
	 * @return
	 */
    public List<CallPrediction> observeLandingCall(long callTime, int floorIndex, DirectionOfTravel direction);
    
    /**
     * Predicts the next time interval worth of future arrivals, given model state.  The interval considered is relative
     * to the current time provided, which must not be earlier than any other time stamp provided to any other methods of
     * this interface.  In the event that prediction falls within the same time prediction window of another previous call
     * without breaking the current time rule, a record will appear in both method call's return lists unless something caused
     * the record to not appear in one prediction or the other.
     */
    public List<CallPrediction> predictFutureArrivals(long fromNow, long predictionWindow, TimeUnit windowUnits);
}
