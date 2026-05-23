package com.mojang.blaze3d.audio;

import net.minecraft.world.phys.Vec3;

public record ListenerTransform(Vec3 position, Vec3 forward, Vec3 up) {
   public static final ListenerTransform INITIAL;

   public Vec3 right() {
      return this.forward.cross(this.up);
   }

   static {
      INITIAL = new ListenerTransform(Vec3.ZERO, new Vec3((double)0.0F, (double)0.0F, (double)-1.0F), new Vec3((double)0.0F, (double)1.0F, (double)0.0F));
   }
}
