package com.github.xnaut97.cosmos.menu.animation.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public final class SlotTraversal {

    private SlotTraversal() {
    }

    public static int[] order(int[] slots, FillPattern pattern, Long randomSeed) {
        int[] safeSlots = AnimationSupport.slots(slots);
        int[] indexes = indexes(safeSlots.length);
        FillPattern resolvedPattern = pattern == null ? FillPattern.SLOT_ORDER : pattern;
        Bounds bounds = bounds(safeSlots);

        switch (resolvedPattern) {
            case LEFT_TO_RIGHT:
                sort(indexes, Comparator
                        .comparingInt((Integer index) -> row(safeSlots[index]))
                        .thenComparingInt(index -> column(safeSlots[index])));
                break;

            case RIGHT_TO_LEFT:
                sort(indexes, Comparator
                        .comparingInt((Integer index) -> row(safeSlots[index]))
                        .thenComparing((Integer first, Integer second) ->
                                Integer.compare(
                                        column(safeSlots[second]),
                                        column(safeSlots[first])
                                )));
                break;

            case TOP_TO_BOTTOM:
                sort(indexes, Comparator
                        .comparingInt((Integer index) -> column(safeSlots[index]))
                        .thenComparingInt(index -> row(safeSlots[index])));
                break;

            case BOTTOM_TO_TOP:
                sort(indexes, Comparator
                        .comparingInt((Integer index) -> column(safeSlots[index]))
                        .thenComparing((Integer first, Integer second) ->
                                Integer.compare(
                                        row(safeSlots[second]),
                                        row(safeSlots[first])
                                )));
                break;

            case CENTER_OUT:
                sort(indexes, Comparator
                        .comparingDouble((Integer index) ->
                                centerDistance(bounds, safeSlots[index]))
                        .thenComparingInt(index -> safeSlots[index]));
                break;

            case OUTSIDE_IN:
                sort(indexes, Comparator
                        .comparingDouble((Integer index) ->
                                centerDistance(bounds, safeSlots[index]))
                        .reversed()
                        .thenComparingInt(index -> safeSlots[index]));
                break;

            case WAVE:
                sort(indexes, Comparator
                        .comparingInt((Integer index) ->
                                row(safeSlots[index]) + column(safeSlots[index]))
                        .thenComparingInt(index -> row(safeSlots[index])));
                break;

            case SNAKE:
            case ZIGZAG:
                sort(indexes,
                        (first, second) ->
                                compareSnake(safeSlots, bounds.minRow(), first, second));
                break;

            case RANDOM:
                shuffle(indexes, randomSeed == null ? 0x5EEDL : randomSeed);
                break;

            case SLOT_ORDER:
                break;
        }

        return indexes;
    }

    private static int[] indexes(int size) {
        int[] indexes = new int[size];
        for (int i = 0; i < size; i++) {
            indexes[i] = i;
        }
        return indexes;
    }

    private static void sort(int[] indexes, Comparator<Integer> comparator) {
        Integer[] boxed = new Integer[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            boxed[i] = indexes[i];
        }

        Arrays.sort(boxed, comparator);
        for (int i = 0; i < boxed.length; i++) {
            indexes[i] = boxed[i];
        }
    }

    private static void shuffle(int[] indexes, long seed) {
        Random random = new Random(seed);
        for (int i = indexes.length - 1; i > 0; i--) {
            int swapIndex = random.nextInt(i + 1);
            int value = indexes[i];
            indexes[i] = indexes[swapIndex];
            indexes[swapIndex] = value;
        }
    }

    private static int compareSnake(int[] slots, int minRow, Integer firstIndex, Integer secondIndex) {
        int firstSlot = slots[firstIndex];
        int secondSlot = slots[secondIndex];
        int firstRow = row(firstSlot);
        int secondRow = row(secondSlot);
        if (firstRow != secondRow) {
            return Integer.compare(firstRow, secondRow);
        }

        boolean reverse = ((firstRow - minRow) & 1) == 1;
        return reverse
                ? Integer.compare(column(secondSlot), column(firstSlot))
                : Integer.compare(column(firstSlot), column(secondSlot));
    }

    private static double centerDistance(Bounds bounds, int slot) {
        double rowDistance = row(slot) - bounds.centerRow();
        double columnDistance = column(slot) - bounds.centerColumn();
        return rowDistance * rowDistance + columnDistance * columnDistance;
    }

    private static Bounds bounds(int[] slots) {
        if (slots.length == 0) {
            return new Bounds(0, 0.0D, 0.0D);
        }

        double rowTotal = 0.0D;
        double columnTotal = 0.0D;
        int minRow = Integer.MAX_VALUE;
        for (int value : slots) {
            int row = row(value);
            rowTotal += row;
            columnTotal += column(value);
            minRow = Math.min(minRow, row);
        }

        return new Bounds(minRow, rowTotal / slots.length, columnTotal / slots.length);
    }

    private static int row(int slot) {
        return Math.floorDiv(Math.max(0, slot), SlotPatterns.COLUMNS);
    }

    private static int column(int slot) {
        return Math.floorMod(Math.max(0, slot), SlotPatterns.COLUMNS);
    }

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = true)
    private static class Bounds {
        int minRow;
        double centerRow;
        double centerColumn;
    }
}
