package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Bounces a highlight back and forth along a path. Recommended for attention on pagination arrows.
 */
@Getter
public class BounceAnimation extends BaseRegionAnimation {

    private final ItemStack bounceItem;
    private final ItemStack backgroundItem;

    @Builder
    public BounceAnimation(String id,
                           Long delayTicks,
                           Long periodTicks,
                           Long maxFrames,
                           Boolean loop,
                           int[] slots,
                           ItemStack bounceItem,
                           ItemStack backgroundItem,
                           Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                new ItemStack[]{bounceItem == null ? AnimationItems.yellowPane() : bounceItem},
                backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.bounceItem = bounceItem == null ? AnimationItems.yellowPane() : bounceItem;
        this.backgroundItem = backgroundItem;
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        if (!hasSlots()) {
            return;
        }

        clear(menu);
        renderAt(menu, AnimationSupport.pingPong(frame, slotCount()), bounceItem);
    }
}
