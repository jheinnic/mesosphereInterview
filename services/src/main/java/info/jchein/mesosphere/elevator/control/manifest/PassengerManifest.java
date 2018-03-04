package info.jchein.mesosphere.elevator.control.manifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.PostConstruct;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.CarIndexContext;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.physics.TravelPathDownComparator;
import info.jchein.mesosphere.elevator.common.physics.TravelPathUpComparator;
import info.jchein.mesosphere.elevator.control.IPassengerManifest;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledStop.ScheduledStopBuilder;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PassengerManifest implements IPassengerManifest
{
   private int carIndex;
   private IRuntimeEventBus carLocalEventBus;
   private double incomingLoad;
   private double outgoingLoad;
   private double currentLoad;
   private int currentFloor;
   private ArrayList<ScheduledStop> scheduledStops;
   private ArrayList<PassengersOrigin> onboardPassengers;
   private ITravelGraph currentTravelGraph;
   private ITravelGraph nextTravelGraph;
   private DirectionOfTravel currentDirection;
   private DirectionOfTravel nextDirection;
   private Comparator<ScheduledStop> currentComparator;
   private ScheduledStop currentObjective;

   
   public PassengerManifest(CarIndexContext carIndexContext, IRuntimeEventBus carLocalEventBus ) {
      this.carIndex = carIndexContext.getCarIndex();
      this.carLocalEventBus = carLocalEventBus;
      this.incomingLoad = 0;
      this.outgoingLoad = 0;
      this.currentLoad = 0;
      this.currentFloor = 0;
      this.scheduledStops = new ArrayList<ScheduledStop>(4);
      this.onboardPassengers = new ArrayList<PassengersOrigin>(4);
      this.currentDirection = DirectionOfTravel.GOING_UP;
      this.currentTravelGraph = this.allocateTravelGraph(DirectionOfTravel.GOING_UP);
      this.currentComparator = new TravelPathUpComparator(this.currentFloor);
      this.nextTravelGraph = null;
      this.nextDirection = null;
   }
   
   @PostConstruct
   void init()
   { 
      this.carLocalEventBus.registerListener(this);
   }
   
   abstract protected ITravelGraph allocateTravelGraph(DirectionOfTravel direction);
   
   
   @Subscribe
   public void trackDoorsOpening(PassengerDoorsOpened event) {
      this.nextDirection = event.getDirection();
      if (this.nextDirection != this.currentDirection) {
         this.nextTravelGraph = this.allocateTravelGraph(this.nextDirection);
         // this.currentTravelGraph.validateChangeOfDirection();
      } else {
         this.nextTravelGraph = this.currentTravelGraph;
      }
      
      this.currentTravelGraph.prepareForDisembarking(event.getFloorIndex());
      this.nextTravelGraph.prepareForDisembarking(event.getFloorIndex());
   }

   @Subscribe
   public void trackWeightChange(WeightLoadUpdated event) 
   {
      // TOOO: Fix weight semantics!
      double weightLoadDelta = event.getCurrent();
      if (weightLoadDelta < 0) {
         this.outgoingLoad = this.outgoingLoad - weightLoadDelta;
         Preconditions.checkState(this.outgoingLoad <= this.currentLoad);
      } else {
         this.incomingLoad = this.incomingLoad + weightLoadDelta;
      }
   }

   @Subscribe
   public void trackDoorsClosed(PassengerDoorsClosed event)
   {
      if ( this.currentTravelGraph != this.nextTravelGraph ) {
         double carryOver = this.currentTravelGraph.getCurrentWeightLoad();
         if (carryOver > 0) {
            log.warn("Passengers still on board at direction reversal reclassifying : {}", carryOver);
            this.incomingLoad += carryOver;
            this.outgoingLoad += carryOver;
         }
      }

      this.currentTravelGraph.recordDisembarked(this.outgoingLoad);
      this.nextTravelGraph.recordBoarded(this.incomingLoad);
      this.currentLoad = this.currentLoad + this.incomingLoad - this.outgoingLoad;
   }

   private void enqueueNewStop(final ScheduledStopBuilder newStop)
   {
      int destinationFloor = newStop.getFloorIndex();
      Preconditions.checkArgument(this.currentFloor != destinationFloor);
      
      if (this.currentComparator == null) {
         // This is the first call from a parked state.  Set direction and launch!
         if (this.currentFloor < destinationFloor) {
            this.currentDirection = DirectionOfTravel.GOING_UP;
            this.currentComparator = new TravelPathUpComparator(this.currentFloor);
         } else {
            this.currentDirection = DirectionOfTravel.GOING_DOWN;
            this.currentComparator = new TravelPathDownComparator(this.currentFloor);
         }
         this.scheduledStops.add(newStop);
      } else {
         int insertionPoint = Collections.<ScheduledStop>binarySearch(this.scheduledStops, newStop, this.currentComparator);
         this.scheduledStops.add(insertionPoint, newStop);
         
         if (this.currentComparator.compare(newStop, this.currentObjective) < 0) {
            // TODO: Midflight destination change!!
            this.currentObjective = newStop;
         }
      }
   }

   @Subscribe
   public void trackDropRequest(DropOffRequested dropOffRequested) 
   {
      final int destinationFloor = dropOffRequested.getDropOffFloorIndex();
      Preconditions.checkArgument(this.currentFloor != destinationFloor);

      final ScheduledStopBuilder newStop =
         ScheduledStop.builder().floorIndex(destinationFloor)
         .inbound((destinationFloor > this.currentFloor) ? DirectionOfTravel.GOING_UP : DirectionOfTravel.GOING_DOWN)
      enqueueNewStop(newStop);

      this.nextTravelGraph.recordDropRequest(dropOffRequested.getDropOffFloorIndex());
   }

   @Override
   public void trackAssignedPickup(int floorIndex, DirectionOfTravel direction)
   {
      Preconditions.checkArgument(this.currentFloor != floorIndex);
      final ScheduledPickup newStop = new ScheduledPickup(floorIndex, direction);
      enqueueNewStop(floorIndex, newStop);
      
      this.nextTravelGraph.recordAssignedPickup(floorIndex, direction);
   }

   @Override
   public void trackCanceledPickup(int floorIndex, DirectionOfTravel direction)
   {
      final ScheduledPickup newStop = new ScheduledPickup(floorIndex, direction);
      int insertionPoint = Collections.<ScheduledStop>binarySearch(this.scheduledStops, newStop, this.currentComparator);
      this.scheduledStops.remove(insertionPoint);
      
      // TODO: Consider the case where this now replaces a travel in progress.

      this.nextTravelGraph.recordCancelledPickup(floorIndex, direction);
   }

   @Override
   public double getCurrentWeightLoad()
   {
      return this.currentLoad + this.incomingLoad - this.outgoingLoad;
   }
}
