package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MarsagliaPolarGaussian {
   public final RandomSource randomSource;
   private double nextNextGaussian;
   private boolean haveNextNextGaussian;

   public MarsagliaPolarGaussian(final RandomSource randomSource) {
      this.randomSource = randomSource;
   }

   public void reset() {
      this.haveNextNextGaussian = false;
   }

   public double nextGaussian() {
      if (this.haveNextNextGaussian) {
         this.haveNextNextGaussian = false;
         return this.nextNextGaussian;
      } else {
         double x;
         double y;
         double radiusSquared;
         do {
            x = (double)2.0F * this.randomSource.nextDouble() - (double)1.0F;
            y = (double)2.0F * this.randomSource.nextDouble() - (double)1.0F;
            radiusSquared = Mth.square(x) + Mth.square(y);
         } while(radiusSquared >= (double)1.0F || radiusSquared == (double)0.0F);

         double multiplier = Math.sqrt((double)-2.0F * Math.log(radiusSquared) / radiusSquared);
         this.nextNextGaussian = y * multiplier;
         this.haveNextNextGaussian = true;
         return x * multiplier;
      }
   }
}
