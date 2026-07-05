package info.jchein.mesosphere.elevator.control.event;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.runtime.event.EventBusEmitter;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import rx.Emitter;

public class ElevatorCarEventEmitter extends EventBusEmitter<ElevatorCarEvent>
{
   public ElevatorCarEventEmitter( Emitter<ElevatorCarEvent> emitter, IRuntimeEventBus eventBus )
   {
      super(emitter, eventBus);
   }

   @Subscribe
   public void onElevatorEvent(ElevatorCarEvent event) {
      this.emit(event);
   }
}
