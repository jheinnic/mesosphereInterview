package test;


import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;


public class TryLeastSquares
{
   // Input Vector Indices
   public static final int IDX_T_0 = 0;
   public static final int IDX_T_1 = 1;
   public static final int IDX_V_1 = 2;
   public static final int IDX_A_1 = 3;

   // Output Vector Indices
   public static final int IDX_V_0 = 0;
   public static final int IDX_J_0 = 1;
   public static final int IDX_J_1 = 2;
   public static final int IDX_V_2 = 3;


   public static void main(String[] args)
   {
      // s3 - s2 = dbrk (braking distance)
      final double dBrk = 0.4;
      // (braking speed)
      final double vBrk = 0.8;
      // (maximum velocity)
      final double vMax = 2.5;
      final double vMin = -2.5;
      final double aMin = -2.0;
      final double aMax = 2.0;

      final double v0 = vMax;
      final double v2 = vBrk;
      final double a0 = 0;
      final double j0 = -2.0;
      final double j1 = 2.0;
      final double s2 = 0;
      final double s3 = dBrk;

      System.out.println(
         String.format(
            "v0 = %f, a0 = %f, j0 = %f, j1 = %f, s2 = %f, v2 = %f, s3 = %f\n",
            v0,
            a0,
            j0,
            j1,
            s2,
            v2,
            s3));
      final double t2 = 3 * dBrk / vBrk;
      final double j2 = 6 * dBrk / t2 / t2 / t2;
      final double a2 = -1 * t2 * j2;
      final double altA2 = -2 * vBrk / t2;

      System.out.println(String.format("t2 = %f, j2 = %f, a2 = %f, altA2 = %f\n", t2, j2, a2, altA2));

      final double cs3 = s2 + (v2 * t2) + (a2 * t2 * t2 / 2.0) + (j2 * t2 * t2 * t2 / 6.0);
      final double errS3 = Math.abs(cs3 - s3);
      System.out.println(String.format("s3 = %f, compS3 = %f, absErrS3 = %f", s3, cs3, errS3));

      final ArrayRealVector targetOutputs =
         new ArrayRealVector(new double[] {v0, j0, j1, v2}, false);
      final ArrayRealVector initialInputs =
         new ArrayRealVector(new double[] {20, 20, vMax, aMin}, false);

      double it0 = initialInputs.getEntry(IDX_T_0);
      double it1 = initialInputs.getEntry(IDX_T_1);
      double iv1 = initialInputs.getEntry(IDX_V_1);
      double ia1 = initialInputs.getEntry(IDX_A_1);

      System.out.println(String.format("it0 = %f, iv1 = %f, ia1 = %f, it1 = %f\n", it0, iv1, ia1, it1));

      ParameterValidator pValidator = (RealVector input) -> {
         double t0 = input.getEntry(IDX_T_0);
         double t1 = input.getEntry(IDX_T_1);
         double v1 = input.getEntry(IDX_V_1);
         double a1 = input.getEntry(IDX_A_1);
         
         if (t0 <= 0) { t0 = 1e-12; }
         if (t1 <= 0) { t1 = 1e-12; }
         if (v1 < vMin) { v1 = vMin; }
         if (v1 > vMax) { v1 = vMax; }
         if (a1 < aMin) { a1 = aMin; }
         if (a1 > aMax) { a1 = aMax; }

         return new ArrayRealVector(new double[] {t0, t1, v1, a1}, false);
      };

      LeastSquaresBuilder bldr = new LeastSquaresBuilder();
      MultivariateJacobianFunction mjf = new BrakingFunction(a2);
      EvaluationRmsChecker convChecker = new EvaluationRmsChecker(0.05);
      LeastSquaresProblem problem =
         bldr.model(mjf)
            .maxEvaluations(Integer.MAX_VALUE)
            .maxIterations(Integer.MAX_VALUE)
            .parameterValidator(pValidator)
            .start(initialInputs)
            .target(targetOutputs)
            .checker(convChecker)
            .build();
      LeastSquaresOptimizer optimizer =
         new GaussNewtonOptimizer().withDecomposition(GaussNewtonOptimizer.Decomposition.QR);
      // LeastSquaresOptimizer optimizer =
      // new LevenbergMarquardtOptimizer().withInitialStepBoundFactor(0.1).withCostRelativeTolerance(1.0e-12)
      // .withParameterRelativeTolerance(1.0e-12);
      LeastSquaresOptimizer.Optimum optimum = optimizer.optimize(problem);
      RealVector solution = optimum.getPoint();

      double t0 = solution.getEntry(IDX_T_0);
      double t1 = solution.getEntry(IDX_T_1);
      double v1 = solution.getEntry(IDX_V_1);
      double a1 = solution.getEntry(IDX_A_1);

      System.out.println(String.format("t0 = %f, v1 = %f, a1 = %f, t1 = %f\n", t0, v1, a1, t1));

      RealVector computed =
         mjf.value(solution)
            .getFirst();

      double cV0 = computed.getEntry(IDX_V_0);
      double cJ0 = computed.getEntry(IDX_J_0);
      double cJ1 = computed.getEntry(IDX_J_1);
      double cV2 = computed.getEntry(IDX_V_2);

      System.out.println(String.format("cv0 = %f, cj0 = %f, cj1 = %f, cv2 = %f\n", cV0, cJ0, cJ1, cV2));

      System.out.println("RMS: " + optimum.getRMS());
      System.out.println("evaluations: " + optimum.getEvaluations());
      System.out.println("iterations: " + optimum.getIterations());

      System.out.println(
         mjf.value(solution)
            .getSecond());
   }


