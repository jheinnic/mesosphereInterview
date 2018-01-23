package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

public interface IHallPanelDriver {
   void pickupRequestCleared(DirectionOfTravel direction);
   void invitePassengersToBoard(DirectionOfTravel direction, int carIndex);
   void withdrawInvitationToBoard(DirectionOfTravel direction, int carIndex);
}
