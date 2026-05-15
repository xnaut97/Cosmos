package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Cycles rainbow glass through a region. Recommended for decorative borders or premium menu idle state.
 */
@Getter
public class RainbowAnimation extends BaseRegionAnimation {

    private final int colorWidth;

    @Builder
    public RainbowAnimation(String id,
                            Long delayTicks,
                            Long periodTicks,
                            Long maxFrames,
                            Boolean loop,
                            int[] slots,
                            ItemStack[] items,
                            Integer colorWidth,
                            ItemStack backgroundItem,
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
        this.colorWidth = AnimationSupport.positive(colorWidth, 1);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, paletteAt(frame + i / colorWidth));
        }
    }
}
