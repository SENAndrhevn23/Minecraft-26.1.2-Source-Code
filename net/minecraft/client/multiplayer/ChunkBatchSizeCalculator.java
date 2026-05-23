package net.minecraft.client.multiplayer;

import net.minecraft.util.Mth;
import net.minecraft.util.Util;

public class ChunkBatchSizeCalculator {
   private static final int MAX_OLD_SAMPLES_WEIGHT = 49;
   private static final int CLAMP_COEFFICIENT = 3;
   private double aggregatedNanosPerChunk = (double)2000000.0F;
   private int oldSamplesWeight = 1;
   private volatile long chunkBatchStartTime = Util.getNanos();

   public void onBatchStart() {
      this.chunkBatchStartTime = Util.getNanos();
   }

   public void onBatchFinished(final int batchSize) {
      if (batchSize > 0) {
         double batchDuration = (double)(Util.getNanos() - this.chunkBatchStartTime);
         double nanosPerChunk = batchDuration / (double)batchSize;
         double clampedNanosPerChunk = Mth.clamp(nanosPerChunk, this.aggregatedNanosPerChunk / (double)3.0F, this.aggregatedNanosPerChunk * (double)3.0F);
         this.aggregatedNanosPerChunk = (this.aggregatedNanosPerChunk * (double)this.oldSamplesWeight + clampedNanosPerChunk) / (double)(this.oldSamplesWeight + 1);
         this.oldSamplesWeight = Math.min(49, this.oldSamplesWeight + 1);
      }

   }

   public float getDesiredChunksPerTick() {
      return (float)((double)7000000.0F / this.aggregatedNanosPerChunk);
   }
}
