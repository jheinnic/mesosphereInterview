package info.jchein.mesosphere.elevator.common.graph;

/**
 * A common marker interface shared by the before-arrival and after-departure pseudonodes that serve as gloal source and global sink, respectively.
 * 
 * No method is offered here to distinguish which endpoint a terminal node represents because that informaation is already embedded in the inheritted
 * getPathNodeType()'s return enum.
 * 
 * @author jheinnic
 */
public interface TravellingEndpoint extends TravellingPathNode
{

}
