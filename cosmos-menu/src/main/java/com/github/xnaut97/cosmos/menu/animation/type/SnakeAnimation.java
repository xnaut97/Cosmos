package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Moves a snake trail through a slot path. Recommended slots: border, spiral, or custom route arrays.
 */
@Getter
public class SnakeAnimation extends BaseRegionAnimation {

    private final int trailLength;
    private final boolean reverse;

    @Builder
    public SnakeAnimation(String id,
                          Long delayTicks,
                          Long periodTicks,
                          Long maxFrames,
                          Boolean loop,
                          int[] slots,
                          ItemStack[] items,
                          ItemStack backgroundItem,
                          Boolean clearOnStop,
                          Integer trailLength,
                          Boolean reverse) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                items == null ? AnimationItems.premiumPanes() : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.trailLength = AnimationSupport.positive(trailLength, 5);
        this.reverse = AnimationSupport.bool(reverse, false);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        if (!hasSlots()) {
            return;
        }

        clear(menu);
        for (int trail = 0; trail < trailLength; trail++) {
            int index = AnimationSupport.floorMod((reverse ? -frame : frame) - trail, slotCount());
            renderAt(menu, index, paletteAt(trail));
        }
    }
}
