package info.jchein.mesosphere.elevator.emulator.physics;


import java.util.Iterator;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
abstract class AbstractPathLeg
implements IPathLeg
{
   protected final PathMoment initialMoment;
   protected final double finalTime;
   protected final double duration;


   protected AbstractPathLeg( PathMoment initialMoment, double duration )
   {
      this.duration = duration;
      this.initialMoment = initialMoment;
      this.finalTime = initialMoment.getTime() + duration;
   }


   @Override
   public double getJerk()
   {
      return this.initialMoment.getJerk();
   }


   public double getInitialTime()
   {
      return this.initialMoment.getTime();
   }


   @Override
   public double getInitialHeight()
   {
      return this.initialMoment.getHeight();
   }


   @Override
   public double getInitialVelocity()
   {
      return this.initialMoment.getVelocity();
   }


   @Override
   public double getInitialAcceleration()
   {
      return this.initialMoment.getAcceleration();
   }


   @Override
   public PathMoment getInitialMoment()
   {
      return this.initialMoment;
   }


   @Override
   public PathMoment getFinalMoment(double nextJerk)
   {
      return PathMoment.build(it -> {
         it.time(this.finalTime)
            .height(this.getFinalHeight())
            .velocity(this.getFinalVelocity())
            .acceleration(this.getFinalAcceleration())
            .jerk(nextJerk);
      });
   }


   /**
    * Factory method for a model-specific iterator that returns a series of PathMoments calculated using either constant
    * velocity, constant acceleration, or non-zero jerk. This abstract class will have computed the duration of any
    * irregularity required from the first tick, but it relies on the concrete subtypes to provide an implementation of
    * the appropriate formula for calculating displacement intervals.
    * 
    * It is unfortunate but true that this method has the same number and types for its parameters as found in
    * {@link #toMomentIterator(double, double)} since the arguments do not mean the same thing. The first argument of
    * {@link #toMomentIterator(double, double)} is the absolute time index of a first tick interval. The first argument
    * of this method does not specify a time index, but rather a duration. The time index of that first interval is
    * always the initial moment of this PathLeg. The {@link #toMomentIterator(double, double)} method does sets the
    * arguments to this method based on what its computation informed it could use to satisfy the contract of its own
    * call signature.
    * 
    * @param intialTickDuration
    *           The tick duration to pass between initialMoment and its first derived PathMoment.
    * @param tickDuration
    *           The tick duration to pass between all PathMoments after the first derived PathMoment.
    * 
    * @return An Iterator<PathMoment> that presents this PathLeg's total displacement as a series of PathMoments
    *         separated by small consecutive tickDuration time intervals.
    */
   abstract AbstractPathLegMomentIterator
   getMomentIterator(double firstTickDuration, double tickDuration);


   @Override
   public Iterator<PathMoment> toMomentIterator(final double firstTick, final double tickDuration)
   {
      final PathMoment initialMoment = this.getInitialMoment();
      final double initialTime = initialMoment.getTime();
      final double nextTick = firstTick + tickDuration;

      // We may legitimately be asked for a moment iterator from a firstTick that is after our initial moment,
      // regardless of whether or not that start time is before our finalTime, and regardless of whether or not
      // we have sufficient duration to return a single tick even if the start is within our time span.
      //
      // However, it is a programming error if we receive a request for a moment iterator that has no portion of
      // overlap between its first tick and our complete duration beccause we cannot even produce an empty iterator
      // without creating an ambiguity since an empty iterator indicates the result accounts for all moments
      // up to and including the end of the PathLeg that returned it.
      //
      // For the sake of the above, a PathLeg with zero duration has overlap with a tick if the zero-duration instant
      // either falls on the tick's start time or between its start and ending times, but does not overlap if it
      // occurs at the same time the tick ends. This sholud never be an issue. A zero duration path leg that
      // falls at the end of a tick will either be:
      // 1) At the beginning of the arc, in which case we would never have requested a tick that ended at its
      // specific time instant because the first tick always begins across any arc of PathLegs begins at the
      // start time of its first PathLeg.
      // 2) In the middle of the arc, in which case there would have been a PathLeg preceding it whose time span
      // ended at the same instant the zero-duration PathLeg occurs, and that would allow the tick to have been
      // accounted for in its own series of PathMoments before reaching the zero-duration PathLeg.
      Preconditions.checkArgument(
         initialTime < nextTick,
         "We can only produce an unambiguous moment series if the first tick ends after we begin");
      Preconditions.checkArgument(
         this.finalTime >= firstTick,
         "We can only produce an unambiguous moment series if the first tick begins as or before we end");

      final Iterator<PathMoment> retVal;
      if (initialTime == firstTick) {
         retVal = this.getMomentIterator(tickDuration, tickDuration);
      } else if (initialTime < firstTick) {
         final AbstractPathLegMomentIterator skippingIterator =
            this.getMomentIterator(firstTick - initialTime, tickDuration);

         // Skip past the partial interval between initalTime and firstTick if that interval is covered within thie
         // PathLeg, which will update the tickDuration to the post-initial value. Note that if the first tick only has
         // partial overlap with the end of this PathLeg, skipping the call to next() here will correctly yield an empty
         // iterator, causing any JourneyArc traversal to advance to the next PathLeg, where that overlap continues on.
         if (skippingIterator.hasNext()) {
            skippingIterator.next();
         }

         retVal = skippingIterator;
      } else {
         // In this case, we do not have the luxury of discarding the irregular first interval--it is a part of the
         // sequence
         // to be returned. Instead, we leverage a feature of the AbstractPathLegMomentIterator that allows it to use a
         // different tickDuration for the first value returned, after which it performs a one-time update to the
         // tickDuration
         // it uses for the remainder of its work.
         retVal = this.getMomentIterator(nextTick - initialTime, tickDuration);
      }

      return retVal;
   }
}
