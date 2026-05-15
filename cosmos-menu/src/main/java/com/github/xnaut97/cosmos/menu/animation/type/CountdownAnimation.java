package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.AbstractRepeatingAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Visual countdown rendered in one slot. Recommended for confirmation dialogs and delayed actions.
 */
@Getter
public class CountdownAnimation extends AbstractRepeatingAnimation {

    private final int slot;
    private final int seconds;
    private final int framesPerSecond;
    private final ItemStack baseItem;
    private final String prefix;
    private final String displayFormat;
    private final String loreFormat;
    private final boolean useAmount;
    private final Map<Integer, ItemStack> countdownItems = new HashMap<>();

    @Builder
    public CountdownAnimation(String id,
                              Long delayTicks,
                              Long periodTicks,
                              Integer slot,
                              Integer seconds,
                              Integer framesPerSecond,
                              ItemStack baseItem,
                              String prefix,
                              String displayFormat,
                              String loreFormat,
                              Boolean useAmount) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 1L),
                (long) AnimationSupport.positive(seconds, 5) * AnimationSupport.positive(framesPerSecond, 20) + 1L,
                false);
        this.slot = slot == null ? 22 : slot;
        this.seconds = AnimationSupport.positive(seconds, 5);
        this.framesPerSecond = AnimationSupport.positive(framesPerSecond, 20);
        this.baseItem = baseItem == null ? new ItemStack(Material.CLOCK) : baseItem;
        this.prefix = prefix == null ? "&e" : prefix;
        this.displayFormat = displayFormat == null ? this.prefix + "{seconds}" : displayFormat;
        this.loreFormat = loreFormat == null ? "&7Remaining: &f{time}" : loreFormat;
        this.useAmount = AnimationSupport.bool(useAmount, true);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        int remaining = Math.max(0, seconds - (int) (frame / framesPerSecond));
        menu.renderSlot(slot, countdownItem(remaining));
    }

    private ItemStack countdownItem(int remaining) {
        return countdownItems.computeIfAbsent(remaining, this::buildItem);
    }

    private ItemStack buildItem(int remaining) {
        String display = applyFormat(displayFormat, remaining);
        String lore = applyFormat(loreFormat, remaining);
        ItemStack item = AnimationItems.namedWithLore(baseItem, display, Collections.singletonList(lore));
        if (useAmount) {
            item.setAmount(amountFor(remaining, item.getMaxStackSize()));
        }
        return item;
    }

    private int amountFor(int remaining, int maxStackSize) {
        if (remaining <= 0 || remaining > maxStackSize) {
            return 1;
        }
        return remaining;
    }

    private String applyFormat(String format, int remaining) {
        return format
                .replace("{seconds}", Integer.toString(remaining))
                .replace("{time}", formatDuration(remaining))
                .replace("{minutes}", String.format(Locale.US, "%.2f", remaining / 60.0D))
                .replace("{hours}", String.format(Locale.US, "%.2f", remaining / 3600.0D));
    }

    private String formatDuration(int remaining) {
        if (remaining >= 3600) {
            int hours = remaining / 3600;
            int minutes = (remaining % 3600) / 60;
            int secondsPart = remaining % 60;
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secondsPart);
        }
        if (remaining >= 60) {
            return String.format(Locale.US, "%02d:%02d", remaining / 60, remaining % 60);
        }
        return Integer.toString(remaining);
    }
}
