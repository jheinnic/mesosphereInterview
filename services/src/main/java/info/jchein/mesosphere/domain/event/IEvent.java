package info.jchein.mesosphere.domain.event;

public interface IEvent<T> {
	T getEventType();
}
