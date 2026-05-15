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
 * Random short-lived sparkle highlights. Recommended for premium rewards and success states.
 */
@Getter
public class SparkleAnimation extends BaseRegionAnimation {

    private final int sparklesPerFrame;
    private final int sparkleLifetimeFrames;
    private final ItemStack sparkleItem;
    private final ItemStack backgroundItem;
    private final int[] ages;
    private final Random random;

    @Builder
    public SparkleAnimation(String id,
                            Long delayTicks,
                            Long periodTicks,
                            Long maxFrames,
                            Boolean loop,
                            int[] slots,
                            ItemStack sparkleItem,
                            ItemStack backgroundItem,
                            Integer sparklesPerFrame,
                            Integer sparkleLifetimeFrames,
                            Long seed,
                            Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots,
                new ItemStack[]{sparkleItem == null ? AnimationItems.sparkle() : sparkleItem},
                backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.sparkleItem = sparkleItem == null ? AnimationItems.sparkle() : sparkleItem;
        this.backgroundItem = backgroundItem;
        this.sparklesPerFrame = AnimationSupport.positive(sparklesPerFrame, 2);
        this.sparkleLifetimeFrames = AnimationSupport.positive(sparkleLifetimeFrames, 3);
        this.ages = new int[slotCount()];
        this.random = new Random(seed == null ? System.nanoTime() : seed);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        if (!hasSlots()) {
            return;
        }

        for (int i = 0; i < slotCount(); i++) {
            if (ages[i] > 0) {
                ages[i]--;
                renderAt(menu, i, ages[i] > 0 ? sparkleItem : backgroundItem);
            }
        }

        for (int i = 0; i < sparklesPerFrame; i++) {
            int index = random.nextInt(slotCount());
            ages[index] = sparkleLifetimeFrames;
            renderAt(menu, index, sparkleItem);
        }
    }
}
