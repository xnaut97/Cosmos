package com.github.xnaut97.cosmos.menu.animation.abstraction;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@Getter
public abstract class BaseRegionAnimation extends AbstractRepeatingAnimation {

    private final int[] slots;
    private final ItemStack[] palette;
    private final ItemStack backgroundItem;
    private final boolean clearOnStop;
    private ItemStack[] lastRendered;
    private boolean[] initialized;

    protected BaseRegionAnimation(String id,
                                  long delayTicks,
                                  long periodTicks,
                                  long maxFrames,
                                  boolean loop,
                                  int[] slots,
                                  ItemStack[] palette,
                                  ItemStack backgroundItem,
                                  boolean clearOnStop) {
        super(id, delayTicks, periodTicks, maxFrames, loop);
        this.slots = AnimationSupport.slots(slots);
        this.palette = AnimationSupport.palette(palette, AnimationItems.whitePane());
        this.backgroundItem = backgroundItem;
        this.clearOnStop = clearOnStop;
        this.lastRendered = new ItemStack[this.slots.length];
        this.initialized = new boolean[this.slots.length];
    }

    @Override
    public void onStart(Menu<?> menu) {
        super.onStart(menu);
        if (lastRendered.length != slots.length) {
            lastRendered = new ItemStack[slots.length];
            initialized = new boolean[slots.length];
        } else {
            Arrays.fill(lastRendered, null);
            Arrays.fill(initialized, false);
        }
    }

    @Override
    public void onStop(Menu<?> menu) {
        if (!clearOnStop) {
            return;
        }
        for (int i = 0; i < slots.length; i++) {
            renderAt(menu, i, backgroundItem);
        }
    }

    protected final boolean hasSlots() {
        return slots.length > 0;
    }

    protected final int slotCount() {
        return slots.length;
    }

    protected final int slotAt(int index) {
        return slots[AnimationSupport.floorMod(index, slots.length)];
    }

    protected final ItemStack paletteAt(long index) {
        return AnimationSupport.itemAt(palette, index);
    }

    protected final void renderAt(Menu<?> menu, int regionIndex, ItemStack item) {
        if (regionIndex < 0 || regionIndex >= slots.length) {
            return;
        }
        if (initialized[regionIndex] && AnimationSupport.sameItem(lastRendered[regionIndex], item)) {
            return;
        }

        menu.renderSlot(slots[regionIndex], item);
        lastRendered[regionIndex] = item;
        initialized[regionIndex] = true;
    }

    protected final void renderAbsolute(Menu<?> menu, int slot, ItemStack item) {
        menu.renderSlot(slot, item);
    }

    protected final void fill(Menu<?> menu, ItemStack item) {
        for (int i = 0; i < slots.length; i++) {
            renderAt(menu, i, item);
        }
    }

    protected final void clear(Menu<?> menu) {
        fill(menu, backgroundItem);
    }
}
