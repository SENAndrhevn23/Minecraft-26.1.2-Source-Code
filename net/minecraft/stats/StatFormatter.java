package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public interface StatFormatter {
   DecimalFormat DECIMAL_FORMAT = new DecimalFormat("########0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
   StatFormatter DEFAULT;
   StatFormatter DIVIDE_BY_TEN;
   StatFormatter DISTANCE;
   StatFormatter TIME;

   String format(int value);

   static {
      NumberFormat var10000 = NumberFormat.getIntegerInstance(Locale.US);
      Objects.requireNonNull(var10000);
      DEFAULT = var10000::format;
      DIVIDE_BY_TEN = (value) -> DECIMAL_FORMAT.format((double)value * 0.1);
      DISTANCE = (cm) -> {
         double meters = (double)cm / (double)100.0F;
         double kilometers = meters / (double)1000.0F;
         if (kilometers > (double)0.5F) {
            return DECIMAL_FORMAT.format(kilometers) + " km";
         } else {
            return meters > (double)0.5F ? DECIMAL_FORMAT.format(meters) + " m" : cm + " cm";
         }
      };
      TIME = (value) -> {
         double seconds = (double)value / (double)20.0F;
         double minutes = seconds / (double)60.0F;
         double hours = minutes / (double)60.0F;
         double days = hours / (double)24.0F;
         double years = days / (double)365.0F;
         if (years > (double)0.5F) {
            return DECIMAL_FORMAT.format(years) + " y";
         } else if (days > (double)0.5F) {
            return DECIMAL_FORMAT.format(days) + " d";
         } else if (hours > (double)0.5F) {
            return DECIMAL_FORMAT.format(hours) + " h";
         } else {
            return minutes > (double)0.5F ? DECIMAL_FORMAT.format(minutes) + " min" : seconds + " s";
         }
      };
   }
}
