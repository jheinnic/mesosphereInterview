package info.jchein.mesosphere.elevator.common.graph;

import java.util.BitSet;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.BuildingProperties;
import info.jchein.mesosphere.elevator.common.graph.TravelPathStageNodes.TravelPathStageNodesBuilder;

public class VertexFactory implements IVertexFactory
{
   private final int numElevators;
   private final int numFloors;

   private final BuildingFloor[] buildingFloors;
   private final ProtectedBitSet[] oneFloorBitSets;

   private final DirectedAdjacency[] ascendingDirectAdjacencies;
   private final DirectedAdjacency[] descendingDirectAdjacencies;
   private final AscendingAdjacency[] ascendingAdjacencies;
   private final DescendingAdjacency[] descendingAdjacencies;
   
//   private final TravelArc[][] travelArcs;
//   private final TravellerIntent[][] travellerIntent;
   
   private final PickupHeading[] ascendingPickupHeadings;
   private final PickupHeading[] descendingPickupHeadings;
   private final CarPickupHeading[][] ascendingCarPickupHeadings;
   private final CarPickupHeading[][] descendingCarPickupHeadings;

   private final CarTravelType[][] ascendingCarTravelTypes;
   private final CarTravelType[][] descendingCarTravelTypes;
   private final CarTravelType[][] parkingCarTravelTypes;
   
   private final CarLanding[][] carLandings;

   private final BeforeTravelling travelPathOriginNode;
   private final AfterTravelling travelPathTerminalNode;
   
   // The BitSets that partition the pickup and drop off floors creaete too many permutations to pre-allocate these that get used with bookkeeping for 
   // projected and potential travel time costs, and possibly for deducing origins from weight deltas.  Use a weak interner to track the in-use set dynamically
   // in order to reuse as much as we can, without also holding on to much we don't.
   private final Interner<InitialPickupStep> pickupStepInterner;
   private final Interner<OngoingRidersStep> ongoingStepInterner;
   private final Interner<FinalDropOffStep> dropOffStepInterner;
   
   // BitSets have a large number of permutations as well, but they tend to see a greater frequency of bias towards reusability, and they are not nearly as
   // potentially plentiful as the step structures since they lack a cross-product detail.  Use a Strong Interner for readonly BitSet wrappers.
   private final Interner<ProtectedBitSet> bitSetInterner;

