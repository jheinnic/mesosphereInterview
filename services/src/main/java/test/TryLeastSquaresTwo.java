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


public class TryLeastSquaresTwo
{
   // Input Vector Indices
   public static final int IDX_T_0 = 0;
   public static final int IDX_T_1 = 1;

   // Output Vector Indices
   public static final int IDX_V_2 = 0;
   public static final int IDX_A_2 = 1;


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
         String.format("v0 = %f, a0 = %f, j0 = %f, j1 = %f, s2 = %f, v2 = %f, s3 = %f\n", v0, a0, j0, j1, s2, v2, s3));
      final double t2 = 3 * dBrk / vBrk;
      final double j2 = 6 * dBrk / t2 / t2 / t2;
      final double a2 = -1 * t2 * j2;
      final double altA2 = -2 * vBrk / t2;

      System.out.println(String.format("t2 = %f, j2 = %f, a2 = %f, altA2 = %f\n", t2, j2, a2, altA2));

      final double cs3 = s2 + (v2 * t2) + (a2 * t2 * t2 / 2.0) + (j2 * t2 * t2 * t2 / 6.0);
      final double errS3 = Math.abs(cs3 - s3);
      System.out.println(String.format("s3 = %f, compS3 = %f, absErrS3 = %f", s3, cs3, errS3));

      final ArrayRealVector targetOutputs =
         new ArrayRealVector(new double[] {v2, a2}, false);
      final ArrayRealVector initialInputs =
         new ArrayRealVector(new double[] {20, 20}, false);

      double it0 = initialInputs.getEntry(IDX_T_0);
      double it1 = initialInputs.getEntry(IDX_T_1);

      System.out.println(String.format("it0 = %f, it1 = %f\n", it0, it1));

      ParameterValidator pValidator = (RealVector input) -> {
         double t0 = input.getEntry(IDX_T_0);
         double t1 = input.getEntry(IDX_T_1);
         
         if (t0 <= 0) { t0 = 1e-12; }
         if (t1 <= 0) { t1 = 1e-12; }

         return new ArrayRealVector(new double[] {t0, t1}, false);
      };

      LeastSquaresBuilder bldr = new LeastSquaresBuilder();
      MultivariateJacobianFunction mjf = new BrakingFunction(v0, j0, j1);
      EvaluationRmsChecker convChecker = new EvaluationRmsChecker(1e-9);
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

      System.out.println(String.format("t0 = %f, t1 = %f\n", t0, t1));

//      RealVector computed =
//         mjf.value(solution)
//            .getFirst();

//      double cV2 = computed.getEntry(IDX_V_2);
//      double cA2 = computed.getEntry(IDX_A_2);
//      System.out.println(String.format("cv2 = %f, ca2 = %f\n", cV2, cA2));

      System.out.println("RMS: " + optimum.getRMS());
      System.out.println("evaluations: " + optimum.getEvaluations());
      System.out.println("iterations: " + optimum.getIterations());

      System.out.println(
         mjf.value(solution)
            .getSecond());
      
      double cv1 = v0 + (j0 * t0 * t0 / 2);
      double ca1 = j0 * t0;
      double cv2 = cv1 + (ca1 * t1) + (j1 * t1 * t1 / 2);
      double ca2 = ca1 + (j1 * t1);
      double cv3 = cv2 + (ca2 * t2) + (j2 * t2 * t2 / 2);
      double ca3 = ca2 + (j2 * t2);
      double cs3b = (cv2 * t2) + (ca2 * t2 * t2 / 2) + (j2 * t2 * t2 * t2 / 6);
      
      System.out.println(
         String.format("cv1 = %f, ca1 = %f, cv2 = %f, ca2 = %f, cv3 = %f, ca3 = %f, cs3 = %f\n",
            cv1, ca1, cv2, ca2, cv3, ca3, cs3b));
   }


   public static class BrakingFunction
   implements MultivariateJacobianFunction
   {

      private double v0;
      private double j0;
      private double j1;


      public BrakingFunction( double v0, double j0, double j1 )
      {
         this.v0 = v0;
         this.j0 = j0;
         this.j1 = j1;
      }


      @Override
      public Pair<RealVector, RealMatrix> value(RealVector point)
      {
         double t0 = point.getEntry(IDX_T_0);
         double t1 = point.getEntry(IDX_T_1);

         ArrayRealVector outputs =
            new ArrayRealVector(new double[]
            {
               this.computeV2(t0, t1), this.computeA2(t0, t1)
            }, false);

         Array2DRowRealMatrix derivatives =
            new Array2DRowRealMatrix(new double[][]
            {
               {
                  this.computeV2dT0(t0, t1), this.computeA2dT0(t0, t1)
               }, {
                  this.computeV2dT1(t0, t1), this.computeA2dT1(t0, t1)
               }
            }, false);

         return new Pair<RealVector, RealMatrix>(outputs, derivatives);
      }


      private double computeV2(final double t0, final double t1)
      {
         return this.v0 + (this.j0 * t0 * t0 / 2.0) + (this.j0 * t0 * t1) + (this.j1 * t1 * t1 / 2.0);
      }


      private double computeA2(final double t0, final double t1)
      {
         return (this.j0 * t0) + (this.j1 * t1);
      }


      private double computeV2dT0(final double t0, final double t1)
      {
         return (this.j0 * t0) + (this.j0 * t1);
      }


      private double computeV2dT1(final double t0, final double t1)
      {
         return (this.j0 * t0) + (this.j1 * t1);
      }


      private double computeA2dT0(final double t0, final double t1)
      {
         return this.j0;
      }


      private double computeA2dT1(final double t0, final double t1)
      {
         return this.j1;
      }
   }
}
