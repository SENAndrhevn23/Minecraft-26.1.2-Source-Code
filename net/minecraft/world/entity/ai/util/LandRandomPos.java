package net.minecraft.world.entity.ai.util;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LandRandomPos {
   public static @Nullable Vec3 getPos(final PathfinderMob mob, final int horizontalDist, final int verticalDist) {
      Objects.requireNonNull(mob);
      return getPos(mob, horizontalDist, verticalDist, mob::getWalkTargetValue);
   }

   public static @Nullable Vec3 getPos(final PathfinderMob mob, final int horizontalDist, final int verticalDist, final ToDoubleFunction<BlockPos> positionWeight) {
      boolean restrict = GoalUtils.mobRestricted(mob, (double)horizontalDist);
      return RandomPos.generateRandomPos((Supplier)(() -> {
         BlockPos direction = RandomPos.generateRandomDirection(mob.getRandom(), horizontalDist, verticalDist);
         BlockPos pos = generateRandomPosTowardDirection(mob, (double)horizontalDist, restrict, direction);
         return pos == null ? null : movePosUpOutOfSolid(mob, pos);
      }), positionWeight);
   }

   public static @Nullable Vec3 getPosTowards(final PathfinderMob mob, final int horizontalDist, final int verticalDist, final Vec3 towardsPos) {
      Vec3 dir = towardsPos.subtract(mob.getX(), mob.getY(), mob.getZ());
      boolean restrict = GoalUtils.mobRestricted(mob, (double)horizontalDist);
      return getPosInDirection(mob, (double)0.0F, (double)horizontalDist, verticalDist, dir, restrict);
   }

   public static @Nullable Vec3 getPosAway(final PathfinderMob mob, final int horizontalDist, final int verticalDist, final Vec3 avoidPos) {
      return getPosAway(mob, (double)0.0F, (double)horizontalDist, verticalDist, avoidPos);
   }

   public static @Nullable Vec3 getPosAway(final PathfinderMob mob, final double minHorizontalDist, final double maxHorizontalDist, final int verticalDist, final Vec3 avoidPos) {
      Vec3 dirAway = mob.position().subtract(avoidPos);
      if (dirAway.length() == (double)0.0F) {
         dirAway = new Vec3(mob.getRandom().nextDouble() - (double)0.5F, (double)0.0F, mob.getRandom().nextDouble() - (double)0.5F);
      }

      boolean restrict = GoalUtils.mobRestricted(mob, maxHorizontalDist);
      return getPosInDirection(mob, minHorizontalDist, maxHorizontalDist, verticalDist, dirAway, restrict);
   }

   private static @Nullable Vec3 getPosInDirection(final PathfinderMob mob, final double minHorizontalDist, final double maxHorizontalDist, final int verticalDist, final Vec3 dir, final boolean restrict) {
      return RandomPos.generateRandomPos(mob, (Supplier)(() -> {
         BlockPos direction = RandomPos.generateRandomDirectionWithinRadians(mob.getRandom(), minHorizontalDist, maxHorizontalDist, verticalDist, 0, dir.x, dir.z, (double)((float)Math.PI / 2F));
         if (direction == null) {
            return null;
         } else {
            BlockPos pos = generateRandomPosTowardDirection(mob, maxHorizontalDist, restrict, direction);
            return pos == null ? null : movePosUpOutOfSolid(mob, pos);
         }
      }));
   }

   public static @Nullable BlockPos movePosUpOutOfSolid(final PathfinderMob mob, BlockPos pos) {
      pos = RandomPos.moveUpOutOfSolid(pos, mob.level().getMaxY(), (blockPos) -> GoalUtils.isSolid(mob, blockPos));
      return !GoalUtils.isWater(mob, pos) && !GoalUtils.hasMalus(mob, pos) ? pos : null;
   }

   public static @Nullable BlockPos generateRandomPosTowardDirection(final PathfinderMob mob, final double horizontalDist, final boolean restrict, final BlockPos direction) {
      BlockPos pos = RandomPos.generateRandomPosTowardDirection(mob, horizontalDist, mob.getRandom(), direction);
      return !GoalUtils.isOutsideLimits(pos, mob) && !GoalUtils.isRestricted(restrict, mob, pos) && !GoalUtils.isNotStable(mob.getNavigation(), pos) ? pos : null;
   }
}
