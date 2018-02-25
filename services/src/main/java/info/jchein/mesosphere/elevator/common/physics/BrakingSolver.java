package info.jchein.mesosphere.elevator.common.physics;


import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.base.Preconditions;


public class BrakingSolver implements IBrakingSolver
{
   // Input Vector Indices
   private static final int IDX_T_0 = 0;
   private static final int IDX_T_1 = 1;

   // Output Vector Indices
   // private static final int IDX_V_2 = 0;
   // private static final int IDX_A_2 = 1;

   private static final EvaluationRmsChecker CONVERGENCE_CHECKER = new EvaluationRmsChecker(1e-9);

   private static final ParameterValidator PARAMETER_VALIDATOR = (RealVector input) -> {
      double t0 = input.getEntry(IDX_T_0);
      double t1 = input.getEntry(IDX_T_1);

      // If t0 or t1 is not a positive value, compensate by replacing the offending values with
      // a smallest possible positive input. The actual optimum values are not expected to be so
      // close to zero, so that this will safely prevent the optimizer from degenerating into
      // unusable altenate roots with negative time values without compromising its ability to
      // find a workable solution.
      //
      // If neither parameter estimate has slipped outside of the valid input domain of positive
      // values, simply return the original input array--no need to reallocate unless necessary.
      if (t0 <= 0) {
         t0 = 1e-12;
         if (t1 <= 0) {
            t1 = 1e-12;
         }
      } else if (t1 <= 0) {
         t1 = 1e-12;
      } else {
         return input;
      }

      return new ArrayRealVector(
         new double[]
         {
            t0, t1
      },
         false);
   };

   private static final LeastSquaresOptimizer OPTIMIZER = new GaussNewtonOptimizer();

   // Values given to problem statement from constructor arguments
   private final double v0;
   private final double v2;
   private final double j0;
   private final double j1;
   private final double s3;

   // Implicit zeros
   // private final double a0 = 0;
   // private final double v3 = 0;
   // private final double a3 = 0;
   // private final double s2 = 0;

   // Precise outputs derived direct formula from constructor arguments
   private final double t2;
   private final double j2;
   private final double a2;

   // Estimated outputs derived by least squares optimization from constructor arguments
   private final double t0;
   private final double t1;

   // Auxiliary outputs derived by direct formula from estimated outputs
   private final double v1;
   private final double a1;
   // private final double s0;
   // private final double s1;

   // Round trip validation computation of
   private final double cv2;
   private final double ca2;
   private final double cv3;
   private final double ca3;
   private final double cs3;

   public BrakingSolver( final double brakeDistance, final double brakeVelocity,
      final double initialVelocity, final double maxJerk )
   {
      Preconditions.checkArgument(brakeDistance > 0);
      Preconditions.checkArgument(maxJerk > 0);
      Preconditions.checkArgument(brakeVelocity != 0);

      // First, compute the initial acceleration required to be able to decelerate from the braking speed in the braking distance.  This will also
      // require calculating the required jerk and the duration it needs to be applied.  Use this result as target acceleration in the next step, 
      // working backwards to get to braking speed from travelling speed.
      // sf = s0 + v0*t + (a0*t^2)/2 + (j*t^3)/6
      // vf = v0 + a0*t + (j*t^2)/2
      // af = a0 + j*t
      // s0 = 0, sf = d(brk)
      // v0 = v(brk), vf = 0
      // a0 = a(min), af = 0
      // a0 = -j*t
      // v0 = 0 - a0*t - (j*t^2)/2
      // v0 = (j*t^2) - (j*t^2)/2 = (j*t^2)/2
      // d(brk) = 0 + ((j*t^2)/2)*t + (-j*t)*((t^2)/2) + (j*t^3)/6
      // d(brk) = ((j*t^3)/2) - ((j*t^3)/2) + (j*t^3)/6 = (j*t^3)/6
      // d(brk) = (v0*t)/3
      // t = 3 * d(brk) / v0
      // j = 6 * d(brk) / t^3
      // a0 = -j * t
      if (brakeVelocity < 0) {
         Preconditions.checkArgument(initialVelocity < brakeVelocity);
         this.j0 = maxJerk;
         this.j1 = -1 * maxJerk;
         this.t2 = -3 * brakeDistance / brakeVelocity;
         this.j2 = -6 * brakeDistance / t2 / t2 / t2;
      } else {
         Preconditions.checkArgument(initialVelocity > brakeVelocity);
         this.j0 = -1 * maxJerk;
         this.j1 = maxJerk;
         this.t2 = 3 * brakeDistance / brakeVelocity;
         this.j2 = 6 * brakeDistance / t2 / t2 / t2;
      }

      this.v0 = initialVelocity;
      this.v2 = brakeVelocity;
      this.s3 = brakeDistance;
      this.a2 = -1 * this.t2 * this.j2;

      // We have formulas to calculate the initial acceleration. constant jerk, and travel time that will allow us to go
      // from braking velocity to rest with a total travel distance equal to the braking distance.
      // We do not have formula for determining how to reach braking velocity at the required acceleration, but we can
      // safely presume existance of some maneuver consisting of a leg at maximimal jerk and another leg at minimal jerk
      // exists that will yield the desired result.
      // We have formulas that describe velocity and acceleration as a formula of the time intervals of these two legs.
      // We also have formula for the derivative of those velocity and acceleration formula with respect to each of the
      // time interval durations.
      // We want to find t0 and t1 such that the two motions at maximal jerk leave us at the braking velocity with the
      // acceleration value we determined will leave us at rest after travelling the brake distance.
      // We lack formulas for t0 and t1, but can use the above requirements to specify their values as the solution to a
      // Least Mean Squares optimization problem and derive a very good approximation of the desired values.
      final LeastSquaresBuilder bldr = new LeastSquaresBuilder();

      final LeastSquaresProblem problem =
         bldr.model(new BrakingValueFunction(v0, j0, j1), new BrakingJacobianFunction(j0, j1))
            .maxEvaluations(Integer.MAX_VALUE)
            .maxIterations(Integer.MAX_VALUE)
            .parameterValidator(PARAMETER_VALIDATOR)
            .lazyEvaluation(true)
            .start(new ArrayRealVector(new double[] { this.t2, this.t2 }, false))
            .target(new ArrayRealVector(new double[] { this.v2, this.a2 }, false))
            .checker(CONVERGENCE_CHECKER)
            .build();
      final LeastSquaresOptimizer.Optimum optimum = OPTIMIZER.optimize(problem);
      final RealVector solution = optimum.getPoint();

      this.t0 = solution.getEntry(IDX_T_0);
      this.t1 = solution.getEntry(IDX_T_1);
      this.v1 = this.v0 + (this.j0 * this.t0 * this.t0 / 2.0);
      this.a1 = this.j0 * this.t0;

      this.cv2 = this.v1 + (this.a1 * this.t1) + (this.j1 * this.t1 * this.t1 / 2.0);
      this.ca2 = this.a1 + (this.j1 * this.t1);
      this.cv3 = this.cv2 + (this.ca2 * this.t2) + (this.j2 * this.t2 * this.t2 / 2.0);
      this.ca3 = this.ca2 + (this.j2 * this.t2);
      this.cs3 =
         (this.cv2 * this.t2) +
            (this.ca2 * this.t2 * this.t2 / 2.0) + (this.j2 * this.t2 * this.t2 * this.t2 / 6.0);
   }
   
   
   @Override
   public boolean isWithinTolerance(double epsilon) {
      boolean retVal = true;
      if (Math.abs(this.cv2 - this.v2) > epsilon) {
         retVal = false;
      } else if(Math.abs(this.ca2 - this.a2) > epsilon) {
         retVal = false;
      } else if(Math.abs(this.ca3) > epsilon) {
         retVal = false;
      } else if(Math.abs(this.cv3) > epsilon) {
         retVal = false;
      } else if(Math.abs(this.cs3 - this.s3) > epsilon) {
         retVal = false;
      }
      
      return retVal;
   }
   
