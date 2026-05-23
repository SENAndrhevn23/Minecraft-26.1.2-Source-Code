package net.minecraft.client.gui.navigation;

import org.jspecify.annotations.Nullable;

public interface FocusNavigationEvent {
   ScreenDirection getVerticalDirectionForInitialFocus();

   public static record TabNavigation(boolean forward) implements FocusNavigationEvent {
      public ScreenDirection getVerticalDirectionForInitialFocus() {
         return this.forward ? ScreenDirection.DOWN : ScreenDirection.UP;
      }
   }

   public static class InitialFocus implements FocusNavigationEvent {
      public ScreenDirection getVerticalDirectionForInitialFocus() {
         return ScreenDirection.DOWN;
      }
   }

   public static record ArrowNavigation(ScreenDirection direction, @Nullable ScreenRectangle previousFocus) implements FocusNavigationEvent {
      public ArrowNavigation(final ScreenDirection direction) {
         this(direction, (ScreenRectangle)null);
      }

      public ScreenDirection getVerticalDirectionForInitialFocus() {
         return this.direction.getAxis() == ScreenAxis.VERTICAL ? this.direction : ScreenDirection.DOWN;
      }

      public ArrowNavigation with(final ScreenRectangle previousFocus) {
         return new ArrowNavigation(this.direction(), previousFocus);
      }
   }
}
