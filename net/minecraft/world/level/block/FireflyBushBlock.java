package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class FireflyBushBlock extends VegetationBlock implements BonemealableBlock {
   private static final double FIREFLY_CHANCE_PER_TICK = 0.7;
   private static final double FIREFLY_HORIZONTAL_RANGE = (double)10.0F;
   private static final double FIREFLY_VERTICAL_RANGE = (double)5.0F;
   private static final int FIREFLY_SPAWN_MAX_BRIGHTNESS_LEVEL = 13;
   private static final int FIREFLY_AMBIENT_SOUND_CHANCE_ONE_IN = 30;
   public static final MapCodec<FireflyBushBlock> CODEC = simpleCodec(FireflyBushBlock::new);

   public FireflyBushBlock(final BlockBehaviour.Properties properties) {
      super(properties);
   }

   protected MapCodec<? extends FireflyBushBlock> codec() {
      return CODEC;
   }

   public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
      if (random.nextInt(30) == 0 && (Boolean)level.environmentAttributes().getValue(EnvironmentAttributes.FIREFLY_BUSH_SOUNDS, pos) && level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos) <= pos.getY()) {
         level.playLocalSound(pos, SoundEvents.FIREFLY_BUSH_IDLE, SoundSource.AMBIENT, 1.0F, 1.0F, false);
      }

      if (level.getMaxLocalRawBrightness(pos) <= 13 && random.nextDouble() <= 0.7) {
         double fireflyX = (double)pos.getX() + random.nextDouble() * (double)10.0F - (double)5.0F;
         double fireflyY = (double)pos.getY() + random.nextDouble() * (double)5.0F;
         double fireflyZ = (double)pos.getZ() + random.nextDouble() * (double)10.0F - (double)5.0F;
         level.addParticle(ParticleTypes.FIREFLY, fireflyX, fireflyY, fireflyZ, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state) {
      return BonemealableBlock.hasSpreadableNeighbourPos(level, pos, state);
   }

   public boolean isBonemealSuccess(final Level level, final RandomSource random, final BlockPos pos, final BlockState state) {
      return true;
   }

   public void performBonemeal(final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state) {
      BonemealableBlock.findSpreadableNeighbourPos(level, pos, state).ifPresent((blockPos) -> level.setBlockAndUpdate(blockPos, this.defaultBlockState()));
   }
}
