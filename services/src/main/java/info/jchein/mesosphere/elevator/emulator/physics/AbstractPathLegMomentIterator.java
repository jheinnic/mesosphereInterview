package info.jchein.mesosphere.elevator.emulator.physics;

import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class AbstractPathLegMomentIterator
implements Iterator<PathMoment>
{
   protected final double finalTime;
   protected final double tickDuration;

   private double nextTime;
   private PathMoment pm;
   private PathMoment.PathMomentBuilder mb;

   // private double tickDegreeOne;
   // private double tickDegreeTwo;
   // private double accelerationDueToJerk;
   // private double velocityDueToJerk;
   // private double heightDueToJerk;

   AbstractPathLegMomentIterator(
      final PathMoment initialMoment, final double initialTickDuration, final double tickDuration, final double finalTime)
   {
      this.nextTime = initialMoment.getTime() + initialTickDuration;
      this.tickDuration = tickDuration;
      this.finalTime = finalTime;
      
      if ((this.nextTime <= this.finalTime)) {
         this.pm = initialMoment;

         // Use the non-existance of the model builder to branch into special case behavior that requires changing the initial
         // tick duration one time after returning the first sequence element, if any.
         this.setTickDuration(initialTickDuration);
         if (initialTickDuration == this.tickDuration) {
            this.mb = this.pm.toBuilder();
         } else {
            this.mb = null;
         }
      } else {
         this.pm = null;
      }
   }
   /*
    * { this.tickDegreeOne = tickDuration; this.tickDegreeTwo = ((tickDuration * tickDuration) / 2.0);
    * this.accelerationDueToJerk = this.pm.getJerk() * tickDuration; this.velocityDueToJerk =
    * ((this.accelerationDueToJerk * tickDuration) / 2.0); this.heightDueToJerk = ((this.velocityDueToJerk *
    * tickDuration) / 3.0); }
    */

   @Override
   public boolean hasNext()
   {
      return (this.pm != null);
   }

   @Override
   public PathMoment next()
   {
      if (this.pm == null) { throw new NoSuchElementException(); }
      
      final PathMoment retVal;
      if (this.mb == null) {
         this.mb = this.pm.toBuilder();
         retVal = this.doGetNext();

         // Set the post-initial tick duration now that we've produced the first value using the one-time
         // tick duration, if one was provided.
         this.setTickDuration(this.tickDuration);
      } else {
         retVal = this.doGetNext();
      }

      this.nextTime = this.nextTime + this.tickDuration;
      this.pm = (this.nextTime <= this.finalTime) ? retVal : null;

      return retVal;
   }
   
   private PathMoment doGetNext() {
      return this.mb.time(this.nextTime)
         .acceleration(this.getNextAcceleration())
         .velocity(this.getNextVelocity())
         .height(this.getNextHeight())
         .build();
   }
   
   double getCurrentAcceleration() { return this.pm.getAcceleration(); }
   double getCurrentVelocity() { return this.pm.getVelocity(); }
   double getCurrentHeight() { return this.pm.getHeight(); }

   abstract double getNextAcceleration();
   abstract double getNextVelocity();
   abstract double getNextHeight();

//   abstract double getTickDuration();
   abstract void setTickDuration(double tickDuration);

   /*
   private double getNextAcceleration()
   {
      double _acceleration = this.pm.getAcceleration();
      return (_acceleration + this.accelerationDueToJerk);
   }


   private double getNextVelocity()
   {
      double _velocity = this.pm.getVelocity();
      double _acceleration = this.pm.getAcceleration();
      double _multiply = (_acceleration * this.tickDegreeOne);
      double _plus = (_velocity + _multiply);
      return (_plus + this.velocityDueToJerk);
   }


   private double getNextHeight()
   {
      double _height = this.pm.getHeight();
      double _velocity = this.pm.getVelocity();
      double _multiply = (_velocity * this.tickDegreeOne);
      double _plus = (_height + _multiply);
      double _acceleration = this.pm.getAcceleration();
      double _multiply_1 = (_acceleration * this.tickDegreeTwo);
      double _plus_1 = (_plus + _multiply_1);
      return (_plus_1 + this.heightDueToJerk);
   }
   */
}