   public static class BrakingFunction
   implements MultivariateJacobianFunction
   {

      private double a2;


      public BrakingFunction( double a2 )
      {
         this.a2 = a2;
      }


      @Override
      public Pair<RealVector, RealMatrix> value(RealVector point)
      {
         double t0 = point.getEntry(IDX_T_0);
         double t1 = point.getEntry(IDX_T_1);
         double v1 = point.getEntry(IDX_V_1);
         double a1 = point.getEntry(IDX_A_1);

         ArrayRealVector outputs =
            new ArrayRealVector(new double[]
            {
               this.computeV0(t0, t1, v1, a1), this.computeJ0(t0, t1, v1, a1),
               this.computeJ1(t0, t1, v1, a1), this.computeV2(t0, t1, v1, a1)
            }, false);

         Array2DRowRealMatrix derivatives =
            new Array2DRowRealMatrix(new double[][]
            {
               {
                  this.computeV0dT0(t0, t1, v1, a1), this.computeJ0dT0(t0, t1, v1, a1),
                  this.computeJ1dT0(t0, t1, v1, a1), this.computeV2dT0(t0, t1, v1, a1)
               }, {
                  this.computeV0dT1(t0, t1, v1, a1), this.computeJ0dT1(t0, t1, v1, a1),
                  this.computeJ1dT1(t0, t1, v1, a1), this.computeV2dT1(t0, t1, v1, a1)
               }, {
                  this.computeV0dV1(t0, t1, v1, a1), this.computeJ0dV1(t0, t1, v1, a1),
                  this.computeJ1dV1(t0, t1, v1, a1), this.computeV2dV1(t0, t1, v1, a1)
               }, {
                  this.computeV0dA1(t0, t1, v1, a1), this.computeJ0dA1(t0, t1, v1, a1),
                  this.computeJ1dA1(t0, t1, v1, a1), this.computeV2dA1(t0, t1, v1, a1)
               }
            }, false);

         return new Pair<RealVector, RealMatrix>(outputs, derivatives);
      }


      private double computeV0(final double t0, final double t1, final double v1, final double a1)
      {
         return v1 - ((a1 * t0) / 2.0);
      }


      private double computeJ0(final double t0, final double t1, final double v1, final double a1)
      {
         return a1 / t0;
      }


      private double computeJ1(final double t0, final double t1, final double v1, final double a1)
      {
         return (this.a2 - a1) / t1;
      }


      private double computeV2(final double t0, final double t1, final double v1, final double a1)
      {
         return v1 + ((a1 * t1) / 2.0) + ((this.a2 * t1) / 2.0);
      }


      private double computeV0dT0(final double t0, final double t1, final double v1, final double a1)
      {
         return a1 / -2.0;
      }


      private double computeJ0dT0(final double t0, final double t1, final double v1, final double a1)
      {
         return -1.0 * a1 / t0 / t0;
      }


      private double computeJ1dT0(final double t0, final double t1, final double v1, final double a1)
      {
         return 0.0;
      }


      private double computeV2dT0(final double t0, final double t1, final double v1, final double a1)
      {
         return 0.0;
      }


      private double computeV0dT1(final double t0, final double t1, final double v1, final double a1)
      {
         return 0.0;
      }


      private double computeJ0dT1(final double t0, final double t1, final double v1, final double a1)
      {
         return 0.0;
      }


      private double computeJ1dT1(final double t0, final double t1, final double v1, final double a1)
      {
         return (a1 / t1 / t1) - (this.a2 / t1 / t1);
      }


      private double computeV2dT1(final double t0, final double t1, final double v1, final double a1)
      {
         return ((a1 + this.a2) / 2.0);
      }


      private double computeV0dV1(final double t0, final double t1, final double v1, final double a1)
      {
         return 1.0;
      }


      private double computeJ0dV1(final double t0, final double t1, final double v1, final double a1)
      {
         return 0.0;
      }


      private double computeJ1dV1(final double t0, final double t1, final double v1, final double a1)
      {
         return 0.0;
      }


      private double computeV2dV1(final double t0, final double t1, final double v1, final double a1)
      {
         return 1.0;
      }


      private double computeV0dA1(final double t0, final double t1, final double v1, final double a1)
      {
         return t0 / -2.0;
      }


      private double computeJ0dA1(final double t0, final double t1, final double v1, final double a1)
      {
         return 1.0 / t0;
      }


      private double computeJ1dA1(final double t0, final double t1, final double v1, final double a1)
      {
         return -1.0 / t1;
      }


      private double computeV2dA1(final double t0, final double t1, final double v1, final double a1)
      {
         return t1 / 2.0;
      }
   }
}
