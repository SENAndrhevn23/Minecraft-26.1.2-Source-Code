package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

public class TryFindWaterGoal extends Goal {
   private final PathfinderMob mob;

   public TryFindWaterGoal(final PathfinderMob mob) {
      this.mob = mob;
   }

   public boolean canUse() {
      return this.mob.onGround() && !this.mob.level().getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
   }

   public void start() {
      BlockPos waterPos = null;

      for(BlockPos pos : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - (double)2.0F), Mth.floor(this.mob.getY() - (double)2.0F), Mth.floor(this.mob.getZ() - (double)2.0F), Mth.floor(this.mob.getX() + (double)2.0F), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + (double)2.0F))) {
         if (this.mob.level().getFluidState(pos).is(FluidTags.WATER)) {
            waterPos = pos;
            break;
         }
      }

      if (waterPos != null) {
         this.mob.getMoveControl().setWantedPosition((double)waterPos.getX(), (double)waterPos.getY(), (double)waterPos.getZ(), (double)1.0F);
      }

   }
}
