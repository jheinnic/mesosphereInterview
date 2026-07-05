package info.jchein.mesosphere.elevator.control.manifest;

import org.statefulj.fsm.FSM;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;


class TravelGraphIndex
{
    private final int numFloors;
    private final DirectionOfTravel t;
    private final FSM<FloorOfOrigin> floorFsm;

    TravelGraphIndex(int numFloors, DirectionOfTravel t, FSM<FloorOfOrigin> floorFsm)
    {
        this.numFloors = numFloors;
        this.t = t;
        this.floorFsm = floorFsm;
    }

    int getNumFloors() { return numFloors; }
    DirectionOfTravel getT() { return t; }
    FSM<FloorOfOrigin> getFloorFsm() { return floorFsm; }
}
