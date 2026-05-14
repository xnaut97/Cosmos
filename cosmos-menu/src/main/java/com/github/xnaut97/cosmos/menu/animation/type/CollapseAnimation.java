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
 * Reverse reveal that collapses a region inward. Recommended for close, cancel, and transition-out states.
 */
@Getter
public class CollapseAnimation extends BaseRegionAnimation {

    private final int centerSlot;
    private final int collapseEveryFrames;
    private final int maxDistance;
    private final int[] distances;
    private final ItemStack visibleItem;
    private final ItemStack collapsedItem;

    @Builder
    public CollapseAnimation(String id,
                             Long delayTicks,
                             Long periodTicks,
                             Long maxFrames,
                             Integer rows,
                             int[] slots,
                             Integer centerSlot,
                             ItemStack visibleItem,
                             ItemStack collapsedItem,
                             Integer collapseEveryFrames,
                             Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.nonNegative(maxFrames, 24L),
                false,
                slots == null ? SlotPatterns.all(rows == null ? 6 : rows) : slots,
                new ItemStack[]{visibleItem == null ? AnimationItems.lightGrayPane() : visibleItem},
                collapsedItem,
                AnimationSupport.bool(clearOnStop, true));
        this.centerSlot = centerSlot == null ? 22 : centerSlot;
        this.collapseEveryFrames = AnimationSupport.positive(collapseEveryFrames, 2);
        this.visibleItem = visibleItem == null ? AnimationItems.lightGrayPane() : visibleItem;
        this.collapsedItem = collapsedItem;
        this.distances = distances(getSlots(), this.centerSlot);
        int computedMaxDistance = 0;
        for (int distance : distances) {
            computedMaxDistance = Math.max(computedMaxDistance, distance);
        }
        this.maxDistance = computedMaxDistance;
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        int collapsedRadius = (int) (frame / collapseEveryFrames);
        int threshold = maxDistance - collapsedRadius;
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, distances[i] <= threshold ? visibleItem : collapsedItem);
        }
    }

    private static int[] distances(int[] slots, int centerSlot) {
        int[] result = new int[slots.length];
        int centerRow = centerSlot / SlotPatterns.COLUMNS;
        int centerColumn = centerSlot % SlotPatterns.COLUMNS;
        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            result[i] = Math.max(Math.abs(slot / SlotPatterns.COLUMNS - centerRow),
                    Math.abs(slot % SlotPatterns.COLUMNS - centerColumn));
        }
        return result;
    }
}
