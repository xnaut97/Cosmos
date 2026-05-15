package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fills a slot bar over time. Recommended slots: a single row or custom horizontal rectangle.
 */
@Getter
public class ProgressBarAnimation extends BaseRegionAnimation {

    private final long durationFrames;
    private final ItemStack filledItem;
    private final ItemStack emptyItem;
    private final boolean displayProgress;
    private final int decimalPlaces;
    private final String progressNameFormat;
    private final String progressLoreFormat;
    private final Map<Integer, ItemStack> progressItems = new HashMap<>();

    @Builder
    public ProgressBarAnimation(String id,
                                Long delayTicks,
                                Long periodTicks,
                                Long durationFrames,
                                Boolean loop,
                                int[] slots,
                                ItemStack filledItem,
                                ItemStack emptyItem,
                                Boolean displayProgress,
                                Integer decimalPlaces,
                                String progressNameFormat,
                                String progressLoreFormat,
                                Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.ticks(durationFrames, 40L),
                AnimationSupport.bool(loop, false),
                slots,
                new ItemStack[]{filledItem == null ? AnimationItems.limePane() : filledItem},
                emptyItem == null ? AnimationItems.blackPane() : emptyItem,
                AnimationSupport.bool(clearOnStop, false));
        this.durationFrames = AnimationSupport.ticks(durationFrames, 40L);
        this.filledItem = filledItem == null ? AnimationItems.limePane() : filledItem;
        this.emptyItem = emptyItem == null ? AnimationItems.blackPane() : emptyItem;
        this.displayProgress = AnimationSupport.bool(displayProgress, true);
        this.decimalPlaces = Math.min(4, Math.max(0, decimalPlaces == null ? 0 : decimalPlaces));
        this.progressNameFormat = progressNameFormat == null ? "&a{progress}%" : progressNameFormat;
        this.progressLoreFormat = progressLoreFormat == null ? "&7Progress: &f{progress}%" : progressLoreFormat;
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        int filled = (int) Math.min(slotCount(), ((frame + 1L) * slotCount() + durationFrames - 1L) / durationFrames);
        double percent = Math.min(100.0D, ((frame + 1.0D) * 100.0D) / durationFrames);
        ItemStack activeItem = progressItem(percent);
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, i < filled ? activeItem : emptyItem);
        }
    }

    private ItemStack progressItem(double percent) {
        if (!displayProgress) {
            return filledItem;
        }

        int key = progressKey(percent);
        ItemStack cached = progressItems.get(key);
        if (cached != null) {
            return cached;
        }

        String formattedProgress = formatProgress(percent);
        ItemStack item = AnimationItems.namedWithLore(
                filledItem,
                progressNameFormat.replace("{progress}", formattedProgress),
                Collections.singletonList(progressLoreFormat.replace("{progress}", formattedProgress)));
        progressItems.put(key, item);
        return item;
    }

    private String formatProgress(double percent) {
        return String.format(Locale.US, "%." + decimalPlaces + "f", percent);
    }

    private int progressKey(double percent) {
        int multiplier = 1;
        for (int i = 0; i < decimalPlaces; i++) {
            multiplier *= 10;
        }
        return (int) Math.round(percent * multiplier);
    }
}
