package info.jchein.mesosphere.elevator.emulator.physics;


import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class JourneyArcMomentSeries
implements Iterable<PathMoment>
{
   private final Iterable<IPathLeg> journeyArc;
   private final double tickDuration;


   JourneyArcMomentSeries( final Iterable<IPathLeg> journeyArc, final double tickDuration )
   {
      this.journeyArc = journeyArc;
      this.tickDuration = tickDuration;
   }


   @Override
   public Iterator<PathMoment> iterator()
   {
      final Iterator<PathMoment> retVal;

      final Iterator<IPathLeg> legSource = this.journeyArc.iterator();
      if (legSource.hasNext()) {
         Iterator<PathMoment> iter = new JourneyArcPathMomentIterator(legSource, this.tickDuration);
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


   static class JourneyArcPathMomentIterator
   implements Iterator<PathMoment>
   {
      private final Iterator<IPathLeg> legSource;
      private IPathLeg currentLeg;
      private Iterator<PathMoment> currentMomentSource;
      private double tickDuration;
      private double nextTickStart;


      public JourneyArcPathMomentIterator( final Iterator<IPathLeg> legSource,
         final double tickDuration )
      {
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
         final PathMoment retVal = this.currentMomentSource.next();
         if (! this.currentMomentSource.hasNext()) {
            this.nextTickStart = retVal.getTime() + this.tickDuration;
         }

         return retVal;
      }
   }
}
