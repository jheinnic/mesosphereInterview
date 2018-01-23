package info.jchein.mesosphere.elevator.domain.sdk;

public interface IElevatorDriverFactory {
	IHallPanelDriver attachFloorHallDriver(IHallPanelPort port);
	IElevatorCarDriver attachElevatorCarDriver(IElevatorCarPort port);
}
