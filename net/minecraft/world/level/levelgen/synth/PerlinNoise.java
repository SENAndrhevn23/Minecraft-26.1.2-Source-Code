package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jspecify.annotations.Nullable;

public class PerlinNoise {
   private static final int ROUND_OFF = 33554432;
   private final @Nullable ImprovedNoise[] noiseLevels;
   private final int firstOctave;
   private final DoubleList amplitudes;
   private final double lowestFreqValueFactor;
   private final double lowestFreqInputFactor;
   private final double maxValue;

   /** @deprecated */
   @Deprecated
   public static PerlinNoise createLegacyForBlendedNoise(final RandomSource random, final IntStream octaves) {
      return new PerlinNoise(random, makeAmplitudes(new IntRBTreeSet((Collection)octaves.boxed().collect(ImmutableList.toImmutableList()))), false);
   }

   /** @deprecated */
   @Deprecated
   public static PerlinNoise createLegacyForLegacyNetherBiome(final RandomSource random, final int firstOctave, final DoubleList amplitudes) {
      return new PerlinNoise(random, Pair.of(firstOctave, amplitudes), false);
   }

   public static PerlinNoise create(final RandomSource random, final IntStream octaves) {
      return create(random, (List)octaves.boxed().collect(ImmutableList.toImmutableList()));
   }

   public static PerlinNoise create(final RandomSource random, final List<Integer> octaveSet) {
      return new PerlinNoise(random, makeAmplitudes(new IntRBTreeSet(octaveSet)), true);
   }

   public static PerlinNoise create(final RandomSource random, final int firstOctave, final double firstAmplitude, final double... amplitudes) {
      DoubleArrayList amplitudeList = new DoubleArrayList(amplitudes);
      amplitudeList.add(0, firstAmplitude);
      return new PerlinNoise(random, Pair.of(firstOctave, amplitudeList), true);
   }

   public static PerlinNoise create(final RandomSource random, final int firstOctave, final DoubleList amplitudes) {
      return new PerlinNoise(random, Pair.of(firstOctave, amplitudes), true);
   }

   private static Pair<Integer, DoubleList> makeAmplitudes(final IntSortedSet octaveSet) {
      if (octaveSet.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int lowFreqOctaves = -octaveSet.firstInt();
         int highFreqOctaves = octaveSet.lastInt();
         int octaves = lowFreqOctaves + highFreqOctaves + 1;
         if (octaves < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            DoubleList amplitudes = new DoubleArrayList(new double[octaves]);
            IntBidirectionalIterator iterator = octaveSet.iterator();

            while(iterator.hasNext()) {
               int octave = iterator.nextInt();
               amplitudes.set(octave + lowFreqOctaves, (double)1.0F);
            }

            return Pair.of(-lowFreqOctaves, amplitudes);
         }
      }
   }