   VertexFactory(BuildingProperties bldgProps) {
      this.numFloors = bldgProps.getNumFloors();
      this.numElevators = bldgProps.getNumElevators();
      
      this.travelPathOriginNode = new BeforeTravelling();
      this.travelPathTerminalNode = new AfterTravelling();

      this.buildingFloors = new BuildingFloor[numFloors];
      this.oneFloorBitSets = new ProtectedBitSet[numFloors];

      this.ascendingPickupHeadings = new PickupHeading[numFloors];
      this.descendingPickupHeadings = new PickupHeading[numFloors];
      
      this.ascendingDirectAdjacencies = new DirectedAdjacency[numFloors];
      this.descendingDirectAdjacencies = new DirectedAdjacency[numFloors];

      this.ascendingAdjacencies = new AscendingAdjacency[numFloors];
      this.descendingAdjacencies = new DescendingAdjacency[numFloors];
      
//      this.travelArcs = new TravelArc[numFloors][numFloors];
//      this.travellerIntent = new TravellerIntent[numFloors][numFloors];

      this.carLandings = new CarLanding[numElevators][numFloors];

      this.ascendingCarPickupHeadings = new CarPickupHeading[numElevators][numFloors];
      this.descendingCarPickupHeadings = new CarPickupHeading[numElevators][numFloors];
      
      this.ascendingCarTravelTypes = new CarTravelType[numElevators][numFloors];
      this.descendingCarTravelTypes = new CarTravelType[numElevators][numFloors];
      this.parkingCarTravelTypes = new CarTravelType[numElevators][numFloors];

      this.pickupStepInterner = Interners.newWeakInterner();
      this.ongoingStepInterner = Interners.newWeakInterner();
      this.dropOffStepInterner = Interners.newWeakInterner();
      this.bitSetInterner = Interners.newStrongInterner();

      for( int ii=0; ii<numFloors; ii++ ) {
         final BitSet bits = new BitSet(this.numFloors);
         bits.set(ii);
         this.oneFloorBitSets[ii] = new ProtectedBitSet(bits);

         this.buildingFloors[ii] = new BuildingFloor(ii);
         this.descendingAdjacencies[0] = null;
         this.descendingDirectAdjacencies[0] = null;

         if (ii > 0) {
            this.descendingDirectAdjacencies[ii] = new DirectedAdjacency(ii, ii - 1);
            this.descendingAdjacencies[ii] = new DescendingAdjacency(ii, ii - 1);
            this.descendingPickupHeadings[ii] = new PickupHeading(ii, DirectionOfTravel.GOING_DOWN);
         }
         if (ii < numFloors - 1) {
            this.ascendingDirectAdjacencies[ii] = new DirectedAdjacency(ii, ii + 1);
            this.ascendingAdjacencies[ii] = new AscendingAdjacency(ii, ii + 1);
            this.ascendingPickupHeadings[ii] = new PickupHeading(ii, DirectionOfTravel.GOING_UP);
         }
         
         for (int jj=0; jj<numElevators; jj++) {
            this.carLandings[jj][ii] = new CarLanding(ii, jj);
            this.parkingCarTravelTypes[jj][ii] = new CarTravelType(ii, jj, DirectionOfTravel.STOPPED);

            if (ii > 0) {
               this.descendingCarPickupHeadings[jj][ii] = new CarPickupHeading(ii, jj, DirectionOfTravel.GOING_DOWN);
               this.descendingCarTravelTypes[jj][ii] = new CarTravelType(ii, jj, DirectionOfTravel.GOING_DOWN);
            }
            if (ii < numFloors - 1) {
               this.ascendingCarPickupHeadings[jj][ii] = new CarPickupHeading(ii, jj, DirectionOfTravel.GOING_UP);
               this.ascendingCarTravelTypes[jj][ii] = new CarTravelType(ii, jj, DirectionOfTravel.GOING_UP);
            }
         }

         this.descendingDirectAdjacencies[0] = null;
         this.descendingAdjacencies[0] = null;
         this.descendingPickupHeadings[0] = null;
         this.ascendingDirectAdjacencies[numFloors - 1] = null;
         this.ascendingAdjacencies[numFloors - 1] = null;
         this.ascendingPickupHeadings[numFloors - 1] = null;

         for (int jj=0; jj<numElevators; jj++) {
            this.descendingCarPickupHeadings[jj][0] = null;
            this.descendingCarTravelTypes[jj][0] = null;
            this.ascendingCarPickupHeadings[jj][numFloors - 1] = null;
            this.ascendingCarTravelTypes[jj][numFloors - 1] = null;
         }
      }
   }

   @Override
   public BuildingFloor getBuildingFloor(int floorIndex)
   {
      Preconditions.checkArgument(floorIndex >= 0 && floorIndex < this.numFloors);
      return this.buildingFloors[floorIndex];
   }

   @Override
   public BeforeTravelling getTravelPathOriginNode()
   {
      return this.travelPathOriginNode;
   }

   @Override
   public AfterTravelling getTravelPathTerminalNode()
   {
      return this.travelPathTerminalNode;
   }

