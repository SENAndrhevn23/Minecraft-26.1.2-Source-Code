package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.debugchart.SampleStorage;

public class PingDebugChart extends AbstractDebugChart {
   private static final int CHART_TOP_VALUE = 500;

   public PingDebugChart(final Font font, final SampleStorage sampleStorage) {
      super(font, sampleStorage);
   }

   protected void extractAdditionalLinesAndLabels(final GuiGraphicsExtractor graphics, final int left, final int width, final int bottom) {
      this.extractStringWithShade(graphics, "500 ms", left + 1, bottom - 60 + 1);
   }

   protected String toDisplayString(final double millis) {
      return String.format(Locale.ROOT, "%d ms", (int)Math.round(millis));
   }

   protected int getSampleHeight(final double millis) {
      return (int)Math.round(millis * (double)60.0F / (double)500.0F);
   }

   protected int getSampleColor(final long millis) {
      return this.getSampleColor((double)millis, (double)0.0F, -16711936, (double)250.0F, -256, (double)500.0F, -65536);
   }
}
