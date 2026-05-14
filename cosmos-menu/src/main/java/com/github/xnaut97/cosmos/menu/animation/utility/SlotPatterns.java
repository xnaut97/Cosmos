package com.github.xnaut97.cosmos.menu.animation.utility;

import java.util.ArrayList;
import java.util.List;

public final class SlotPatterns {

    public static final int COLUMNS = 9;

    private SlotPatterns() {
    }

    public static int[] all(int rows) {
        int size = normalizeRows(rows) * COLUMNS;
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return slots;
    }

    public static int[] row(int row) {
        int[] slots = new int[COLUMNS];
        int start = row * COLUMNS;
        for (int column = 0; column < COLUMNS; column++) {
            slots[column] = start + column;
        }
        return slots;
    }

    public static int[] rows(int startRow, int endRow) {
        List<Integer> slots = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            for (int slot : row(row)) {
                slots.add(slot);
            }
        }
        return toArray(slots);
    }

    public static int[] column(int column, int rows) {
        int normalizedRows = normalizeRows(rows);
        int[] slots = new int[normalizedRows];
        for (int row = 0; row < normalizedRows; row++) {
            slots[row] = row * COLUMNS + column;
        }
        return slots;
    }

    public static int[] border(int rows) {
        int normalizedRows = normalizeRows(rows);
        List<Integer> slots = new ArrayList<>(normalizedRows * 2 + 14);
        for (int row = 0; row < normalizedRows; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                if (row == 0 || row == normalizedRows - 1 || column == 0 || column == COLUMNS - 1) {
                    slots.add(row * COLUMNS + column);
                }
            }
        }
        return toArray(slots);
    }

    public static int[] borderClockwise(int rows) {
        int normalizedRows = normalizeRows(rows);
        if (normalizedRows <= 0) {
            return new int[0];
        }

        List<Integer> slots = new ArrayList<>();
        for (int column = 0; column < COLUMNS; column++) {
            slots.add(column);
        }
        for (int row = 1; row < normalizedRows; row++) {
            slots.add(row * COLUMNS + COLUMNS - 1);
        }
        if (normalizedRows > 1) {
            for (int column = COLUMNS - 2; column >= 0; column--) {
                slots.add((normalizedRows - 1) * COLUMNS + column);
            }
        }
        for (int row = normalizedRows - 2; row >= 1; row--) {
            slots.add(row * COLUMNS);
        }
        return toArray(slots);
    }

    public static int[] rectangle(int startSlot, int width, int height) {
        int normalizedWidth = Math.max(0, Math.min(COLUMNS, width));
        int normalizedHeight = Math.max(0, height);
        List<Integer> slots = new ArrayList<>(normalizedWidth * normalizedHeight);
        int startRow = startSlot / COLUMNS;
        int startColumn = startSlot % COLUMNS;
        for (int row = 0; row < normalizedHeight; row++) {
            for (int column = 0; column < normalizedWidth; column++) {
                int absoluteColumn = startColumn + column;
                if (absoluteColumn >= 0 && absoluteColumn < COLUMNS) {
                    slots.add((startRow + row) * COLUMNS + absoluteColumn);
                }
            }
        }
        return toArray(slots);
    }

    public static int[] rectangleBorder(int startSlot, int width, int height) {
        List<Integer> slots = new ArrayList<>();
        int startRow = startSlot / COLUMNS;
        int startColumn = startSlot % COLUMNS;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (row == 0 || row == height - 1 || column == 0 || column == width - 1) {
                    int absoluteColumn = startColumn + column;
                    if (absoluteColumn >= 0 && absoluteColumn < COLUMNS) {
                        slots.add((startRow + row) * COLUMNS + absoluteColumn);
                    }
                }
            }
        }
        return toArray(slots);
    }

    public static int[] borderlessCenter(int rows) {
        int normalizedRows = normalizeRows(rows);
        if (normalizedRows < 3) {
            return new int[0];
        }
        return rectangle(COLUMNS + 1, COLUMNS - 2, normalizedRows - 2);
    }

    public static int[] spiral(int rows) {
        int normalizedRows = normalizeRows(rows);
        List<Integer> slots = new ArrayList<>(normalizedRows * COLUMNS);
        int top = 0;
        int bottom = normalizedRows - 1;
        int left = 0;
        int right = COLUMNS - 1;

        while (top <= bottom && left <= right) {
            for (int column = left; column <= right; column++) {
                slots.add(top * COLUMNS + column);
            }
            top++;

            for (int row = top; row <= bottom; row++) {
                slots.add(row * COLUMNS + right);
            }
            right--;

            if (top <= bottom) {
                for (int column = right; column >= left; column--) {
                    slots.add(bottom * COLUMNS + column);
                }
                bottom--;
            }

            if (left <= right) {
                for (int row = bottom; row >= top; row--) {
                    slots.add(row * COLUMNS + left);
                }
                left++;
            }
        }
        return toArray(slots);
    }

    public static int[] circle(int centerSlot, int radius, int rows) {
        int normalizedRows = normalizeRows(rows);
        int radiusSquared = radius * radius;
        int centerRow = centerSlot / COLUMNS;
        int centerColumn = centerSlot % COLUMNS;
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < normalizedRows; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int rowDistance = row - centerRow;
                int columnDistance = column - centerColumn;
                if (rowDistance * rowDistance + columnDistance * columnDistance <= radiusSquared) {
                    slots.add(row * COLUMNS + column);
                }
            }
        }
        return toArray(slots);
    }

    public static int[] diagonalDown(int rows) {
        int normalizedRows = normalizeRows(rows);
        int[] slots = new int[normalizedRows];
        for (int row = 0; row < normalizedRows; row++) {
            slots[row] = row * COLUMNS + Math.min(COLUMNS - 1, row);
        }
        return slots;
    }

    public static int[] diagonalUp(int rows) {
        int normalizedRows = normalizeRows(rows);
        int[] slots = new int[normalizedRows];
        for (int row = 0; row < normalizedRows; row++) {
            slots[row] = row * COLUMNS + Math.max(0, COLUMNS - 1 - row);
        }
        return slots;
    }

    public static int normalizeRows(int rows) {
        return Math.max(1, Math.min(6, rows));
    }

    private static int[] toArray(List<Integer> slots) {
        int[] result = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            result[i] = slots.get(i);
        }
        return result;
    }
}
