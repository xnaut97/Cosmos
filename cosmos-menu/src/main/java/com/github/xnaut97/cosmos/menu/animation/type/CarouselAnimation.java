package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Rotates a set of items around a slot path. Recommended slots: circle or rectangle border.
 */
@Getter
public class CarouselAnimation extends BaseRegionAnimation {

    private final boolean reverse;

    @Builder
    public CarouselAnimation(String id,
                             Long delayTicks,
                             Long periodTicks,
                             Long maxFrames,
                             Boolean loop,
                             int[] slots,
                             ItemStack[] items,
                             ItemStack backgroundItem,
                             Boolean reverse,
                             Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                items == null ? AnimationItems.rainbowPanes() : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.reverse = AnimationSupport.bool(reverse, false);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        long offset = reverse ? frame : -frame;
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, paletteAt(i + offset));
        }
    }
}
