package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.AbstractRepeatingAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Simulates typing text into a single display slot. Recommended for loading placeholders and status cells.
 */
@Getter
public class TypingAnimation extends AbstractRepeatingAnimation {

    private final int slot;
    private final String text;
    private final String prefix;
    private final ItemStack baseItem;
    private final Number number;
    private final Double percentage;
    private final Supplier<?> valueSupplier;
    private final String valueFormat;
    private ItemStack[] frames;
    private String renderedText;

    @Builder
    public TypingAnimation(String id,
                           Long delayTicks,
                           Long periodTicks,
                           Long maxFrames,
                           Boolean loop,
                           Integer slot,
                           String text,
                           String prefix,
                           Number number,
                           Double percentage,
                           Supplier<?> valueSupplier,
                           String valueFormat,
                           ItemStack baseItem) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true));
        this.slot = slot == null ? 22 : slot;
        this.text = text == null ? "Loading..." : text;
        this.prefix = prefix == null ? "&e" : prefix;
        this.baseItem = baseItem == null ? new ItemStack(Material.PAPER) : baseItem;
        this.number = number;
        this.percentage = percentage;
        this.valueSupplier = valueSupplier;
        this.valueFormat = valueFormat;
        this.renderedText = resolveText();
        this.frames = buildFrames(this.baseItem, this.prefix, this.renderedText);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        refreshFramesIfNeeded();
        menu.renderSlot(slot, frames[AnimationSupport.floorMod(frame, frames.length)]);
    }

    private void refreshFramesIfNeeded() {
        String resolvedText = resolveText();
        if (Objects.equals(renderedText, resolvedText)) {
            return;
        }

        renderedText = resolvedText;
        frames = buildFrames(baseItem, prefix, renderedText);
    }

    private String resolveText() {
        if (valueSupplier != null) {
            Object value = valueSupplier.get();
            return value == null ? "" : formatValue(value, false);
        }
        if (percentage != null) {
            return formatValue(percentage, true);
        }
        if (number != null) {
            return formatValue(number, false);
        }
        return text;
    }

    private String formatValue(Object value, boolean percentageValue) {
        if (valueFormat != null && !valueFormat.isBlank()) {
            try {
                return String.format(Locale.US, valueFormat, value);
            } catch (IllegalFormatException ignored) {
                return String.valueOf(value);
            }
        }

        if (percentageValue) {
            return String.format(Locale.US, "%.1f%%", ((Number) value).doubleValue());
        }
        return String.valueOf(value);
    }

    private static ItemStack[] buildFrames(ItemStack baseItem, String prefix, String text) {
        int length = Math.max(1, text.length());
        ItemStack[] result = new ItemStack[length + 1];
        result[0] = AnimationItems.named(baseItem, prefix);
        for (int i = 1; i <= length; i++) {
            result[i] = AnimationItems.named(baseItem, prefix + text.substring(0, i));
        }
        return result;
    }
}
