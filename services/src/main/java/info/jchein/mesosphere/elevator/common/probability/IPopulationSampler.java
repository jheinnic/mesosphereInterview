package info.jchein.mesosphere.elevator.common.probability;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Range;

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
   
   /**
    * Returns a value from modeled population corresponding to a pair of probability values.  The first 
    * probability value is used to identify which subgrop the returned value should be sampled from, and
    * the second value is used as the probability when sampling the chosen subgroup's RealDistribution.
    * 
    * @param groupProb Probability value used to identify what subgroup to sample the return value from.
    * @param distProb Probability value used to sample the distribution of the subgroup mapped from 
    * {@code groupProb}.
    * @return A population value indexed by the pair of probability arguments against population model.
    */
   double lookup(@Range(min=0, max=1) double groupProb, @Range(min=0, max=1) double distProb);
}
