package info.jchein.mesosphere.elevator.common.physics;

/**
 * Represents a value sampled from a distribution model that involves N subgroups, each with it own
 * RealDistribution model, where P(i) gives probability that any given individual will belong to subgroup i,
 * and so will have a value for an instance of this class's real value taken from the real distribution for 
 * subgroup i.
 * 
 * In practice, sampling a value from a "Popuplation" made of subgroups defined this way is a two step 
 * process, where one first samples the subgroup according to their relative sizes to select a specific
 * subgroup's value distribution, then sample a value from that to return to the caller.
 * 
 * See IDistributionFactory for a helper method that can derive an IPopulationSampler given a set of
 * RealDistributions and their relative likelihood density weights.
 * 
 * @author jheinnic
 *
 */
public interface IPopulationSampler
{
   double sample();
}
