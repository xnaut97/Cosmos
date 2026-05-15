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
 * Traverses slots in spiral order. Recommended for first-open reveals and paginated content loading.
 */
@Getter
public class SpiralAnimation extends BaseRegionAnimation {

    private final ItemStack activeItem;
    private final ItemStack backgroundItem;
    private final int trailLength;

    @Builder
    public SpiralAnimation(String id,
                           Long delayTicks,
                           Long periodTicks,
                           Long maxFrames,
                           Boolean loop,
                           Integer rows,
                           int[] slots,
                           ItemStack[] items,
                           ItemStack activeItem,
                           ItemStack backgroundItem,
                           Integer trailLength,
                           Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots == null ? SlotPatterns.spiral(rows == null ? 6 : rows) : slots,
                items == null ? new ItemStack[]{activeItem == null ? AnimationItems.cyanPane() : activeItem,
                        AnimationItems.bluePane(), AnimationItems.purplePane()} : items,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.activeItem = activeItem == null ? AnimationItems.cyanPane() : activeItem;
        this.backgroundItem = backgroundItem;
        this.trailLength = AnimationSupport.positive(trailLength, 6);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        clear(menu);
        for (int i = 0; i < trailLength; i++) {
            renderAt(menu, AnimationSupport.floorMod(frame - i, slotCount()), i == 0 ? activeItem : paletteAt(i));
        }
    }
}
