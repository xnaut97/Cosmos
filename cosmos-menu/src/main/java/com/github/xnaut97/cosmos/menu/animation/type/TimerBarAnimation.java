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
 * Draining timer bar. Recommended for temporary offers, cooldowns, and transaction timeouts.
 */
@Getter
public class TimerBarAnimation extends BaseRegionAnimation {

    private final long durationFrames;
    private final ItemStack timeItem;
    private final ItemStack emptyItem;
    private final boolean displayProgress;
    private final int decimalPlaces;
    private final String progressNameFormat;
    private final String progressLoreFormat;
    private final Map<Integer, ItemStack> progressItems = new HashMap<>();

    @Builder
    public TimerBarAnimation(String id,
                             Long delayTicks,
                             Long periodTicks,
                             Long durationFrames,
                             int[] slots,
                             ItemStack timeItem,
                             ItemStack emptyItem,
                             Boolean displayProgress,
                             Integer decimalPlaces,
                             String progressNameFormat,
                             String progressLoreFormat,
                             Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                AnimationSupport.ticks(durationFrames, 100L) + 1L,
                false,
                slots,
                new ItemStack[]{timeItem == null ? AnimationItems.yellowPane() : timeItem},
                emptyItem == null ? AnimationItems.blackPane() : emptyItem,
                AnimationSupport.bool(clearOnStop, false));
        this.durationFrames = AnimationSupport.ticks(durationFrames, 100L);
        this.timeItem = timeItem == null ? AnimationItems.yellowPane() : timeItem;
        this.emptyItem = emptyItem == null ? AnimationItems.blackPane() : emptyItem;
        this.displayProgress = AnimationSupport.bool(displayProgress, true);
        this.decimalPlaces = Math.min(4, Math.max(0, decimalPlaces == null ? 0 : decimalPlaces));
        this.progressNameFormat = progressNameFormat == null ? "&eTimer {progress}%" : progressNameFormat;
        this.progressLoreFormat = progressLoreFormat == null
                ? "&7Remaining: &f{remaining_seconds}s &8({remaining_ticks} ticks)"
                : progressLoreFormat;
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        long remainingFrames = Math.max(0L, durationFrames - frame);
        int remaining = (int) Math.min(slotCount(), (remainingFrames * slotCount() + durationFrames - 1L) / durationFrames);
        ItemStack activeItem = progressItem(frame, remainingFrames);
        for (int i = 0; i < slotCount(); i++) {
            renderAt(menu, i, i < remaining ? activeItem : emptyItem);
        }
    }

    private ItemStack progressItem(long frame, long remainingFrames) {
        if (!displayProgress) {
            return timeItem;
        }

        double progress = Math.min(100.0D, Math.max(0.0D, (frame * 100.0D) / durationFrames));
        long remainingTicks = Math.max(0L, remainingFrames * getPeriodTicks());
        int key = progressKey(progress, remainingTicks);
        ItemStack cached = progressItems.get(key);
        if (cached != null) {
            return cached;
        }

        String formattedProgress = formatProgress(progress);
        String remainingSeconds = String.format(Locale.US, "%.1f", remainingTicks / 20.0D);
        ItemStack item = AnimationItems.namedWithLore(
                timeItem,
                applyFormat(progressNameFormat, formattedProgress, remainingFrames, remainingTicks, remainingSeconds),
                Collections.singletonList(applyFormat(progressLoreFormat, formattedProgress, remainingFrames, remainingTicks, remainingSeconds)));
        progressItems.put(key, item);
        return item;
    }

    private String applyFormat(String format,
                               String progress,
                               long remainingFrames,
                               long remainingTicks,
                               String remainingSeconds) {
        return format
                .replace("{progress}", progress)
                .replace("{remaining_frames}", Long.toString(remainingFrames))
                .replace("{remaining_ticks}", Long.toString(remainingTicks))
                .replace("{remaining_seconds}", remainingSeconds);
    }

    private String formatProgress(double progress) {
        return String.format(Locale.US, "%." + decimalPlaces + "f", progress);
    }

    private int progressKey(double progress, long remainingTicks) {
        int multiplier = 1;
        for (int i = 0; i < decimalPlaces; i++) {
            multiplier *= 10;
        }
        int progressKey = (int) Math.round(progress * multiplier);
        return 31 * progressKey + (int) Math.min(Integer.MAX_VALUE, remainingTicks);
    }
}
