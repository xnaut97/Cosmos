package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import com.github.xnaut97.cosmos.menu.animation.utility.SlotPatterns;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Cascading vertical slot trails. Recommended for loading screens and admin/storage menus.
 */
@Getter
public class MatrixAnimation extends BaseRegionAnimation {

    private final int rows;
    private final int trailLength;
    private final ItemStack backgroundItem;

    @Builder
    public MatrixAnimation(String id,
                           Long delayTicks,
                           Long periodTicks,
                           Long maxFrames,
                           Boolean loop,
                           Integer rows,
                           int[] slots,
                           ItemStack[] items,
                           ItemStack backgroundItem,
                           Integer trailLength,
                           Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                slots == null ? SlotPatterns.all(rows == null ? 6 : rows) : slots,
                items == null ? new ItemStack[]{AnimationItems.limePane(), AnimationItems.greenPane(), AnimationItems.blackPane()} : items,
                backgroundItem == null ? AnimationItems.blackPane() : backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.rows = SlotPatterns.normalizeRows(rows == null ? 6 : rows);
        this.trailLength = AnimationSupport.positive(trailLength, 4);
        this.backgroundItem = backgroundItem == null ? AnimationItems.blackPane() : backgroundItem;
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        int cycle = rows + trailLength;
        for (int i = 0; i < slotCount(); i++) {
            int slot = slotAt(i);
            int row = slot / SlotPatterns.COLUMNS;
            int column = slot % SlotPatterns.COLUMNS;
            int phase = AnimationSupport.floorMod(frame + column * 2L - row, cycle);
            renderAt(menu, i, phase < trailLength ? paletteAt(phase) : backgroundItem);
        }
    }
}
