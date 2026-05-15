package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import com.github.xnaut97.cosmos.menu.animation.utility.SlotPatterns;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Expands a warm fire-like effect from an origin slot. Recommended for destructive confirmations.
 */
@Getter
public class FireSpreadAnimation extends BaseRegionAnimation {

    private final int originSlot;
    private final int spreadEveryFrames;
    private final int[] distances;
    private final ItemStack unburnedItem;

    @Builder
    public FireSpreadAnimation(String id,
                               Long delayTicks,
                               Long periodTicks,
                               Long maxFrames,
                               Boolean loop,
                               Integer rows,
                               int[] slots,
                               Integer originSlot,
                               ItemStack[] items,
                               ItemStack unburnedItem,
                               Integer spreadEveryFrames,
                               Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 32L),
                AnimationSupport.bool(loop, false),
                slots == null ? SlotPatterns.all(rows == null ? 6 : rows) : slots,
                items == null ? new ItemStack[]{AnimationItems.redPane(), AnimationItems.orangePane(), AnimationItems.yellowPane(), AnimationItems.fire()} : items,
                unburnedItem == null ? AnimationItems.blackPane() : unburnedItem,
                AnimationSupport.bool(clearOnStop, false));
        this.originSlot = originSlot == null ? 22 : originSlot;
        this.spreadEveryFrames = AnimationSupport.positive(spreadEveryFrames, 1);
        this.unburnedItem = unburnedItem == null ? AnimationItems.blackPane() : unburnedItem;
        this.distances = buildDistances(getSlots(), this.originSlot);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        int spread = (int) (frame / spreadEveryFrames);
        for (int i = 0; i < slotCount(); i++) {
            int distance = distances[i];
            renderAt(menu, i, distance <= spread ? paletteAt(spread - distance) : unburnedItem);
        }
    }

    private static int[] buildDistances(int[] slots, int originSlot) {
        int[] result = new int[slots.length];
        int originRow = originSlot / SlotPatterns.COLUMNS;
        int originColumn = originSlot % SlotPatterns.COLUMNS;
        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            result[i] = Math.abs(slot / SlotPatterns.COLUMNS - originRow)
                    + Math.abs(slot % SlotPatterns.COLUMNS - originColumn);
        }
        return result;
    }
}
