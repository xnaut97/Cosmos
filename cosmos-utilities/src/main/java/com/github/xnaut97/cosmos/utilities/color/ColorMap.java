package com.github.xnaut97.cosmos.utilities.color;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.Range;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ColorMap {

    /**
     * Ordered map for deterministic lookup
     */
    private final Map<Range<Integer>, String> colorMap = new LinkedHashMap<>();

    /**
     * Append Bukkit color
     */
    public ColorMap append(Range<Integer> range,
                           ChatColor color) {

        if (range == null) {
            throw new NullPointerException("Range cannot be null");
        }

        if (color == null) {
            throw new NullPointerException("Color cannot be null");
        }

        validateOverlap(range);

        colorMap.put(range, color.toString());

        return this;
    }

    /**
     * Append string/hex color
     */
    public ColorMap append(Range<Integer> range,
                           String color) {

        if (range == null) {
            throw new NullPointerException("Range cannot be null");
        }

        if (color == null) {
            throw new NullPointerException("Color cannot be null");
        }

        validateOverlap(range);

        colorMap.put(range, color.replace('&', '§'));

        return this;
    }

    /**
     * Get color of percent
     */
    public String getColorOf(int percent,
                             String defaultColor) {

        for (Map.Entry<Range<Integer>, String> entry : colorMap.entrySet()) {

            if (entry.getKey().contains(percent)) {
                return entry.getValue();
            }
        }

        return defaultColor == null
                ? "§f"
                : defaultColor.replace('&', '§');
    }

    /**
     * Prevent overlapping ranges
     */
    private void validateOverlap(Range<Integer> input) {

        for (Range<Integer> existing : colorMap.keySet()) {

            if (existing.isOverlappedBy(input)) {

                throw new IllegalArgumentException(
                        "Range overlaps existing range: "
                                + existing
                );
            }
        }
    }
}