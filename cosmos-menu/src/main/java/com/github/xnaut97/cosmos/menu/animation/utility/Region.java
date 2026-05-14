package com.github.xnaut97.cosmos.menu.animation.utility;

import lombok.Getter;

import java.util.Arrays;

@Getter
public final class Region {

    private final int[] slots;

    private Region(int[] slots) {
        this.slots = AnimationSupport.slots(slots);
    }

    public static Region of(int... slots) {
        return new Region(slots);
    }

    public static Region border(int rows) {
        return new Region(SlotPatterns.border(rows));
    }

    public static Region rectangle(int startSlot, int width, int height) {
        return new Region(SlotPatterns.rectangle(startSlot, width, height));
    }

    public static Region spiral(int rows) {
        return new Region(SlotPatterns.spiral(rows));
    }

    public static Region circle(int centerSlot, int radius, int rows) {
        return new Region(SlotPatterns.circle(centerSlot, radius, rows));
    }

    public int size() {
        return slots.length;
    }

    public boolean isEmpty() {
        return slots.length == 0;
    }

    public int slotAt(int index) {
        return slots[AnimationSupport.floorMod(index, slots.length)];
    }

    public int[] copySlots() {
        return slots.clone();
    }

    @Override
    public String toString() {
        return "Region" + Arrays.toString(slots);
    }
}
