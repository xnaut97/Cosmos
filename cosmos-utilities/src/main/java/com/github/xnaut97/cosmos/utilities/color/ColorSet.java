package com.github.xnaut97.cosmos.utilities.color;

import lombok.Getter;

@Getter
public final class ColorSet {

    int red;

    int green;

    int blue;

    public ColorSet(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ColorSet(ColorSet color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }

    public ColorSet setRed(int red) {
        this.red = validate(red);
        return this;
    }

    public ColorSet setGreen(int green) {
        this.green = validate(green);
        return this;
    }

    public ColorSet setBlue(int blue) {
        this.blue = validate(blue);
        return this;
    }

    public ColorSet setAll(int color) {
        return setRed(color).setGreen(color).setBlue(color);
    }

    private int validate(int color) {
        return Math.max(0, Math.min(255, color));
    }

    /**
     * Convert hex code to color set
     * @param hex Hex color code
     * @return New color set from hex
     */
    public static ColorSet fromHex(String hex) {
        if (!hex.startsWith("#") && hex.length() != 7)
            return null;
        int red = Integer.valueOf(hex.substring(1, 3), 16);
        int green = Integer.valueOf(hex.substring(3, 5), 16);
        int blue = Integer.valueOf(hex.substring(5, 7), 16);
        return new ColorSet(red, green, blue);
    }

}
