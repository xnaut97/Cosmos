package com.github.xnaut97.cosmos.menu;

import java.util.ArrayList;
import java.util.List;

public final class MenuLayout {

    public static List<Integer> row(int row) {
        List<Integer> slots = new ArrayList<>(9);
        int start = row * 9;
        for (int column = 0; column < 9; column++) {
            slots.add(start + column);
        }
        return slots;
    }

    public static List<Integer> column(int column, int rows) {
        List<Integer> slots = new ArrayList<>(Math.max(0, rows));
        for (int row = 0; row < rows; row++) {
            slots.add(row * 9 + column);
        }
        return slots;
    }

    public static List<Integer> border(int size) {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;
        for (int slot = 0; slot < size; slot++) {
            int row = slot / 9;
            int column = slot % 9;
            if (row == 0 || row == rows - 1 || column == 0 || column == 8) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public static List<Integer> rectangle(int startSlot, int width, int height) {
        List<Integer> slots = new ArrayList<>(Math.max(0, width * height));
        int startRow = startSlot / 9;
        int startColumn = startSlot % 9;

        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int absoluteColumn = startColumn + column;
                if (absoluteColumn < 0 || absoluteColumn > 8) {
                    continue;
                }
                slots.add((startRow + row) * 9 + absoluteColumn);
            }
        }
        return slots;
    }

    public static List<Integer> borderlessCenter(int size) {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;
        for (int row = 1; row < rows - 1; row++) {
            for (int column = 1; column < 8; column++) {
                slots.add(row * 9 + column);
            }
        }
        return slots;
    }

    public static List<Integer> circle(int centerSlot, int radius, int size) {
        List<Integer> slots = new ArrayList<>();
        int rows = size / 9;
        int centerRow = centerSlot / 9;
        int centerColumn = centerSlot % 9;
        int radiusSquared = radius * radius;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < 9; column++) {
                int rowDistance = row - centerRow;
                int columnDistance = column - centerColumn;
                if (rowDistance * rowDistance + columnDistance * columnDistance <= radiusSquared) {
                    slots.add(row * 9 + column);
                }
            }
        }
        return slots;
    }
}
