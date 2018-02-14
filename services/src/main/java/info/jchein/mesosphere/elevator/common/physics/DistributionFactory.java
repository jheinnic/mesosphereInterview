package info.jchein.mesosphere.elevator.common.physics;

public class DistributionFactory
implements IDistributionFactory
{
   ExponentialDistribution getExponentialDistribution(double mean, double error);
}
