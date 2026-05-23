package net.minecraft.client;

import org.joml.Vector2i;

public class ScrollWheelHandler {
   private double accumulatedScrollX;
   private double accumulatedScrollY;

   public Vector2i onMouseScroll(final double scaledXScrollOffset, final double scaledYScrollOffset) {
      if (this.accumulatedScrollX != (double)0.0F && Math.signum(scaledXScrollOffset) != Math.signum(this.accumulatedScrollX)) {
         this.accumulatedScrollX = (double)0.0F;
      }

      if (this.accumulatedScrollY != (double)0.0F && Math.signum(scaledYScrollOffset) != Math.signum(this.accumulatedScrollY)) {
         this.accumulatedScrollY = (double)0.0F;
      }

      this.accumulatedScrollX += scaledXScrollOffset;
      this.accumulatedScrollY += scaledYScrollOffset;
      int wheelX = (int)this.accumulatedScrollX;
      int wheelY = (int)this.accumulatedScrollY;
      if (wheelX == 0 && wheelY == 0) {
         return new Vector2i(0, 0);
      } else {
         this.accumulatedScrollX -= (double)wheelX;
         this.accumulatedScrollY -= (double)wheelY;
         return new Vector2i(wheelX, wheelY);
      }
   }

   public static int getNextScrollWheelSelection(final double wheel, int currentSelected, final int limit) {
      int step = (int)Math.signum(wheel);
      currentSelected -= step;

      for(currentSelected = Math.max(-1, currentSelected); currentSelected < 0; currentSelected += limit) {
      }

      while(currentSelected >= limit) {
         currentSelected -= limit;
      }

      return currentSelected;
   }
}
