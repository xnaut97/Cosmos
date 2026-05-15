package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Randomly toggles slots between highlight and background. Recommended for magical or unstable loading states.
 */
@Getter
public class RandomFlickerAnimation extends BaseRegionAnimation {

    private final int changesPerFrame;
    private final ItemStack onItem;
    private final ItemStack offItem;
    private final Random random;
    private final boolean[] active;

    @Builder
    public RandomFlickerAnimation(String id,
                                  Long delayTicks,
                                  Long periodTicks,
                                  Long maxFrames,
                                  Boolean loop,
                                  int[] slots,
                                  ItemStack onItem,
                                  ItemStack offItem,
                                  Integer changesPerFrame,
                                  Long seed,
                                  Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                new ItemStack[]{onItem == null ? AnimationItems.whitePane() : onItem},
                offItem == null ? AnimationItems.blackPane() : offItem,
                AnimationSupport.bool(clearOnStop, true));
        this.changesPerFrame = AnimationSupport.positive(changesPerFrame, 3);
        this.onItem = onItem == null ? AnimationItems.whitePane() : onItem;
        this.offItem = offItem == null ? AnimationItems.blackPane() : offItem;
        this.random = new Random(seed == null ? System.nanoTime() : seed);
        this.active = new boolean[slotCount()];
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        if (!hasSlots()) {
            return;
        }

        for (int i = 0; i < changesPerFrame; i++) {
            int index = random.nextInt(slotCount());
            active[index] = !active[index];
            renderAt(menu, index, active[index] ? onItem : offItem);
        }
    }
}
