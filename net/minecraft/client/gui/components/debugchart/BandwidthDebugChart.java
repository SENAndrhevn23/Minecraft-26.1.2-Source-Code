package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.SampleStorage;

public class BandwidthDebugChart extends AbstractDebugChart {
   private static final int MIN_COLOR = -16711681;
   private static final int MID_COLOR = -6250241;
   private static final int MAX_COLOR = -65536;
   private static final int KILOBYTE = 1024;
   private static final int MEGABYTE = 1048576;
   private static final int CHART_TOP_VALUE = 1048576;

   public BandwidthDebugChart(final Font font, final SampleStorage sampleStorage) {
      super(font, sampleStorage);
   }

   protected void extractAdditionalLinesAndLabels(final GuiGraphicsExtractor graphics, final int left, final int width, final int bottom) {
      this.extractLabeledLineAtValue(graphics, left, width, bottom, 64);
      this.extractLabeledLineAtValue(graphics, left, width, bottom, 1024);
      this.extractLabeledLineAtValue(graphics, left, width, bottom, 16384);
      this.extractStringWithShade(graphics, toDisplayStringInternal((double)1048576.0F), left + 1, bottom - getSampleHeightInternal((double)1048576.0F) + 1);
   }

   private void extractLabeledLineAtValue(final GuiGraphicsExtractor graphics, final int left, final int width, final int bottom, final int bytesPerSecond) {
      this.extractLineWithLabel(graphics, left, width, bottom - getSampleHeightInternal((double)bytesPerSecond), toDisplayStringInternal((double)bytesPerSecond));
   }

   private void extractLineWithLabel(final GuiGraphicsExtractor graphics, final int x, final int width, final int y, final String label) {
      this.extractStringWithShade(graphics, label, x + 1, y + 1);
      graphics.horizontalLine(x, x + width - 1, y, -1);
   }

   protected String toDisplayString(final double bytesPerTick) {
      return toDisplayStringInternal(toBytesPerSecond(bytesPerTick));
   }

   private static String toDisplayStringInternal(final double bytesPerSecond) {
      if (bytesPerSecond >= (double)1048576.0F) {
         return String.format(Locale.ROOT, "%.1f MiB/s", bytesPerSecond / (double)1048576.0F);
      } else {
         return bytesPerSecond >= (double)1024.0F ? String.format(Locale.ROOT, "%.1f KiB/s", bytesPerSecond / (double)1024.0F) : String.format(Locale.ROOT, "%d B/s", Mth.floor(bytesPerSecond));
      }
   }

   protected int getSampleHeight(final double bytesPerTick) {
      return getSampleHeightInternal(toBytesPerSecond(bytesPerTick));
   }

   private static int getSampleHeightInternal(final double bytesPerSecond) {
      return (int)Math.round(Math.log(bytesPerSecond + (double)1.0F) * (double)60.0F / Math.log((double)1048576.0F));
   }

   protected int getSampleColor(final long bytesPerTick) {
      return this.getSampleColor(toBytesPerSecond((double)bytesPerTick), (double)0.0F, -16711681, (double)8192.0F, -6250241, (double)1.048576E7F, -65536);
   }

   private static double toBytesPerSecond(final double bytesPerTick) {
      return bytesPerTick * (double)20.0F;
   }
}
