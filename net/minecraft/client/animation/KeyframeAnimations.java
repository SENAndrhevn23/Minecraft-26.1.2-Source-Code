package net.minecraft.client.animation;

import org.joml.Vector3f;

public class KeyframeAnimations {
   public static Vector3f posVec(final float x, final float y, final float z) {
      return new Vector3f(x, -y, z);
   }

   public static Vector3f degreeVec(final float x, final float y, final float z) {
      return new Vector3f(x * ((float)Math.PI / 180F), y * ((float)Math.PI / 180F), z * ((float)Math.PI / 180F));
   }

   public static Vector3f scaleVec(final double x, final double y, final double z) {
      return new Vector3f((float)(x - (double)1.0F), (float)(y - (double)1.0F), (float)(z - (double)1.0F));
   }
}
