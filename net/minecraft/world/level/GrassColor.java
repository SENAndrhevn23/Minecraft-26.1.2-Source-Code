package net.minecraft.world.level;

public class GrassColor {
   private static int[] pixels = new int[65536];

   public static void init(final int[] pixels) {
      GrassColor.pixels = pixels;
   }

   public static int get(final double temp, final double rain) {
      return ColorMapColorUtil.get(temp, rain, pixels, -65281);
   }

   public static int getDefaultColor() {
      return get((double)0.5F, (double)1.0F);
   }
}
