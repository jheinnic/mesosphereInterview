package info.jchein.mesosphere.test_support.fixtures.physics;


import info.jchein.mesosphere.elevator.emulator.physics.ConstantVelocityPathLeg;
import info.jchein.mesosphere.elevator.emulator.physics.PathMoment;


public class PhysicsDataFixture
{
   public static final ConstantVelocityPathLeg A =
      new ConstantVelocityPathLeg(
         PathMoment.build( bldr -> { bldr.acceleration(5.0); }), 2.0);
}
