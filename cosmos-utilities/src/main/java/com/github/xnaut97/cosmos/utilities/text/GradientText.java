package com.github.xnaut97.cosmos.utilities.text;


import com.github.xnaut97.cosmos.utilities.color.ColorContainer;
import com.github.xnaut97.cosmos.utilities.color.ColorSet;

import java.util.ArrayList;
import java.util.List;

public class GradientText {

    private final String text;
    private final List<ColorSet> colors = new ArrayList<>();

    public GradientText(String text) {
        if (text == null) {
            throw new NullPointerException("Text must not be null");
        }

        this.text = text;
    }

    public GradientText addColors(String... colors) {
        for (String color : colors) {
            this.colors.add(convert(color));
        }
        return this;
    }

    public String build() {
        if (text.isEmpty()) {
            return "";
        }

        List<String> gradient = generateGradient();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            builder.append(toMinecraftColor(gradient.get(i)))
                    .append(text.charAt(i));
        }

        return builder.toString();
    }

    private List<String> generateGradient() {
        List<String> output = new ArrayList<String>();

        int length = text.length();
        int sections = colors.size() - 1;

        for (int i = 0; i < length; i++) {

            float progress = (float) i / Math.max(1, length - 1);

            float scaled = progress * sections;

            int index = (int) Math.floor(scaled);

            if (index >= sections) {
                index = sections - 1;
            }

            float localProgress = scaled - index;

            ColorSet start = colors.get(index);
            ColorSet end = colors.get(index + 1);

            int red = interpolate(start.getRed(), end.getRed(), localProgress);
            int green = interpolate(start.getGreen(), end.getGreen(), localProgress);
            int blue = interpolate(start.getBlue(), end.getBlue(), localProgress);

            output.add(String.format("#%02x%02x%02x", red, green, blue));
        }

        return output;
    }

    private int interpolate(int start, int end, float progress) {
        return (int) (start + (end - start) * progress);
    }

    private String toMinecraftColor(String hex) {
        char[] chars = hex.replace("#", "").toCharArray();

        StringBuilder builder = new StringBuilder("§x");

        for (char c : chars) {
            builder.append('§').append(c);
        }

        return builder.toString();
    }

    private ColorSet convert(String color) {
        if (color == null) {
            throw new NullPointerException("Color must not be null");
        }

        ColorSet set = color.startsWith("#")
                ? ColorSet.fromHex(color)
                : ColorContainer.getColor(color);

        if (set == null) {
            throw new IllegalArgumentException("Invalid color: " + color);
        }

        return new ColorSet(set);
    }
}