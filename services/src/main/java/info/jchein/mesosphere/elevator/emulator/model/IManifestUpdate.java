package info.jchein.mesosphere.elevator.emulator.model;

public interface IManifestUpdate
{
   public void disembark(double weight);
   public boolean board(double weight);
   public void requestDropOff(int dropOffFloorIndex);
}