   @Override
   public double getTimeZero() {
      return this.t0;
   }
   
   @Override
   public double getTimeOne() {
      return this.t1;
   }
   
   @Override
   public double getTimeTwo() {
      return this.t2;
   }
   
   @Override
   public double getJerkTwo() {
      return this.j2;
   }
   
   @Override
   public double getJerkZero() {
      return this.j0;
   }

   @Override
   public double getJerkOne() {
      return this.j1;
   }
   
   @Override
   public double getVelocityOne() {
      return this.v1;
   }
   
   @Override
   public double getVelocityTwo() {
      return this.v2;
   }
   
   @Override
   public double getAccelerationOne() {
      return this.a1;
   }
   
   @Override
   public double getAccelerationTwo() {
      return this.a2;
   }

   public static class BrakingValueFunction
   implements MultivariateVectorFunction
   {
      private double v0;
      private double j0;
      private double j1;


      public BrakingValueFunction( double v0, double j0, double j1 )
      {
         this.v0 = v0;
         this.j0 = j0;
         this.j1 = j1;
      }


      @Override
      public double[] value(double[] point) throws IllegalArgumentException
      {
         final double t0 = point[IDX_T_0];
         final double t1 = point[IDX_T_1];

         return new double[] {
            this.computeV2(t0, t1), this.computeA2(t0, t1)
         };
      }


      private double computeV2(final double t0, final double t1)
      {
         return this.v0 + (this.j0 * t0 * t0 / 2.0) + (this.j0 * t0 * t1) + (this.j1 * t1 * t1 / 2.0);
      }


      private double computeA2(final double t0, final double t1)
      {
         return (this.j0 * t0) + (this.j1 * t1);
      }
   }


   public static class BrakingJacobianFunction
   implements MultivariateMatrixFunction
   {
      private double j0;
      private double j1;


      public BrakingJacobianFunction( double j0, double j1 )
      {
         this.j0 = j0;
         this.j1 = j1;
      }


      @Override
      public double[][] value(double[] point) throws IllegalArgumentException
      {
         final double t0 = point[IDX_T_0];
         final double t1 = point[IDX_T_1];

         return new double[][] {
            { this.computeV2dT0(t0, t1), this.j0 },
            { this.computeV2dT1(t0, t1), this.j1 }
         };
      }


      private double computeV2dT0(final double t0, final double t1)
      {
         return (this.j0 * t0) + (this.j0 * t1);
      }


      private double computeV2dT1(final double t0, final double t1)
      {
         return (this.j0 * t0) + (this.j1 * t1);
      }
   }
}
