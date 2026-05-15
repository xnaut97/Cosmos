package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Smooth ping-pong pulse through a palette. Recommended for premium idle states and subtle confirmation panels.
 */
@Getter
public class BreathingAnimation extends BaseRegionAnimation {

    private final int holdFrames;

    @Builder
    public BreathingAnimation(String id,
                              Long delayTicks,
                              Long periodTicks,
                              Long maxFrames,
                              Boolean loop,
                              int[] slots,
                              ItemStack[] items,
                              ItemStack backgroundItem,
                              Integer holdFrames,
                              Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                items == null ? AnimationItems.loadingPanes() : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.holdFrames = AnimationSupport.positive(holdFrames, 2);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        fill(menu, paletteAt(AnimationSupport.pingPong(frame / holdFrames, getPalette().length)));
    }
}
