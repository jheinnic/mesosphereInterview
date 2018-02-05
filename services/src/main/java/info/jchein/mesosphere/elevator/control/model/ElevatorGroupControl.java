package info.jchein.mesosphere.elevator.control.model;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.Event;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import rx.Emitter.BackpressureMode;
import rx.Observable;

@Component
public class ElevatorGroupControl implements IElevatorGroupControl {
	private final ImmutableList<? extends IElevatorCar> carList;
	private final ICallDispatcher dispatcher;
	private final IRuntimeEventBus eventBus;
	private final IRuntimeClock clock;
   private final IRuntimeScheduler scheduler;
   
   private Observable<Event> changeStream;
	
	@Autowired
	public ElevatorGroupControl(
		@NotNull ImmutableList<? extends IElevatorCar> carList,
		@NotNull ICallDispatcher dispatcher,
		@NotNull IRuntimeScheduler scheduler,
		@NotNull IRuntimeEventBus eventBus,
		@NotNull IRuntimeClock clock
	) {
		this.dispatcher = dispatcher;
		this.carList = carList;
      this.scheduler = scheduler;
		this.eventBus = eventBus;
		this.clock = clock;
	}

	@PostConstruct
	void init() {
	   this.changeStream = Observable.<Event>create((emitter) -> {
	     this.eventBus.registerListener(new Object() {
	        @Subscribe
	        public void handleEvent(Event event) {
	           emitter.onNext(event);
	        }
	     }); 
	   }, BackpressureMode.BUFFER);
	}

   @Override
   public Observable<Event> getChangeStream()
   {
      return this.changeStream;
   }

}
	