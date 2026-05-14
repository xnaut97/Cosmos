package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Finite red shake/flash effect. Recommended for validation errors and rejected transactions.
 */
@Getter
public class ErrorAnimation extends BaseRegionAnimation {

    private final int shakeFrames;
    private final ItemStack errorItem;
    private final ItemStack backgroundItem;

    @Builder
    public ErrorAnimation(String id,
                          Long delayTicks,
                          Long periodTicks,
                          Integer shakes,
                          int[] slots,
                          ItemStack errorItem,
                          ItemStack backgroundItem,
                          Integer shakeFrames,
                          Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                (long) AnimationSupport.positive(shakes, 4) * AnimationSupport.positive(shakeFrames, 2),
                false,
                slots,
                new ItemStack[]{errorItem == null ? AnimationItems.redPane() : errorItem},
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.shakeFrames = AnimationSupport.positive(shakeFrames, 2);
        this.errorItem = errorItem == null ? AnimationItems.redPane() : errorItem;
        this.backgroundItem = backgroundItem;
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        int offset = (int) ((frame / shakeFrames) % 2L);
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, i % 2 == offset ? errorItem : backgroundItem);
        }
    }
}
