package net.minecraft.util;

public class SmoothDouble {
   private double targetValue;
   private double remainingValue;
   private double lastAmount;

   public double getNewDeltaValue(final double targetDelta, final double time) {
      this.targetValue += targetDelta;
      double delta = this.targetValue - this.remainingValue;
      double newLastAmount = Mth.lerp((double)0.5F, this.lastAmount, delta);
      double deltaSign = Math.signum(delta);
      if (deltaSign * delta > deltaSign * this.lastAmount) {
         delta = newLastAmount;
      }

      this.lastAmount = newLastAmount;
      this.remainingValue += delta * time;
      return delta * time;
   }

   public void reset() {
      this.targetValue = (double)0.0F;
      this.remainingValue = (double)0.0F;
      this.lastAmount = (double)0.0F;
   }
}
