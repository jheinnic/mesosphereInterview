package info.jchein.mesosphere.elevator.common.physics;


import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import info.jchein.mesosphere.elevator.common.physics.PathMoment;


public class JourneyArcMomentSeries
implements Iterable<PathMoment>
{
   private final Iterable<IPathLeg> journeyArc;
   private final PathMoment initial;
   private final PathMoment terminal;
   private final double tickDuration;


   JourneyArcMomentSeries( final Iterable<IPathLeg> journeyArc, final PathMoment initial, final PathMoment terminal, final double tickDuration )
   {
      this.journeyArc = journeyArc;
      this.initial = initial;
      this.terminal = terminal;
      this.tickDuration = tickDuration;
   }

   @Override
   public Iterator<PathMoment> iterator()
   {
      final Iterator<PathMoment> retVal;

      final Iterator<IPathLeg> legSource = this.journeyArc.iterator();
      if (legSource.hasNext()) {
         Iterator<PathMoment> iter = new JourneyArcPathMomentIterator(legSource, this.terminal, this.tickDuration);
         if (iter.hasNext()) {
            retVal = iter;
         } else {
            retVal = Collections.emptyIterator();
         }
      } else {
         retVal = Collections.emptyIterator();
      }

      return retVal;
   }
   
   public PathMoment getInitial() {
      return this.initial;
   }

   public PathMoment getTerminal()
   {
      return this.terminal;
   }

   static class JourneyArcPathMomentIterator
   implements Iterator<PathMoment>
   {
      private final PathMoment destination;
      private final Iterator<IPathLeg> legSource;
      private IPathLeg currentLeg;
      private Iterator<PathMoment> currentMomentSource;
      private double tickDuration;
      private double nextTickStart;

      public JourneyArcPathMomentIterator( final Iterator<IPathLeg> legSource, PathMoment destination, final double tickDuration )
      {
         this.destination = destination;
         this.tickDuration = tickDuration;
         this.legSource = legSource;
         this.currentLeg = legSource.next();
         this.nextTickStart = this.currentLeg.getInitialTime();
         this.currentMomentSource =
            this.currentLeg.toMomentIterator(this.nextTickStart, this.tickDuration);
      }


      @Override
      public boolean hasNext()
      {
         // This loop exhausts when either currentMomentSource addresses a non-empty PathMoment iterator from the most
         // recent PathLeg returned from legSource, or when currentMomentSource addresses an exhausted PathMoment iterator
         // and legSource has also run out of PathLegs.  In the former case, the return statement will return true, and in
         // the latter case false.
         while (!this.currentMomentSource.hasNext() && this.legSource.hasNext()) {
            this.currentLeg = this.legSource.next();
            this.currentMomentSource =
               this.currentLeg.toMomentIterator(this.nextTickStart, this.tickDuration);
         }

         return this.currentMomentSource.hasNext();
      }


      @Override
      public PathMoment next()
      {
         // The constructore and hasNext() invariants make it unnecessary to consult legSource here.  If currentMomentSource is
         // depleted at this instruction, we have already ensured that legSource is depleted as well.
         if (! this.currentMomentSource.hasNext()) { throw new NoSuchElementException(); }

         // Get the next moment to return. If it is the last of its PathLeg's content, read its timestamp and calculate
         // next PathLeg's stating point before losing reference on returning it.
         PathMoment thisMoment = this.currentMomentSource.next();
         if (! this.currentMomentSource.hasNext()) {
            this.nextTickStart = thisMoment.getTime() + this.tickDuration;
         }

         return thisMoment;
      }
      
      /**
       * Exhause the current iteration to produce a new Series which can be used to recreate the current iteration as needed.
       * 
       * @return
       */
      // TODO: Either drop this method if it has no callers, or figure out how to expose it through the return type of JourneyArcPathSeries.iterator().
      //       The emptyIterators it currently may return will lack a pruneHere() method.  Probably best bet is to create an interface that adds
      //       pruneHere() to Iterator, then create a special empty implementation to replace Collections.emptyIterator().
      public JourneyArcMomentSeries pruneHere() {
         if (! this.hasNext()) { throw new NoSuchElementException(); }
         
         final PathMoment initialMoment = this.next();
         final LinkedList<IPathLeg> newLegSource = new LinkedList<IPathLeg>();
         newLegSource.add(this.currentLeg.truncate(initialMoment.getTime()));
         this.legSource.forEachRemaining(leg -> newLegSource.add(leg));

         this.currentMomentSource = Collections.emptyIterator();

         return new JourneyArcMomentSeries(newLegSource, initialMoment, this.destination, this.tickDuration);
      }
   }
}
