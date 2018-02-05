package info.jchein.mesosphere.elevator.common.graph;

/**
 * There is an UndirectedFloorAdjacency node for every pair of floors that share a cieling or floor.  The property names call out which is higher and
 * which is lower in order to make the addressing unambiguous.
 * 
 * There will be twice as many of instances of this class for any building than there will be of DirectedFloorAdjacency.  For example, there is a 
 * UndirectedFloorAdjacency(1, 2) that corresponds to DirectedFloorAdjacency(1, 2) and DirectedFloorAdjacency(2, 1).
 * 
 * There will also be just as many instances of UndirectedFloorAdjacency as there are of AscendingFloorAdjaceny and DescendingFloorAdjacency.  This class
 * could often be used interchangably with either of those classes, but the latter two should be reserved for cases that need to include cosideration for 
 * direction in their enumeration of floor pairs, but need to treat ascent and descent distinctly.  An example problem calling for the latter usage might 
 * involve addressing vertex pairs as reversal points.  Such a problem would need to recognize (C -> D, F -> E) as a valid circuit definition, while also
 * rejecting (D -> C, F -> E); (C -> D, E -> F), and (D -> C, E -> F) as non-cycle-defining, without also excluding (A -> B, D -> C) or (E -> F, G -> F).
 * 
 * @author jheinnic
 *
 */
public class UndirectedAdjacency
{
   int lowerFloor;
   int higherFloor;
}
