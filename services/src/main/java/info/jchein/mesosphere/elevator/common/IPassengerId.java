package info.jchein.mesosphere.elevator.common;

/**
 * Memento object representing a boarded passenger and used to refer to same when disembarking on arrival.
 * 
 * PassengerId's do have a meaningful toString() method to support use with audit logging, but the value returned is intended
 * to be considered opaque.  Comparing two PassengerId.toString() outputs has no greater value than passing one artifact to
 * the others Object.equals() method, which will likely perform the comparison more efficiently and with less possibility of 
 * false positives/negatives induced by comparing stringified PassengerId's to any other stringified concept found.
 * 
 * @author jheinnic
 */
public interface IPassengerId
{
}
