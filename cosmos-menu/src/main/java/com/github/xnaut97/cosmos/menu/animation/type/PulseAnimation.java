package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Pulses an entire region through a palette. Recommended for loading panels and premium backgrounds.
 */
@Getter
public class PulseAnimation extends BaseRegionAnimation {

    private final int holdFrames;

    @Builder
    public PulseAnimation(String id,
                          Long delayTicks,
                          Long periodTicks,
                          Long maxFrames,
                          Boolean loop,
                          int[] slots,
                          ItemStack[] items,
                          ItemStack backgroundItem,
                          Boolean clearOnStop,
                          Integer holdFrames) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 3L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                items == null ? AnimationItems.loadingPanes() : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.holdFrames = AnimationSupport.positive(holdFrames, 2);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        fill(menu, paletteAt(frame / holdFrames));
    }
}
