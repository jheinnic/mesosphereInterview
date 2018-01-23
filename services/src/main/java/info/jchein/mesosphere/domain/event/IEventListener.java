package info.jchein.mesosphere.domain.event;

public interface IEventListener<E extends IEvent<?>> {
    void handleEvent( E event );
}
