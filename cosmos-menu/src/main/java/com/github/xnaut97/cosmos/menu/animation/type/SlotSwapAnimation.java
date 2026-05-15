package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.AbstractRepeatingAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Visualizes two slots swapping items. Recommended for sorting and storage menus.
 */
@Getter
public class SlotSwapAnimation extends AbstractRepeatingAnimation {

    private final int firstSlot;
    private final int secondSlot;
    private final long durationFrames;
    private final ItemStack firstItem;
    private final ItemStack secondItem;
    private final ItemStack transitionItem;

    @Builder
    public SlotSwapAnimation(String id,
                             Long delayTicks,
                             Long periodTicks,
                             Long durationFrames,
                             Integer firstSlot,
                             Integer secondSlot,
                             ItemStack firstItem,
                             ItemStack secondItem,
                             ItemStack transitionItem) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.ticks(durationFrames, 8L),
                false);
        this.firstSlot = firstSlot == null ? 20 : firstSlot;
        this.secondSlot = secondSlot == null ? 24 : secondSlot;
        this.durationFrames = AnimationSupport.ticks(durationFrames, 8L);
        this.firstItem = firstItem == null ? AnimationItems.bluePane() : firstItem;
        this.secondItem = secondItem == null ? AnimationItems.purplePane() : secondItem;
        this.transitionItem = transitionItem == null ? AnimationItems.whitePane() : transitionItem;
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        if (frame + 1L >= durationFrames) {
            menu.renderSlot(firstSlot, secondItem);
            menu.renderSlot(secondSlot, firstItem);
            return;
        }

        boolean flash = frame % 2L == 0L;
        menu.renderSlot(firstSlot, flash ? transitionItem : firstItem);
        menu.renderSlot(secondSlot, flash ? transitionItem : secondItem);
    }
}
