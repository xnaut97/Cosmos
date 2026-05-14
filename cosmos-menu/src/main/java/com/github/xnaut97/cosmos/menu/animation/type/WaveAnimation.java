package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Sends a rolling color wave across a region. Recommended for storage and pagination item areas.
 */
@Getter
public class WaveAnimation extends BaseRegionAnimation {

    private final int waveWidth;

    @Builder
    public WaveAnimation(String id,
                         Long delayTicks,
                         Long periodTicks,
                         Long maxFrames,
                         Boolean loop,
                         int[] slots,
                         ItemStack[] items,
                         ItemStack backgroundItem,
                         Boolean clearOnStop,
                         Integer waveWidth) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                items == null ? AnimationItems.rainbowPanes() : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.waveWidth = AnimationSupport.positive(waveWidth, 2);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, paletteAt((i - frame) / waveWidth));
        }
    }
}
