package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Blinks target slots between two items. Recommended for confirm/cancel buttons and important controls.
 */
@Getter
public class BlinkAnimation extends BaseRegionAnimation {

    private final int intervalFrames;
    private final ItemStack onItem;
    private final ItemStack offItem;

    @Builder
    public BlinkAnimation(String id,
                          Long delayTicks,
                          Long periodTicks,
                          Long maxFrames,
                          Boolean loop,
                          int[] slots,
                          ItemStack onItem,
                          ItemStack offItem,
                          Integer intervalFrames,
                          Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                new ItemStack[]{onItem == null ? AnimationItems.yellowPane() : onItem},
                offItem,
                AnimationSupport.bool(clearOnStop, false));
        this.intervalFrames = AnimationSupport.positive(intervalFrames, 4);
        this.onItem = onItem == null ? AnimationItems.yellowPane() : onItem;
        this.offItem = offItem;
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        boolean on = (frame / intervalFrames) % 2L == 0L;
        fill(menu, on ? onItem : offItem);
    }
}
