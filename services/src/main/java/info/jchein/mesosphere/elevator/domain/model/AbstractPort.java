package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.sdk.ICommonDriver;
import info.jchein.mesosphere.elevator.domain.sdk.ICommonPort;

abstract class AbstractPort implements ICommonPort {
	private ICommonDriver commonDriver;

	@Override
	public void attachCommon(ICommonDriver commonDriver) {
		if (this.commonDriver != null) {
			throw new RuntimeException("Common Driver has already been attached");
		}

		this.commonDriver = commonDriver;
	}

	@Override
	public void setTimer(String key, long delayMs) {
		this.commonDriver.setTimer(key, delayMs);
	}

	@Override
	public void abortTimer(String key) {
		this.commonDriver.abortTimer(key);
	}

}
