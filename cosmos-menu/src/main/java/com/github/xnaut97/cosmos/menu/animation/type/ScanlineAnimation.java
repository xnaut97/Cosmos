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
 * Moves a row or column highlight across the menu. Recommended slots: full menu region.
 */
@Getter
public class ScanlineAnimation extends BaseRegionAnimation {

    private final boolean horizontal;
    private final int rows;
    private final ItemStack scanItem;
    private final ItemStack backgroundItem;
    private final long durationFrames;
    private final int scanCount;

    @Builder
    public ScanlineAnimation(String id,
                             Long delayTicks,
                             Long periodTicks,
                             Long maxFrames,
                             Long durationFrames,
                             Integer scanCount,
                             Boolean loop,
                             Integer rows,
                             int[] slots,
                             ItemStack scanItem,
                             ItemStack backgroundItem,
                             Boolean horizontal,
                             Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                resolveMaxFrames(maxFrames, durationFrames, scanCount, horizontal, rows),
                resolveLoop(loop, maxFrames, durationFrames, scanCount),
                slots == null ? SlotPatterns.all(rows == null ? 6 : rows) : slots,
                new ItemStack[]{scanItem == null ? AnimationItems.lightGrayPane() : scanItem},
                backgroundItem == null ? AnimationItems.blackPane() : backgroundItem,
                AnimationSupport.bool(clearOnStop, true));
        this.horizontal = AnimationSupport.bool(horizontal, true);
        this.rows = SlotPatterns.normalizeRows(rows == null ? 6 : rows);
        this.scanItem = scanItem == null ? AnimationItems.lightGrayPane() : scanItem;
        this.backgroundItem = backgroundItem == null ? AnimationItems.blackPane() : backgroundItem;
        this.durationFrames = durationFrames == null ? 0L : Math.max(0L, durationFrames);
        this.scanCount = scanCount == null ? 0 : Math.max(0, scanCount);
    }

    @Override
    protected void renderFrame(Menu<?> menu, long frame) {
        int active = activeLine(frame);

        for (int i = 0; i < slotCount(); i++) {
            int slot = slotAt(i);
            int row = slot / SlotPatterns.COLUMNS;
            int column = slot % SlotPatterns.COLUMNS;
            boolean lit = horizontal ? row == active : column == active;
            renderAt(menu, i, lit ? scanItem : backgroundItem);
        }
    }

    private int activeLine(long frame) {
        int lineCount = horizontal ? rows : SlotPatterns.COLUMNS;
        if (lineCount <= 1) {
            return 0;
        }

        if (durationFrames > 0L) {
            long normalizedFrame = loop()
                    ? AnimationSupport.floorMod(frame, (int) Math.min(Integer.MAX_VALUE, durationFrames))
                    : Math.min(frame, durationFrames - 1L);
            return (int) Math.min(lineCount - 1L, (normalizedFrame * lineCount) / durationFrames);
        }

        return AnimationSupport.floorMod(frame, lineCount);
    }

    private static long resolveMaxFrames(Long maxFrames,
                                         Long durationFrames,
                                         Integer scanCount,
                                         Boolean horizontal,
                                         Integer rows) {
        if (maxFrames != null) {
            return Math.max(0L, maxFrames);
        }
        if (durationFrames != null && durationFrames > 0L) {
            return durationFrames;
        }
        if (scanCount != null && scanCount > 0) {
            int lineCount = AnimationSupport.bool(horizontal, true)
                    ? SlotPatterns.normalizeRows(rows == null ? 6 : rows)
                    : SlotPatterns.COLUMNS;
            return (long) scanCount * lineCount;
        }
        return 0L;
    }

    private static boolean resolveLoop(Boolean loop, Long maxFrames, Long durationFrames, Integer scanCount) {
        if (loop != null) {
            return loop;
        }
        return maxFrames == null && durationFrames == null && scanCount == null;
    }
}
