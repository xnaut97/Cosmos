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
 * Clockwise or counter-clockwise color movement around a menu border.
 * Recommended slots: {@link SlotPatterns#borderClockwise(int)}.
 */
@Getter
public class BorderAnimation extends BaseRegionAnimation {

    private final boolean clockwise;

    @Builder
    public BorderAnimation(String id,
                           Long delayTicks,
                           Long periodTicks,
                           Long maxFrames,
                           Boolean loop,
                           Integer rows,
                           int[] slots,
                           ItemStack[] items,
                           ItemStack backgroundItem,
                           Boolean clearOnStop,
                           Boolean clockwise) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots == null ? SlotPatterns.borderClockwise(rows == null ? 6 : rows) : slots,
                items == null ? AnimationItems.premiumPanes() : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.clockwise = AnimationSupport.bool(clockwise, true);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        if (!hasSlots()) {
            return;
        }

        for (int i = 0; i < slotCount(); i++) {
            long offset = clockwise ? i - frame : i + frame;
            renderAt(menu, i, paletteAt(offset));
        }
    }
}
