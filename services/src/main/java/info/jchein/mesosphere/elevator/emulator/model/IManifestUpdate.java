package info.jchein.mesosphere.elevator.emulator.model;

public interface IManifestUpdate
{
   public boolean disembark(double weight);
   public boolean board(double weight);
   public void requestStop(int floorIndex);
}
