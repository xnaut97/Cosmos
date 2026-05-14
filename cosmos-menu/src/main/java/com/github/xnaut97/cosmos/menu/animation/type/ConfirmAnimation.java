package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Finite green confirmation pulse. Recommended for successful transaction and save confirmations.
 */
@Getter
public class ConfirmAnimation extends BaseRegionAnimation {

    private final int holdFrames;

    @Builder
    public ConfirmAnimation(String id,
                            Long delayTicks,
                            Long periodTicks,
                            Integer pulses,
                            int[] slots,
                            ItemStack[] items,
                            ItemStack backgroundItem,
                            Integer holdFrames,
                            Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                (long) AnimationSupport.positive(pulses, 2) * AnimationSupport.positive(holdFrames, 3) * 4L,
                false,
                slots,
                items == null ? new ItemStack[]{AnimationItems.greenPane(), AnimationItems.limePane(), AnimationItems.whitePane(), AnimationItems.limePane()} : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.holdFrames = AnimationSupport.positive(holdFrames, 3);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        fill(menu, paletteAt(frame / holdFrames));
    }
}
