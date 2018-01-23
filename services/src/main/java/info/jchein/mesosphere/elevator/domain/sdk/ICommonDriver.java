package info.jchein.mesosphere.elevator.domain.sdk;

public interface ICommonDriver {
	void setTimer(String key, long delayMs);
	void abortTimer(String key);
}