   protected PerlinNoise(final RandomSource random, final Pair<Integer, DoubleList> pair, final boolean useNewInitialization) {
      this.firstOctave = (Integer)pair.getFirst();
      this.amplitudes = (DoubleList)pair.getSecond();
      int octaves = this.amplitudes.size();
      int zeroOctaveIndex = -this.firstOctave;
      this.noiseLevels = new ImprovedNoise[octaves];
      if (useNewInitialization) {
         PositionalRandomFactory positional = random.forkPositional();

         for(int i = 0; i < octaves; ++i) {
            if (this.amplitudes.getDouble(i) != (double)0.0F) {
               int octave = this.firstOctave + i;
               this.noiseLevels[i] = new ImprovedNoise(positional.fromHashOf("octave_" + octave));
            }
         }
      } else {
         ImprovedNoise zeroOctave = new ImprovedNoise(random);
         if (zeroOctaveIndex >= 0 && zeroOctaveIndex < octaves) {
            double zeroOctaveAmplitude = this.amplitudes.getDouble(zeroOctaveIndex);
            if (zeroOctaveAmplitude != (double)0.0F) {
               this.noiseLevels[zeroOctaveIndex] = zeroOctave;
            }
         }

         for(int i = zeroOctaveIndex - 1; i >= 0; --i) {
            if (i < octaves) {
               double amplitude = this.amplitudes.getDouble(i);
               if (amplitude != (double)0.0F) {
                  this.noiseLevels[i] = new ImprovedNoise(random);
               } else {
                  skipOctave(random);
               }
            } else {
               skipOctave(random);
            }
         }

         if (Arrays.stream(this.noiseLevels).filter(Objects::nonNull).count() != this.amplitudes.stream().filter((a) -> a != (double)0.0F).count()) {
            throw new IllegalStateException("Failed to create correct number of noise levels for given non-zero amplitudes");
         }

         if (zeroOctaveIndex < octaves - 1) {
            throw new IllegalArgumentException("Positive octaves are temporarily disabled");
         }
      }

      this.lowestFreqInputFactor = Math.pow((double)2.0F, (double)(-zeroOctaveIndex));
      this.lowestFreqValueFactor = Math.pow((double)2.0F, (double)(octaves - 1)) / (Math.pow((double)2.0F, (double)octaves) - (double)1.0F);
      this.maxValue = this.edgeValue((double)2.0F);
   }

   protected double maxValue() {
      return this.maxValue;
   }

   private static void skipOctave(final RandomSource random) {
      random.consumeCount(262);
   }

   public double getValue(final double x, final double y, final double z) {
      return this.getValue(x, y, z, (double)0.0F, (double)0.0F);
   }

   /** @deprecated */
   @Deprecated
   public double getValue(final double x, final double y, final double z, final double yScale, final double yFudge) {
      double value = (double)0.0F;
      double factor = this.lowestFreqInputFactor;
      double valueFactor = this.lowestFreqValueFactor;

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         ImprovedNoise noise = this.noiseLevels[i];
         if (noise != null) {
            double noiseVal = noise.noise(wrap(x * factor), wrap(y * factor), wrap(z * factor), yScale * factor, yFudge * factor);
            value += this.amplitudes.getDouble(i) * noiseVal * valueFactor;
         }

         factor *= (double)2.0F;
         valueFactor /= (double)2.0F;
      }

      return value;
   }

   public double maxBrokenValue(final double yScale) {
      return this.edgeValue(yScale + (double)2.0F);
   }

   private double edgeValue(final double noiseValue) {
      double value = (double)0.0F;
      double valueFactor = this.lowestFreqValueFactor;

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         ImprovedNoise noise = this.noiseLevels[i];
         if (noise != null) {
            value += this.amplitudes.getDouble(i) * noiseValue * valueFactor;
         }

         valueFactor /= (double)2.0F;
      }

      return value;
   }

   public @Nullable ImprovedNoise getOctaveNoise(final int i) {
      return this.noiseLevels[this.noiseLevels.length - 1 - i];
   }

   public static double wrap(final double x) {
      return x - (double)Mth.lfloor(x / (double)3.3554432E7F + (double)0.5F) * (double)3.3554432E7F;
   }

   protected int firstOctave() {
      return this.firstOctave;
   }

   protected DoubleList amplitudes() {
      return this.amplitudes;
   }

   @VisibleForTesting
   public void parityConfigString(final StringBuilder sb) {
      sb.append("PerlinNoise{");
      List<String> amplitudeStrings = this.amplitudes.stream().map((d) -> String.format(Locale.ROOT, "%.2f", d)).toList();
      sb.append("first octave: ").append(this.firstOctave).append(", amplitudes: ").append(amplitudeStrings).append(", noise levels: [");

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         sb.append(i).append(": ");
         ImprovedNoise noiseLevel = this.noiseLevels[i];
         if (noiseLevel == null) {
            sb.append("null");
         } else {
            noiseLevel.parityConfigString(sb);
         }

         sb.append(", ");
      }

      sb.append("]");
      sb.append("}");
   }
}
