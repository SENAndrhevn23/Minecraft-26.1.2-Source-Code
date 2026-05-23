package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TwistingVinesPlantBlock extends GrowingPlantBodyBlock {
   public static final MapCodec<TwistingVinesPlantBlock> CODEC = simpleCodec(TwistingVinesPlantBlock::new);
   private static final VoxelShape SHAPE = Block.column((double)8.0F, (double)0.0F, (double)16.0F);

   public MapCodec<TwistingVinesPlantBlock> codec() {
      return CODEC;
   }

   public TwistingVinesPlantBlock(final BlockBehaviour.Properties properties) {
      super(properties, Direction.UP, SHAPE, false);
   }

   protected GrowingPlantHeadBlock getHeadBlock() {
      return (GrowingPlantHeadBlock)Blocks.TWISTING_VINES;
   }
}
