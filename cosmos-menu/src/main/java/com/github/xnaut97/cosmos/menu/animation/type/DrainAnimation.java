package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import com.github.xnaut97.cosmos.menu.animation.utility.FillPattern;
import com.github.xnaut97.cosmos.menu.animation.utility.SlotTraversal;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Gradually clears a region in slot order. Recommended for closing, removal, and reset transitions.
 */
@Getter
public class DrainAnimation extends BaseRegionAnimation {

    private final ItemStack filledItem;
    private final ItemStack emptyItem;
    private final FillPattern pattern;
    private final Long randomSeed;
    private final int[] traversal;

    @Builder
    public DrainAnimation(String id,
                          Long delayTicks,
                          Long periodTicks,
                          Boolean loop,
                          int[] slots,
                          ItemStack filledItem,
                          ItemStack emptyItem,
                          FillPattern pattern,
                          Long randomSeed,
                          Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                slots == null ? 0L : slots.length,
                AnimationSupport.bool(loop, false),
                slots,
                new ItemStack[]{filledItem == null ? AnimationItems.redPane() : filledItem},
                emptyItem,
                AnimationSupport.bool(clearOnStop, false));
        this.filledItem = filledItem == null ? AnimationItems.redPane() : filledItem;
        this.emptyItem = emptyItem;
        this.pattern = pattern == null ? FillPattern.SLOT_ORDER : pattern;
        this.randomSeed = randomSeed;
        this.traversal = SlotTraversal.order(getSlots(), this.pattern, this.randomSeed);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        int drained = (int) Math.min(slotCount(), frame + 1L);
        for (int i = 0; i < traversal.length; i++) {
            renderAt(menu, traversal[i], i < drained ? emptyItem : filledItem);
        }
    }
}
