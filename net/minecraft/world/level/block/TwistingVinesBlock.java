package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TwistingVinesBlock extends GrowingPlantHeadBlock {
   public static final MapCodec<TwistingVinesBlock> CODEC = simpleCodec(TwistingVinesBlock::new);
   private static final VoxelShape SHAPE = Block.column((double)8.0F, (double)0.0F, (double)15.0F);

   public MapCodec<TwistingVinesBlock> codec() {
      return CODEC;
   }

   public TwistingVinesBlock(final BlockBehaviour.Properties properties) {
      super(properties, Direction.UP, SHAPE, false, 0.1);
   }

   protected int getBlocksToGrowWhenBonemealed(final RandomSource random) {
      return NetherVines.getBlocksToGrowWhenBonemealed(random);
   }

   protected Block getBodyBlock() {
      return Blocks.TWISTING_VINES_PLANT;
   }

   protected boolean canGrowInto(final BlockState state) {
      return NetherVines.isValidGrowthState(state);
   }
}
