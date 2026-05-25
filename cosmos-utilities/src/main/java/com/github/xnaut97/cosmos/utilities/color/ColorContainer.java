package com.github.xnaut97.cosmos.utilities.color;

import com.google.common.collect.Maps;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public final class ColorContainer {

    private static final Map<String, ColorSet> COLORS = Maps.newHashMap();

    static {

        Arrays.stream(ChatColor.values()).forEach(color -> {

            ColorSet colorSet = new ColorSet(0, 0, 0);

            switch (color) {

                case DARK_BLUE:
                    colorSet.setBlue(170);
                    break;

                case DARK_GREEN:
                    colorSet.setGreen(170);
                    break;

                case DARK_AQUA:
                    colorSet.setGreen(170)
                            .setBlue(170);
                    break;

                case DARK_RED:
                    colorSet.setRed(170);
                    break;

                case DARK_PURPLE:
                    colorSet.setRed(170)
                            .setBlue(170);
                    break;

                case GOLD:
                    colorSet.setRed(255)
                            .setGreen(170);
                    break;

                case GRAY:
                    colorSet.setAll(170);
                    break;

                case DARK_GRAY:
                    colorSet.setAll(85);
                    break;

                case BLUE:
                    colorSet.setAll(85)
                            .setBlue(255);
                    break;

                case GREEN:
                    colorSet.setAll(85)
                            .setGreen(255);
                    break;

                case AQUA:
                    colorSet.setAll(255)
                            .setRed(85);
                    break;

                case RED:
                    colorSet.setAll(85)
                            .setRed(255);
                    break;

                case LIGHT_PURPLE:
                    colorSet.setAll(255)
                            .setGreen(85);
                    break;

                case YELLOW:
                    colorSet.setAll(255)
                            .setBlue(85);
                    break;

                case WHITE:
                    colorSet.setAll(255);
                    break;

                default:
                    break;
            }

            COLORS.put(
                    String.valueOf(color.getChar()),
                    colorSet
            );
        });
    }

    private ColorContainer() {
    }

    public static ColorSet getColor(ChatColor color) {

        return color == null
                ? new ColorSet(255, 255, 255)
                : getColor(String.valueOf(color.getChar()));
    }

    @Nullable
    public static ColorSet getColor(String code) {

        return COLORS.getOrDefault(
                code,
                new ColorSet(255, 255, 255)
        );
    }
}