package com.github.xnaut97.cosmos.menu.animation.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.abstraction.BaseRegionAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Rotating spinner for async loading states. Recommended default slots form a compact 3x3 spinner.
 */
@Getter
public class LoadingSpinnerAnimation extends BaseRegionAnimation {

    private static final int[] DEFAULT_SPINNER = {12, 13, 14, 23, 32, 31, 30, 21};

    private final ItemStack activeItem;
    private final ItemStack inactiveItem;
    private final boolean clockwise;
    private int previousActiveIndex = -1;

    @Builder
    public LoadingSpinnerAnimation(String id,
                                   Long delayTicks,
                                   Long periodTicks,
                                   Long maxFrames,
                                   Boolean loop,
                                   int[] slots,
                                   ItemStack activeItem,
                                   ItemStack inactiveItem,
                                   Boolean clockwise,
                                   Boolean clearOnStop) {
        super(id,
                AnimationSupport.nonNegative(delayTicks, 0L),
                AnimationSupport.ticks(periodTicks, 2L),
                AnimationSupport.nonNegative(maxFrames, 0L),
                AnimationSupport.bool(loop, true),
                orderSpinnerSlots(slots == null ? DEFAULT_SPINNER : slots, AnimationSupport.bool(clockwise, true)),
                new ItemStack[]{activeItem == null ? AnimationItems.cyanPane() : activeItem},
                inactiveItem == null ? AnimationItems.blackPane() : inactiveItem,
                AnimationSupport.bool(clearOnStop, true));
        this.activeItem = activeItem == null ? AnimationItems.cyanPane() : activeItem;
        this.inactiveItem = inactiveItem == null ? AnimationItems.blackPane() : inactiveItem;
        this.clockwise = AnimationSupport.bool(clockwise, true);
    }

    @Override
    protected void renderFrame(Menu menu, long frame) {
        if (!hasSlots()) {
            return;
        }

        int activeIndex = AnimationSupport.floorMod(frame, slotCount());
        if (previousActiveIndex >= 0 && previousActiveIndex != activeIndex) {
            renderAt(menu, previousActiveIndex, inactiveItem);
        } else if (previousActiveIndex < 0) {
            fill(menu, inactiveItem);
        }

        renderAt(menu, activeIndex, activeItem);
        previousActiveIndex = activeIndex;
    }

    @Override
    public void onStart(Menu menu) {
        super.onStart(menu);
        previousActiveIndex = -1;
    }

    private static int[] orderSpinnerSlots(int[] slots, boolean clockwise) {
        int[] ordered = AnimationSupport.slots(slots);
        if (ordered.length <= 2) {
            return ordered;
        }

        double centerRow = 0.0D;
        double centerColumn = 0.0D;
        SpinnerSlot[] points = new SpinnerSlot[ordered.length];
        for (int i = 0; i < ordered.length; i++) {
            int slot = ordered[i];
            int row = Math.floorDiv(slot, 9);
            int column = Math.floorMod(slot, 9);
            centerRow += row;
            centerColumn += column;
            points[i] = new SpinnerSlot(slot, row, column);
        }

        centerRow /= ordered.length;
        centerColumn /= ordered.length;
        double resolvedCenterRow = centerRow;
        double resolvedCenterColumn = centerColumn;
        Arrays.sort(points, Comparator.comparingDouble(point ->
                Math.atan2(point.row - resolvedCenterRow, point.column - resolvedCenterColumn)));

        int startIndex = 0;
        for (int i = 1; i < points.length; i++) {
            SpinnerSlot current = points[i];
            SpinnerSlot previous = points[startIndex];
            int rowComparison = Integer.compare(current.row, previous.row);
            if (rowComparison < 0 || rowComparison == 0
                    && Math.abs(current.column - resolvedCenterColumn) < Math.abs(previous.column - resolvedCenterColumn)) {
                startIndex = i;
            }
        }

        int[] rotated = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            rotated[i] = points[(startIndex + i) % points.length].slot;
        }

        if (clockwise) {
            return rotated;
        }

        int[] reversed = new int[rotated.length];
        reversed[0] = rotated[0];
        for (int i = 1; i < rotated.length; i++) {
            reversed[i] = rotated[rotated.length - i];
        }
        return reversed;
    }

    @Getter
    @AllArgsConstructor
    @Accessors(fluent = true)
    private static class SpinnerSlot {
        int slot;
        int row;
        int column;
    }
}
