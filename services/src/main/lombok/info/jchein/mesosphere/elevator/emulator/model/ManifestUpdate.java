package info.jchein.mesosphere.elevator.emulator.model;

import javax.validation.constraints.Min;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ValidateOnExecution(type= {ExecutableType.ALL})
public class ManifestUpdate
implements IManifestUpdate
{
   private final IElevatorCarPort carPort;

   private double maximumWeight;
   private double initialWeight;
   private double loWeight;
   private double hiWeight;
   private double finalWeight;


   @Autowired
   ManifestUpdate(IElevatorCarPort carPort) {
      this.carPort = carPort;
      this.maximumWeight = this.carPort.getMaximumWeightLoad();
      this.initialWeight = this.carPort.getCurrentWeightLoad();
      this.loWeight = this.initialWeight;
      this.hiWeight = this.initialWeight;
      this.finalWeight = this.initialWeight;
   }
   
   @Override
   public void disembark(@Positive double weight)
   {
      Preconditions.checkArgument(weight <= this.finalWeight, "Cannot disembark to negative weight");
      final double initialWeight = this.finalWeight;
      this.finalWeight -= weight;
      if (this.finalWeight < this.loWeight) {
         this.loWeight = this.finalWeight;
      }
      this.carPort.updateWeightLoad(initialWeight, weight, this.finalWeight);
   }


   @Override
   public boolean board(@Positive double weight)
   {
      final double initialWeight = this.finalWeight;
      final double potentialNextWeight = this.finalWeight + weight;
      if (potentialNextWeight > this.maximumWeight) {
         log.info("Rejected passenger that would require weight load capacity of {}", potentialNextWeight);
         return false;
      }
      
      this.finalWeight = potentialNextWeight;
      if (potentialNextWeight > this.hiWeight) {
         this.hiWeight = potentialNextWeight;
      }
      this.carPort.updateWeightLoad(initialWeight, weight, this.finalWeight);

      return true;
   }


   @Override
   public void requestDropOff(@Min(0) int dropOffFloorIndex)
   {
      this.carPort.dropOffRequested(dropOffFloorIndex);
   }
   
   public void post() {
//      this.carPort.updateWeightLoad(this.initialWeight, this.loWeight, this.hiWeight, this.finalWeight);
   }
}