   @Override
   public TravelPathStageNodes getNextPathStage(int pickupFloorIndex, int nextFloorIndex,
      @NotNull BitSet possibleDropFloors, @NotNull Collection<TravellingPassengers> previousStage)
   {
      Preconditions.checkArgument(pickupFloorIndex > 0 && pickupFloorIndex < this.numFloors);
      Preconditions.checkArgument(nextFloorIndex > 0 && nextFloorIndex < this.numFloors);
      Preconditions.checkArgument(nextFloorIndex != pickupFloorIndex);
      Preconditions.checkArgument(possibleDropFloors.get(pickupFloorIndex) == false);
      
      final ProtectedBitSet protectedPickupFloors = this.getSingleFloorBitSet(pickupFloorIndex);
      final ProtectedBitSet protectedDropOffFloors = this.getInternedFloorBitSet(possibleDropFloors);
      final TravelPathStageNodesBuilder retValBuilder = TravelPathStageNodes.builder();
      
      retValBuilder.currentPickupNode(
         this.pickupStepInterner.intern(
            new InitialPickupStep(
               protectedPickupFloors,
               pickupFloorIndex, 
               nextFloorIndex, 
               protectedDropOffFloors)));
      
      // Nodes of the previous stage already represent a mutually exclusive partitioning on the potential drop floors.  Factoring the next stage in requires
      // finding its drop off floor difference from and intersection with each of the exisitng partitions.  Since the existing nodes are already a partition, 
      // splitting each existing node based on whether or not its floor indices are also in the new stage's drop requests maintains the mutual exclusivity of
      // a partition.  Any bits of the new stage's drop requests that were not removed by matching of any existing partition yields an additional final vertex.
      // If there are N vertices in the previous stage, there will be somewhere between |N| vertices and |1 + 2N| vertices in the "next" generation.
      final BitSet nextStageOnly = protectedDropOffFloors.copy();
      previousStage.forEach( previousPool -> {
         final BitSet previousOnly = previousPool.getDropOffFloors().copy();
         final BitSet sharedDest = (BitSet) previousOnly.clone();
         
         // Common destinations will be in "and" of previous and next.  Unique stops will be in an appropriate "andNot" of previous or next with the
         // shared common destiations, removing just what's in common.
         sharedDest.and(nextStageOnly);
         previousOnly.andNot(sharedDest);
         nextStageOnly.andNot(sharedDest);
         
         // Prune any references to the current floor from each previous stage's node.  For any that had this current floor queued as a destination,
         if (previousOnly.get(pickupFloorIndex)) {
            previousOnly.clear(pickupFloorIndex);
            previousPool.getPickupFloors().stream().forEach( pickedUpFrom -> {
               retValBuilder.departureNode(
                  this.dropOffStepInterner.intern(
                     new FinalDropOffStep(pickedUpFrom, pickupFloorIndex)));
            });
         }
         
         if (! sharedDest.isEmpty()) {
            final BitSet mergedBoarding = previousPool.getPickupFloors().copy();
            mergedBoarding.set(pickupFloorIndex);
            retValBuilder.ongoingNode(
               this.ongoingStepInterner.intern(
                  new OngoingRidersStep(
                     this.doGetInternedFloorBitSet(mergedBoarding),
                     pickupFloorIndex,
                     nextFloorIndex, 
                     this.doGetInternedFloorBitSet(sharedDest))));
         }

         if (! previousOnly.isEmpty()) {
            retValBuilder.ongoingNode(
               this.ongoingStepInterner.intern(
                  new OngoingRidersStep(
                     previousPool.getPickupFloors(), 
                     pickupFloorIndex, 
                     nextFloorIndex, 
                     this.doGetInternedFloorBitSet(previousOnly))));
         }

         // If the 
      });
      
      if (! nextStageOnly.isEmpty()) {
         retValBuilder.ongoingNode(
            this.ongoingStepInterner.intern(
               new OngoingRidersStep(
                  protectedPickupFloors, 
                  pickupFloorIndex, 
                  nextFloorIndex, 
                  this.doGetInternedFloorBitSet(nextStageOnly))));
      }

      return retValBuilder.build();
   }

   public ProtectedBitSet getInternedFloorBitSet(BitSet possibleDropFloors)
   {
      Preconditions.checkArgument(possibleDropFloors.size() > 0);
      Preconditions.checkArgument(possibleDropFloors.length() <= this.numFloors);
      
      return this.doGetInternedFloorBitSet((BitSet) possibleDropFloors.clone());
   }

   private ProtectedBitSet doGetInternedFloorBitSet(BitSet possibleDropFloors)
   {
      // This method uses the non-cloning constructor, and is private so we an be careful about only calling it with BitSets that have already been
      // cloned from the outside world and are therefore safe from unexpected mutation.
      return this.bitSetInterner.intern(
         new ProtectedBitSet(possibleDropFloors, true));
   }

   public ProtectedBitSet getSingleFloorBitSet(int pickupFloorIndex)
   {
      Preconditions.checkArgument(pickupFloorIndex >= 0 && pickupFloorIndex < this.numFloors);
      
      return this.oneFloorBitSets[pickupFloorIndex];
   }
}
