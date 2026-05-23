package net.minecraft.world.level;

public interface ColorMapColorUtil {
   static int get(final double temp, double rain, final int[] pixels, final int defaultMapColor) {
      rain *= temp;
      int x = (int)(((double)1.0F - temp) * (double)255.0F);
      int y = (int)(((double)1.0F - rain) * (double)255.0F);
      int index = y << 8 | x;
      return index >= pixels.length ? defaultMapColor : pixels[index];
   }
}
