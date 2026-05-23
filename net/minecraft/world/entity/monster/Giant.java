package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class Giant extends Monster {
   public Giant(final EntityType<? extends Giant> type, final Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, (double)100.0F).add(Attributes.MOVEMENT_SPEED, (double)0.5F).add(Attributes.ATTACK_DAMAGE, (double)50.0F).add(Attributes.CAMERA_DISTANCE, (double)16.0F);
   }

   public float getWalkTargetValue(final BlockPos pos, final LevelReader level) {
      return level.getPathfindingCostFromLightLevels(pos);
   }
}
