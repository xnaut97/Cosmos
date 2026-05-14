package com.github.xnaut97.cosmos.menu.animation.utility;

import com.github.xnaut97.cosmos.menu.animation.Animation;
import com.github.xnaut97.cosmos.menu.animation.type.*;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public final class AnimationPresets {

    private AnimationPresets() {
    }

    public static PresetBuilder loadingPreset(String id) {
        return new PresetBuilder(id, PresetType.LOADING)
                .periodTicks(2L)
                .primaryItem(AnimationItems.cyanPane())
                .secondaryItem(AnimationItems.blackPane())
                .text("Loading...");
    }

    public static PresetBuilder asyncLoadingPreset(String id) {
        return new PresetBuilder(id, PresetType.ASYNC_LOADING)
                .periodTicks(2L)
                .primaryItem(AnimationItems.cyanPane())
                .secondaryItem(AnimationItems.blackPane())
                .palette(AnimationItems.loadingPanes())
                .text("Loading data...");
    }

    public static PresetBuilder borderPulsePreset(String id) {
        return new PresetBuilder(id, PresetType.BORDER_PULSE)
                .periodTicks(3L)
                .holdFrames(2)
                .palette(AnimationItems.loadingPanes())
                .backgroundItem(AnimationItems.blackPane());
    }

    public static PresetBuilder confirmPreset(String id) {
        return new PresetBuilder(id, PresetType.CONFIRM)
                .periodTicks(1L)
                .pulses(2)
                .holdFrames(3)
                .palette(new ItemStack[]{AnimationItems.greenPane(), AnimationItems.limePane(), AnimationItems.whitePane()});
    }

    public static PresetBuilder errorPreset(String id) {
        return new PresetBuilder(id, PresetType.ERROR)
                .periodTicks(1L)
                .shakes(6)
                .holdFrames(2)
                .primaryItem(AnimationItems.redPane())
                .backgroundItem(AnimationItems.blackPane());
    }

    public static PresetBuilder countdownPreset(String id) {
        return new PresetBuilder(id, PresetType.COUNTDOWN)
                .periodTicks(1L)
                .seconds(5)
                .framesPerSecond(20)
                .displayFormat("&e{time}")
                .loreFormat("&7Remaining: &f{seconds}s");
    }

    public static PresetBuilder rainbowBorderPreset(String id) {
        return new PresetBuilder(id, PresetType.RAINBOW_BORDER)
                .periodTicks(2L)
                .colorWidth(2)
                .palette(AnimationItems.rainbowPanes());
    }

    public static PresetBuilder successFlashPreset(String id) {
        return new PresetBuilder(id, PresetType.SUCCESS_FLASH)
                .periodTicks(1L)
                .flashes(3)
                .flashFrames(3)
                .primaryItem(AnimationItems.greenPane())
                .backgroundItem(AnimationItems.blackPane());
    }

    public static PresetBuilder timerBarPreset(String id) {
        return new PresetBuilder(id, PresetType.TIMER_BAR)
                .periodTicks(1L)
                .durationFrames(100L)
                .primaryItem(AnimationItems.yellowPane())
                .secondaryItem(AnimationItems.blackPane())
                .progressNameFormat("&eTimer {progress}%")
                .progressLoreFormat("&7Remaining: &f{remaining_seconds}s");
    }

    public static BorderAnimation premiumBorder(String id, int rows) {
        return BorderAnimation.builder()
                .id(id)
                .rows(rows)
                .periodTicks(2L)
                .items(AnimationItems.premiumPanes())
                .clockwise(true)
                .loop(true)
                .build();
    }

    public static BorderTrailAnimation paginationBorder(String id, int rows) {
        return BorderTrailAnimation.builder()
                .id(id)
                .rows(rows)
                .periodTicks(1L)
                .trailLength(6)
                .backgroundItem(AnimationItems.blackPane())
                .build();
    }

    public static LoadingSpinnerAnimation loadingSpinner(String id, int centerSlot) {
        return loadingPreset(id).centerSlot(centerSlot).buildLoadingSpinner();
    }

    public static TypingAnimation loadingText(String id, int slot, String text) {
        return loadingPreset(id).slot(slot).text(text).buildTyping();
    }

    public static PulseAnimation loadingPanel(String id, int[] slots) {
        return loadingPreset(id).slots(slots).buildPulse();
    }

    public static RainbowAnimation premiumIdle(String id, int[] slots) {
        return rainbowBorderPreset(id).slots(slots).buildRainbowBorder();
    }

    public static ConfirmAnimation confirm(String id, int[] slots) {
        return confirmPreset(id).slots(slots).buildConfirm();
    }

    public static ErrorAnimation error(String id, int[] slots) {
        return errorPreset(id).slots(slots).buildError();
    }

    public static NotificationFlashAnimation notification(String id, int[] slots, ItemStack flashItem) {
        return successFlashPreset(id)
                .slots(slots)
                .primaryItem(flashItem == null ? AnimationItems.yellowPane() : flashItem)
                .buildNotificationFlash();
    }

    public static TimerBarAnimation timerBar(String id, int[] slots, long durationFrames) {
        return timerBarPreset(id).slots(slots).durationFrames(durationFrames).buildTimerBar();
    }

    public static CountdownAnimation countdown(String id, int slot, int seconds) {
        return countdownPreset(id).slot(slot).seconds(seconds).buildCountdown();
    }

    public static ChestOpenAnimation chestOpen(String id, int rows) {
        return ChestOpenAnimation.builder()
                .id(id)
                .rows(rows)
                .centerSlot(centerSlot(rows))
                .periodTicks(1L)
                .revealEveryFrames(2)
                .build();
    }

    public static CollapseAnimation collapse(String id, int rows) {
        return CollapseAnimation.builder()
                .id(id)
                .rows(rows)
                .centerSlot(centerSlot(rows))
                .periodTicks(1L)
                .collapseEveryFrames(2)
                .build();
    }

    public static SlotMachineAnimation slotMachine(String id, int[] slots, ItemStack[] resultItems) {
        return SlotMachineAnimation.builder()
                .id(id)
                .slots(slots)
                .durationFrames(50L)
                .reelItems(AnimationItems.rainbowPanes())
                .resultItems(resultItems)
                .build();
    }

    public static SparkleAnimation rewardSparkle(String id, int[] slots) {
        return SparkleAnimation.builder()
                .id(id)
                .slots(slots)
                .sparklesPerFrame(3)
                .sparkleLifetimeFrames(4)
                .periodTicks(2L)
                .build();
    }

    public static final class PresetBuilder {

        private final String id;
        private final PresetType type;
        private long delayTicks;
        private long periodTicks = 1L;
        private long maxFrames;
        private long durationFrames = 100L;
        private int rows = 6;
        private int slot = 22;
        private int centerSlot = 22;
        private int seconds = 5;
        private int framesPerSecond = 20;
        private int holdFrames = 2;
        private int pulses = 2;
        private int shakes = 6;
        private int flashes = 3;
        private int flashFrames = 3;
        private int colorWidth = 2;
        private int decimalPlaces;
        private boolean clockwise = true;
        private boolean loop = true;
        private boolean clearOnStop;
        private boolean displayProgress = true;
        private String text = "Loading...";
        private String displayFormat;
        private String loreFormat;
        private String progressNameFormat;
        private String progressLoreFormat;
        private Supplier<?> valueSupplier;
        private int[] slots;
        private ItemStack primaryItem;
        private ItemStack secondaryItem;
        private ItemStack backgroundItem;
        private ItemStack[] palette;

        private PresetBuilder(String id, PresetType type) {
            this.id = id;
            this.type = type;
        }

        public Animation build() {
            switch (type) {
                case LOADING: return buildLoadingSpinner();
                case ASYNC_LOADING: return buildLoadingSpinner();
                case BORDER_PULSE: return buildBorderPulse();
                case CONFIRM: return buildConfirm();
                case ERROR: return buildError();
                case COUNTDOWN: return buildCountdown();
                case RAINBOW_BORDER: return buildRainbowBorder();
                case SUCCESS_FLASH: return buildNotificationFlash();
                case TIMER_BAR: return buildTimerBar();
                default: return null;
            }
        }

        public Animation[] buildAll() {
            if (type != PresetType.ASYNC_LOADING) {
                return new Animation[]{build()};
            }

            return new Animation[]{
                    buildPulse(id + "-panel"),
                    buildLoadingSpinner(id + "-spinner"),
                    buildTyping(id + "-text")
            };
        }

        public LoadingSpinnerAnimation buildLoadingSpinner() {
            return buildLoadingSpinner(id);
        }

        public LoadingSpinnerAnimation buildLoadingSpinner(String animationId) {
            return LoadingSpinnerAnimation.builder()
                    .id(animationId)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .maxFrames(maxFrames)
                    .loop(loop)
                    .slots(slots == null ? spinnerSlots(centerSlot) : slots)
                    .activeItem(primaryItem == null ? AnimationItems.cyanPane() : primaryItem)
                    .inactiveItem(secondaryItem == null ? AnimationItems.blackPane() : secondaryItem)
                    .clockwise(clockwise)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public TypingAnimation buildTyping() {
            return buildTyping(id);
        }

        public TypingAnimation buildTyping(String animationId) {
            return TypingAnimation.builder()
                    .id(animationId)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .maxFrames(maxFrames)
                    .loop(loop)
                    .slot(slot)
                    .text(text)
                    .valueSupplier(valueSupplier)
                    .baseItem(primaryItem)
                    .build();
        }

        public PulseAnimation buildPulse() {
            return buildPulse(id);
        }

        public PulseAnimation buildPulse(String animationId) {
            return PulseAnimation.builder()
                    .id(animationId)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .maxFrames(maxFrames)
                    .loop(loop)
                    .slots(slots == null ? SlotPatterns.borderlessCenter(rows) : slots)
                    .items(palette == null ? AnimationItems.loadingPanes() : palette)
                    .backgroundItem(backgroundItem)
                    .holdFrames(holdFrames)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public PulseAnimation buildBorderPulse() {
            return PulseAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .maxFrames(maxFrames)
                    .loop(loop)
                    .slots(slots == null ? SlotPatterns.borderClockwise(rows) : slots)
                    .items(palette == null ? AnimationItems.loadingPanes() : palette)
                    .backgroundItem(backgroundItem)
                    .holdFrames(holdFrames)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public ConfirmAnimation buildConfirm() {
            return ConfirmAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .pulses(pulses)
                    .slots(slots == null ? SlotPatterns.borderlessCenter(rows) : slots)
                    .items(palette)
                    .backgroundItem(backgroundItem)
                    .holdFrames(holdFrames)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public ErrorAnimation buildError() {
            return ErrorAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .shakes(shakes)
                    .slots(slots == null ? SlotPatterns.borderlessCenter(rows) : slots)
                    .errorItem(primaryItem == null ? AnimationItems.redPane() : primaryItem)
                    .backgroundItem(backgroundItem)
                    .shakeFrames(holdFrames)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public CountdownAnimation buildCountdown() {
            return CountdownAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .slot(slot)
                    .seconds(seconds)
                    .framesPerSecond(framesPerSecond)
                    .baseItem(primaryItem)
                    .displayFormat(displayFormat)
                    .loreFormat(loreFormat)
                    .useAmount(true)
                    .build();
        }

        public RainbowAnimation buildRainbowBorder() {
            return RainbowAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .maxFrames(maxFrames)
                    .loop(loop)
                    .slots(slots == null ? SlotPatterns.borderClockwise(rows) : slots)
                    .items(palette == null ? AnimationItems.rainbowPanes() : palette)
                    .colorWidth(colorWidth)
                    .backgroundItem(backgroundItem)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public NotificationFlashAnimation buildNotificationFlash() {
            return NotificationFlashAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .flashes(flashes)
                    .slots(slots == null ? SlotPatterns.all(rows) : slots)
                    .flashItem(primaryItem == null ? AnimationItems.greenPane() : primaryItem)
                    .backgroundItem(backgroundItem)
                    .flashFrames(flashFrames)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public TimerBarAnimation buildTimerBar() {
            return TimerBarAnimation.builder()
                    .id(id)
                    .delayTicks(delayTicks)
                    .periodTicks(periodTicks)
                    .durationFrames(durationFrames)
                    .slots(slots == null ? SlotPatterns.row(Math.max(0, rows - 1)) : slots)
                    .timeItem(primaryItem == null ? AnimationItems.yellowPane() : primaryItem)
                    .emptyItem(secondaryItem == null ? AnimationItems.blackPane() : secondaryItem)
                    .displayProgress(displayProgress)
                    .decimalPlaces(decimalPlaces)
                    .progressNameFormat(progressNameFormat)
                    .progressLoreFormat(progressLoreFormat)
                    .clearOnStop(clearOnStop)
                    .build();
        }

        public PresetBuilder delayTicks(long delayTicks) {
            this.delayTicks = Math.max(0L, delayTicks);
            return this;
        }

        public PresetBuilder periodTicks(long periodTicks) {
            this.periodTicks = Math.max(1L, periodTicks);
            return this;
        }

        public PresetBuilder maxFrames(long maxFrames) {
            this.maxFrames = Math.max(0L, maxFrames);
            return this;
        }

        public PresetBuilder durationFrames(long durationFrames) {
            this.durationFrames = Math.max(1L, durationFrames);
            return this;
        }

        public PresetBuilder rows(int rows) {
            this.rows = SlotPatterns.normalizeRows(rows);
            return this;
        }

        public PresetBuilder slot(int slot) {
            this.slot = slot;
            return this;
        }

        public PresetBuilder centerSlot(int centerSlot) {
            this.centerSlot = centerSlot;
            return this;
        }

        public PresetBuilder seconds(int seconds) {
            this.seconds = Math.max(1, seconds);
            return this;
        }

        public PresetBuilder framesPerSecond(int framesPerSecond) {
            this.framesPerSecond = Math.max(1, framesPerSecond);
            return this;
        }

        public PresetBuilder holdFrames(int holdFrames) {
            this.holdFrames = Math.max(1, holdFrames);
            return this;
        }

        public PresetBuilder pulses(int pulses) {
            this.pulses = Math.max(1, pulses);
            return this;
        }

        public PresetBuilder shakes(int shakes) {
            this.shakes = Math.max(1, shakes);
            return this;
        }

        public PresetBuilder flashes(int flashes) {
            this.flashes = Math.max(1, flashes);
            return this;
        }

        public PresetBuilder flashFrames(int flashFrames) {
            this.flashFrames = Math.max(1, flashFrames);
            return this;
        }

        public PresetBuilder colorWidth(int colorWidth) {
            this.colorWidth = Math.max(1, colorWidth);
            return this;
        }

        public PresetBuilder decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = Math.min(4, Math.max(0, decimalPlaces));
            return this;
        }

        public PresetBuilder clockwise(boolean clockwise) {
            this.clockwise = clockwise;
            return this;
        }

        public PresetBuilder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public PresetBuilder clearOnStop(boolean clearOnStop) {
            this.clearOnStop = clearOnStop;
            return this;
        }

        public PresetBuilder displayProgress(boolean displayProgress) {
            this.displayProgress = displayProgress;
            return this;
        }

        public PresetBuilder text(String text) {
            this.text = text == null ? "" : text;
            return this;
        }

        public PresetBuilder displayFormat(String displayFormat) {
            this.displayFormat = displayFormat;
            return this;
        }

        public PresetBuilder loreFormat(String loreFormat) {
            this.loreFormat = loreFormat;
            return this;
        }

        public PresetBuilder progressNameFormat(String progressNameFormat) {
            this.progressNameFormat = progressNameFormat;
            return this;
        }

        public PresetBuilder progressLoreFormat(String progressLoreFormat) {
            this.progressLoreFormat = progressLoreFormat;
            return this;
        }

        public PresetBuilder valueSupplier(Supplier<?> valueSupplier) {
            this.valueSupplier = valueSupplier;
            return this;
        }

        public PresetBuilder slots(int[] slots) {
            this.slots = AnimationSupport.slots(slots);
            return this;
        }

        public PresetBuilder primaryItem(ItemStack primaryItem) {
            this.primaryItem = primaryItem;
            return this;
        }

        public PresetBuilder secondaryItem(ItemStack secondaryItem) {
            this.secondaryItem = secondaryItem;
            return this;
        }

        public PresetBuilder backgroundItem(ItemStack backgroundItem) {
            this.backgroundItem = backgroundItem;
            return this;
        }

        public PresetBuilder palette(ItemStack... palette) {
            this.palette = palette == null ? null : palette.clone();
            return this;
        }
    }

    private enum PresetType {
        LOADING,
        ASYNC_LOADING,
        BORDER_PULSE,
        CONFIRM,
        ERROR,
        COUNTDOWN,
        RAINBOW_BORDER,
        SUCCESS_FLASH,
        TIMER_BAR
    }

    private static int[] spinnerSlots(int centerSlot) {
        int row = centerSlot / SlotPatterns.COLUMNS;
        int column = centerSlot % SlotPatterns.COLUMNS;
        if (row <= 0 || row >= 5 || column <= 0 || column >= 8) {
            return new int[]{centerSlot};
        }
        return new int[]{
                centerSlot - SlotPatterns.COLUMNS - 1,
                centerSlot - SlotPatterns.COLUMNS,
                centerSlot - SlotPatterns.COLUMNS + 1,
                centerSlot + 1,
                centerSlot + SlotPatterns.COLUMNS + 1,
                centerSlot + SlotPatterns.COLUMNS,
                centerSlot + SlotPatterns.COLUMNS - 1,
                centerSlot - 1
        };
    }

    private static int centerSlot(int rows) {
        int normalizedRows = SlotPatterns.normalizeRows(rows);
        return (normalizedRows / 2) * SlotPatterns.COLUMNS + 4;
    }
}
