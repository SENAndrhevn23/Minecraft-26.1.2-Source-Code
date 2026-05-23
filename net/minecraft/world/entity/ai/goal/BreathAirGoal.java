package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BreathAirGoal extends Goal {
   private final PathfinderMob mob;

   public BreathAirGoal(final PathfinderMob mob) {
      this.mob = mob;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public boolean canUse() {
      return this.mob.getAirSupply() < 140;
   }

   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean isInterruptable() {
      return false;
   }

   public void start() {
      this.findAirPosition();
      this.mob.getNavigation().stop();
   }

   private void findAirPosition() {
      Iterable<BlockPos> between = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - (double)1.0F), this.mob.getBlockY(), Mth.floor(this.mob.getZ() - (double)1.0F), Mth.floor(this.mob.getX() + (double)1.0F), Mth.floor(this.mob.getY() + (double)8.0F), Mth.floor(this.mob.getZ() + (double)1.0F));
      BlockPos destinationPos = null;

      for(BlockPos pos : between) {
         if (this.givesAir(this.mob.level(), pos)) {
            destinationPos = pos;
            break;
         }
      }

      if (destinationPos == null) {
         destinationPos = BlockPos.containing(this.mob.getX(), this.mob.getY() + (double)8.0F, this.mob.getZ());
      }

      this.mob.getNavigation().moveTo((double)destinationPos.getX(), (double)(destinationPos.getY() + 1), (double)destinationPos.getZ(), (double)1.0F);
   }

   public void tick() {
      this.findAirPosition();
      this.mob.moveRelative(0.02F, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
      this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
   }

   private boolean givesAir(final LevelReader level, final BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return (level.getFluidState(pos).isEmpty() || state.is(Blocks.BUBBLE_COLUMN)) && state.isPathfindable(PathComputationType.LAND);
   }
}
