package net.minecraft.world.level.block.entity;

import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

public interface Hopper extends Container {
   AABB SUCK_AABB = (AABB)Block.column((double)16.0F, (double)11.0F, (double)32.0F).toAabbs().get(0);

   default AABB getSuckAabb() {
      return SUCK_AABB;
   }

   double getLevelX();

   double getLevelY();

   double getLevelZ();

   boolean isGridAligned();
}
