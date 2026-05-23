package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.jspecify.annotations.Nullable;

public class PerlinSimplexNoise {
   private final @Nullable SimplexNoise[] noiseLevels;
   private final double highestFreqValueFactor;
   private final double highestFreqInputFactor;

   public PerlinSimplexNoise(final RandomSource random, final List<Integer> octaveSet) {
      this(random, new IntRBTreeSet(octaveSet));
   }

   private PerlinSimplexNoise(final RandomSource random, final IntSortedSet octaveSet) {
      if (octaveSet.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int lowFreqOctaves = -octaveSet.firstInt();
         int highFreqOctaves = octaveSet.lastInt();
         int octaves = lowFreqOctaves + highFreqOctaves + 1;
         if (octaves < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            SimplexNoise zeroOctave = new SimplexNoise(random);
            int zeroOctaveIndex = highFreqOctaves;
            this.noiseLevels = new SimplexNoise[octaves];
            if (highFreqOctaves >= 0 && highFreqOctaves < octaves && octaveSet.contains(0)) {
               this.noiseLevels[highFreqOctaves] = zeroOctave;
            }

            for(int i = highFreqOctaves + 1; i < octaves; ++i) {
               if (i >= 0 && octaveSet.contains(zeroOctaveIndex - i)) {
                  this.noiseLevels[i] = new SimplexNoise(random);
               } else {
                  random.consumeCount(262);
               }
            }

            if (highFreqOctaves > 0) {
               long positiveOctaveSeed = (long)(zeroOctave.getValue(zeroOctave.xo, zeroOctave.yo, zeroOctave.zo) * (double)Long.MAX_VALUE);
               RandomSource highFreqRandom = new WorldgenRandom(new LegacyRandomSource(positiveOctaveSeed));

               for(int i = zeroOctaveIndex - 1; i >= 0; --i) {
                  if (i < octaves && octaveSet.contains(zeroOctaveIndex - i)) {
                     this.noiseLevels[i] = new SimplexNoise(highFreqRandom);
                  } else {
                     highFreqRandom.consumeCount(262);
                  }
               }
            }

            this.highestFreqInputFactor = Math.pow((double)2.0F, (double)highFreqOctaves);
            this.highestFreqValueFactor = (double)1.0F / (Math.pow((double)2.0F, (double)octaves) - (double)1.0F);
         }
      }
   }

   public double getValue(final double x, final double y, final boolean useNoiseStart) {
      double value = (double)0.0F;
      double factor = this.highestFreqInputFactor;
      double valueFactor = this.highestFreqValueFactor;

      for(SimplexNoise noiseLevel : this.noiseLevels) {
         if (noiseLevel != null) {
            value += noiseLevel.getValue(x * factor + (useNoiseStart ? noiseLevel.xo : (double)0.0F), y * factor + (useNoiseStart ? noiseLevel.yo : (double)0.0F)) * valueFactor;
         }

         factor /= (double)2.0F;
         valueFactor *= (double)2.0F;
      }

      return value;
   }
}
