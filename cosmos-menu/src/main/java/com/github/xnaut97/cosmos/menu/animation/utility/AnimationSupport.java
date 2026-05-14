package com.github.xnaut97.cosmos.menu.animation.utility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Objects;

public final class AnimationSupport {

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private AnimationSupport() {
    }

    public static String id(String id, String fallback) {
        return id == null || id.isBlank() ? fallback : id;
    }

    public static long ticks(Long value, long fallback) {
        return value == null || value < 1L ? fallback : value;
    }

    public static long nonNegative(Long value, long fallback) {
        return value == null || value < 0L ? fallback : value;
    }

    public static int positive(Integer value, int fallback) {
        return value == null || value < 1 ? fallback : value;
    }

    public static int nonNegative(Integer value, int fallback) {
        return value == null || value < 0 ? fallback : value;
    }

    public static boolean bool(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    public static int[] slots(int[] slots) {
        return slots == null ? new int[0] : slots.clone();
    }

    public static int[] slots(Collection<Integer> slots) {
        if (slots == null || slots.isEmpty()) {
            return new int[0];
        }

        int[] result = new int[slots.size()];
        int index = 0;
        for (Integer slot : slots) {
            result[index++] = slot == null ? -1 : slot;
        }
        return result;
    }

    public static ItemStack[] palette(ItemStack[] items, ItemStack fallback) {
        if (items == null || items.length == 0) {
            return new ItemStack[]{fallback == null ? AIR : fallback};
        }

        ItemStack[] result = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            result[i] = items[i] == null ? AIR : items[i];
        }
        return result;
    }

    public static ItemStack[] palette(Collection<ItemStack> items, ItemStack fallback) {
        if (items == null || items.isEmpty()) {
            return new ItemStack[]{fallback == null ? AIR : fallback};
        }

        ItemStack[] result = new ItemStack[items.size()];
        int index = 0;
        for (ItemStack item : items) {
            result[index++] = item == null ? AIR : item;
        }
        return result;
    }

    public static int floorMod(long value, int length) {
        if (length <= 0) {
            return 0;
        }
        int result = (int) (value % length);
        return result < 0 ? result + length : result;
    }

    public static int pingPong(long value, int length) {
        if (length <= 1) {
            return 0;
        }
        int cycle = length * 2 - 2;
        int index = floorMod(value, cycle);
        return index < length ? index : cycle - index;
    }

    public static ItemStack itemAt(ItemStack[] items, long index) {
        Objects.requireNonNull(items, "items");
        return items[floorMod(index, items.length)];
    }

    public static boolean sameItem(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.getType() != second.getType() || first.getAmount() != second.getAmount()) {
            return false;
        }
        if (first.hasItemMeta() != second.hasItemMeta()) {
            return false;
        }
        return Objects.equals(first.getItemMeta(), second.getItemMeta());
    }
}
