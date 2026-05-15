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
 * Light trail constrained to a menu border. Recommended for premium idle borders and selected pages.
 */
@Getter
public class BorderTrailAnimation extends BaseRegionAnimation {

    private final int trailLength;
    private final ItemStack trailHeadItem;
    private final ItemStack backgroundItem;

    @Builder
    public BorderTrailAnimation(String id,
                                Long delayTicks,
                                Long periodTicks,
                                Long maxFrames,
                                Boolean loop,
                                Integer rows,
                                int[] slots,
                                ItemStack[] items,
                                ItemStack trailHeadItem,
                                ItemStack backgroundItem,
                                Integer trailLength,
                                Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots == null ? SlotPatterns.borderClockwise(rows == null ? 6 : rows) : slots,
                items == null ? AnimationItems.premiumPanes() : items,
                backgroundItem == null ? AnimationItems.blackPane() : backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.trailLength = AnimationSupport.positive(trailLength, 7);
        this.trailHeadItem = trailHeadItem == null ? AnimationItems.whitePane() : trailHeadItem;
        this.backgroundItem = backgroundItem == null ? AnimationItems.blackPane() : backgroundItem;
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        clear(menu);
        for (int i = 0; i < trailLength; i++) {
            int index = AnimationSupport.floorMod(frame - i, slotCount());
            renderAt(menu, index, i == 0 ? trailHeadItem : paletteAt(i));
        }
    }
}
