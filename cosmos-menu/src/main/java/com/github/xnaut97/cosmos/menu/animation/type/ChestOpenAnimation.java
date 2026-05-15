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
 * Expanding reveal from a center slot. Recommended for opening storage, mailboxes, and crate menus.
 */
@Getter
public class ChestOpenAnimation extends BaseRegionAnimation {

    private final int centerSlot;
    private final int revealEveryFrames;
    private final int[] distances;
    private final ItemStack revealItem;
    private final ItemStack hiddenItem;

    @Builder
    public ChestOpenAnimation(String id,
                              Long delayTicks,
                              Long periodTicks,
                              Long maxFrames,
                              Integer rows,
                              int[] slots,
                              Integer centerSlot,
                              ItemStack revealItem,
                              ItemStack hiddenItem,
                              Integer revealEveryFrames,
                              Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.nonNegative(maxFrames, 24L),
                false,
                slots == null ? SlotPatterns.all(rows == null ? 6 : rows) : slots,
                new ItemStack[]{revealItem == null ? AnimationItems.chest() : revealItem},
                hiddenItem == null ? AnimationItems.blackPane() : hiddenItem,
                AnimationSupport.bool(clearOnStop, false));
        this.centerSlot = centerSlot == null ? 22 : centerSlot;
        this.revealEveryFrames = AnimationSupport.positive(revealEveryFrames, 2);
        this.revealItem = revealItem == null ? AnimationItems.chest() : revealItem;
        this.hiddenItem = hiddenItem == null ? AnimationItems.blackPane() : hiddenItem;
        this.distances = distances(getSlots(), this.centerSlot);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        int radius = (int) (frame / revealEveryFrames);
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, distances[i] <= radius ? revealItem : hiddenItem);
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
