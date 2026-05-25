package com.github.xnaut97.cosmos.utilities.bar;

import com.google.common.base.Strings;
import org.apache.commons.lang.math.IntRange;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ActionProgressBar {

    private ActionProgressBar() {
    }

    public static String createProgressBar(double current,
                                           double total,
                                           int length,
                                           String symbol,
                                           String completedColor,
                                           String incompleteColor) {

        return createProgressBar(
                current,
                total,
                length,
                symbol,
                completedColor,
                incompleteColor,
                false
        );
    }

    public static String createProgressBar(double current,
                                           double total,
                                           int length,
                                           String symbol,
                                           String completedColor,
                                           String incompleteColor,
                                           boolean displayPercent) {

        float percent = calculatePercent(current, total);

        int completed = (int) (length * percent);

        String complete = color(completedColor);
        String incomplete = color(incompleteColor);

        return Strings.repeat(
                complete + symbol,
                completed
        ) +
                Strings.repeat(
                        incomplete + symbol,
                        Math.max(0, length - completed)
                ) +
                (displayPercent
                        ? " §7[" + complete
                          + (int) (percent * 100)
                          + "%§7]"
                        : "");
    }

    public static String createProgressBar(double current,
                                           double total,
                                           int length,
                                           String symbol,
                                           PercentColorMap completedColor,
                                           String incompleteColor) {

        return createProgressBar(
                current,
                total,
                length,
                symbol,
                completedColor,
                incompleteColor,
                false
        );
    }

    public static String createProgressBar(double current,
                                           double total,
                                           int length,
                                           String symbol,
                                           PercentColorMap completedColor,
                                           String incompleteColor,
                                           boolean displayPercent) {

        float percent = calculatePercent(current, total);

        int completed = (int) (length * percent);

        String complete = completedColor.getColorOf(
                (int) (percent * 100),
                "§c"
        );

        String incomplete = color(incompleteColor);

        return Strings.repeat(
                complete + symbol,
                completed
        ) +
                Strings.repeat(
                        incomplete + symbol,
                        Math.max(0, length - completed)
                ) +
                (displayPercent
                        ? " §7[" + complete
                          + (int) (percent * 100)
                          + "%§7]"
                        : "");
    }

    private static float calculatePercent(double current,
                                          double total) {

        if (total <= 0D) {
            return 0F;
        }

        float percent = (float) (current / total);

        return Math.max(
                0F,
                Math.min(1F, percent)
        );
    }

    private static String color(String text) {

        return text == null
                ? ""
                : text.replace('&', '§');
    }

    public static class PercentColorMap {

        /*
         * Keep insertion order
         */
        private final Map<IntRange, String> colorMap =
                new LinkedHashMap<>();

        public PercentColorMap append(IntRange range,
                                      String color) {

            if (range == null) {
                throw new NullPointerException("Range cannot be null");
            }

            colorMap.put(
                    range,
                    ActionProgressBar.color(color)
            );

            return this;
        }

        public String getColorOf(int percent,
                                 String defaultColor) {

            for (Map.Entry<IntRange, String> entry
                    : colorMap.entrySet()) {

                IntRange range = entry.getKey();

                /*
                 * Apache Commons Lang IntRange
                 */
                if (range.containsInteger(percent)) {
                    return entry.getValue();
                }
            }

            return ActionProgressBar.color(defaultColor);
        }

        public void clear() {
            colorMap.clear();
        }

        public boolean isEmpty() {
            return colorMap.isEmpty();
        }

        public Map<IntRange, String> asMap() {
            return new LinkedHashMap<>(colorMap);
        }
    }
}