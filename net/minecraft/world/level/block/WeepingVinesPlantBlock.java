package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WeepingVinesPlantBlock extends GrowingPlantBodyBlock {
   public static final MapCodec<WeepingVinesPlantBlock> CODEC = simpleCodec(WeepingVinesPlantBlock::new);
   private static final VoxelShape SHAPE = Block.column((double)14.0F, (double)0.0F, (double)16.0F);

   public MapCodec<WeepingVinesPlantBlock> codec() {
      return CODEC;
   }

   public WeepingVinesPlantBlock(final BlockBehaviour.Properties properties) {
      super(properties, Direction.DOWN, SHAPE, false);
   }

   protected GrowingPlantHeadBlock getHeadBlock() {
      return (GrowingPlantHeadBlock)Blocks.WEEPING_VINES;
   }
}
