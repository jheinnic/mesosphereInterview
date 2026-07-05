package info.jchein.mesosphere.elevator.control.manifest;

import java.util.TreeSet;


class FloorOfOrigin
{
    private final int floorIndex;
    private double ownWeightAtPickup;
    private double ownWeightRemaining;
    private final TreeSet<Integer> dropFloors = new TreeSet<>();
    private int currentOpenFloor = -1;

    FloorOfOrigin(int floorIndex)
    {
        this.floorIndex = floorIndex;
    }

    int getFloorIndex() { return floorIndex; }
    double getOwnWeightAtPickup() { return ownWeightAtPickup; }
    double getOwnWeightRemaining() { return ownWeightRemaining; }

    /**
     * Called by TravelGraph before firing the DOORS_OPENED FSM event so that
     * isPotentialStop() and isLastStop() can evaluate against the current floor.
     */
    void notifyDoorsOpening(int floorIndex) {
        this.currentOpenFloor = floorIndex;
    }

    boolean isPotentialStop() {
        return dropFloors.contains(currentOpenFloor);
    }

    boolean isLastStop() {
        return dropFloors.size() == 1;
    }

    void trackDropRequest(Integer floorIndex) {
        dropFloors.add(floorIndex);
    }

    void trackBeginPickup(Long clockTime) { }

    void trackBeginVisit(Long clockTime) { }

    void trackOwnBoardingWeight(Double weight) {
        ownWeightAtPickup += weight;
        ownWeightRemaining += weight;
    }

    void trackPeerBoardingWeight(Double weight) { }

    void trackDisembarkingWeight(Double weight) {
        ownWeightRemaining = Math.max(0.0, ownWeightRemaining - weight);
    }

    void trackCompletePickup() { }

    void trackCompleteVisit() {
        dropFloors.remove(currentOpenFloor);
    }
}
