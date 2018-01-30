package info.jchein.mesosphere.elevator.domain.model;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupSnapshot;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import rx.Observable;
import rx.Scheduler;

@Component
public class ElevatorGroup implements IElevatorGroupControl {
	private final ImmutableList<? extends IElevatorCar> carList;
	private final IElevatorCallDispatcher dispatcher;
	private final IRuntimeEventBus eventBus;
	private final IRuntimeClock clock;
   private final IRuntimeScheduler scheduler;
	
	@Autowired
	public ElevatorGroup(
		@NotNull ImmutableList<? extends IElevatorCar> carList,
		@NotNull IElevatorCallDispatcher dispatcher,
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
	   /*
		this.eventBus.register(this.maintenanceHandler);
		this.eventBus.register(this.analyzer);

		int ii = 0;
		for (final IElevatorCar nextCar: ElevatorGroup.this.carList) {
			this.carStateModel[ii++] = nextCar.pollForBootstrap();
		}
		
		this.scheduler.bootstrapModel(this.carStateModel);
		
		for (final ILandingControls nextLanding: this.hallList) {
			nextLanding.pollForBootstrap();
		}

		for (final IElevatorCar nextCar: ElevatorGroup.this.carList) {
			nextCar.pollForClock();
		}
		*/
	}

   @Override
   public Observable<ElevatorGroupSnapshot> getStates()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Scheduler getScheduler()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public IRuntimeEventBus getEventBus()
   {
      return this.eventBus;
   }

   @Override
   public IRuntimeClock getRuntimeClock()
   {
      // TODO Auto-generated method stub
      return null;
   }
}
	