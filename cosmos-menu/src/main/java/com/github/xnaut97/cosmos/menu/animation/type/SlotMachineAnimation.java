package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Rolls items through reel slots, then lands on result items. Recommended for crates and reward previews.
 */
@Getter
public class SlotMachineAnimation extends BaseRegionAnimation {

    private final long durationFrames;
    private final ItemStack[] resultItems;

    @Builder
    public SlotMachineAnimation(String id,
                                Long delayTicks,
                                Long periodTicks,
                                Long durationFrames,
                                int[] slots,
                                ItemStack[] reelItems,
                                ItemStack[] resultItems,
                                ItemStack backgroundItem,
                                Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.ticks(durationFrames, 40L),
                false,
                slots,
                reelItems == null ? AnimationItems.rainbowPanes() : reelItems,
                backgroundItem,
                AnimationSupport.bool(clearOnStop, false));
        this.durationFrames = AnimationSupport.ticks(durationFrames, 40L);
        this.resultItems = AnimationSupport.palette(resultItems, AnimationItems.emerald());
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        boolean finished = frame + 1L >= durationFrames;
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, finished ? resultItems[i % resultItems.length] : paletteAt(frame + i * 3L));
        }
    }
}
