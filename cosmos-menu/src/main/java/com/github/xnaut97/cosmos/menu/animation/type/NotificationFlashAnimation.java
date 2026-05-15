package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Finite full-region flash for important events. Recommended for warnings and completed actions.
 */
@Getter
public class NotificationFlashAnimation extends BaseRegionAnimation {

    private final int flashFrames;
    private final ItemStack flashItem;
    private final ItemStack backgroundItem;

    @Builder
    public NotificationFlashAnimation(String id,
                                      Long delayTicks,
                                      Long periodTicks,
                                      Integer flashes,
                                      int[] slots,
                                      ItemStack flashItem,
                                      ItemStack backgroundItem,
                                      Integer flashFrames,
                                      Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                (long) AnimationSupport.positive(flashes, 3) * AnimationSupport.positive(flashFrames, 3) * 2L,
                false,
                slots,
                new ItemStack[]{flashItem == null ? AnimationItems.yellowPane() : flashItem},
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.flashFrames = AnimationSupport.positive(flashFrames, 3);
        this.flashItem = flashItem == null ? AnimationItems.yellowPane() : flashItem;
        this.backgroundItem = backgroundItem;
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        boolean on = (frame / flashFrames) % 2L == 0L;
        fill(menu, on ? flashItem : backgroundItem);
    }
}
