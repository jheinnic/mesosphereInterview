package info.jchein.mesosphere.elevator.common.physics;


public interface IBrakingSolver
{

   boolean isWithinTolerance(double epsilon);


   double getTimeZero();


   double getTimeOne();


   double getTimeTwo();


   double getJerkTwo();


   double getJerkZero();


   double getJerkOne();


   double getVelocityOne();


   double getVelocityTwo();


   double getAccelerationOne();


   double getAccelerationTwo();

}
