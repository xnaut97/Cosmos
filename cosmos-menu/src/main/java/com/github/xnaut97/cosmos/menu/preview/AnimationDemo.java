package com.github.xnaut97.cosmos.menu.preview;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.animation.Animation;
import com.github.xnaut97.cosmos.menu.animation.type.*;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.menu.animation.utility.FillPattern;
import com.github.xnaut97.cosmos.menu.animation.utility.SlotPatterns;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum AnimationDemo {

    BORDER("Border Animation", Material.BLACK_STAINED_GLASS_PANE,
            "Palette movement around the border.",
            id -> BorderAnimation.builder()
                    .id(id)
                    .rows(5)
                    .periodTicks(2L)
                    .clockwise(true)
                    .items(AnimationItems.premiumPanes())
                    .build()),

    PULSE("Pulse Animation", Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            "Region-wide pulsing loading state.",
            id -> PulseAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(3L)
                    .holdFrames(2)
                    .items(AnimationItems.loadingPanes())
                    .build()),

    WAVE("Wave Animation", Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            "Color wave across the preview grid.",
            id -> WaveAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(1L)
                    .waveWidth(2)
                    .items(AnimationItems.rainbowPanes())
                    .build()),

    SNAKE("Snake Animation", Material.LIME_STAINED_GLASS_PANE,
            "Moving trail through a spiral path.",
            id -> SnakeAnimation.builder()
                    .id(id)
                    .slots(spiral())
                    .periodTicks(2L)
                    .trailLength(8)
                    .items(AnimationItems.premiumPanes())
                    .build()),

    LOADING_SPINNER("Loading Spinner", Material.CLOCK,
            "True rotating one-slot spinner.",
            id -> LoadingSpinnerAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.rectangleBorder(12, 3, 3))
                    .periodTicks(2L)
                    .clockwise(true)
                    .activeItem(AnimationItems.cyanPane())
                    .inactiveItem(AnimationItems.blackPane())
                    .build()),

    PROGRESS_BAR("Progress Bar", Material.EMERALD,
            "Progress bar with percentage item text.",
            id -> ProgressBarAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.row(2))
                    .periodTicks(1L)
                    .durationFrames(80L)
                    .filledItem(AnimationItems.limePane())
                    .emptyItem(AnimationItems.blackPane())
                    .displayProgress(true)
                    .decimalPlaces(1)
                    .progressNameFormat("&aProgress {progress}%")
                    .progressLoreFormat("&7Exact progress: &f{progress}%")
                    .build()),

    BLINK("Blink Animation", Material.YELLOW_STAINED_GLASS_PANE,
            "Button-like blinking target slots.",
            id -> BlinkAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.rectangle(20, 5, 1))
                    .periodTicks(2L)
                    .intervalFrames(4)
                    .onItem(AnimationItems.yellowPane())
                    .offItem(AnimationItems.blackPane())
                    .build()),

    RAINBOW("Rainbow Animation", Material.MAGENTA_STAINED_GLASS_PANE,
            "Rainbow palette cycling across a border.",
            id -> RainbowAnimation.builder()
                    .id(id)
                    .slots(border())
                    .periodTicks(2L)
                    .colorWidth(2)
                    .build()),

    SCANLINE("Scanline Animation", Material.IRON_BARS,
            "Finite five-pass horizontal scan.",
            id -> ScanlineAnimation.builder()
                    .id(id)
                    .rows(5)
                    .periodTicks(2L)
                    .scanCount(5)
                    .loop(false)
                    .horizontal(true)
                    .scanItem(AnimationItems.lightGrayPane())
                    .backgroundItem(AnimationItems.blackPane())
                    .build()),

    TYPING("Typing Animation", Material.PAPER,
            "Dynamic percentage typing text.",
            id -> TypingAnimation.builder()
                    .id(id)
                    .slot(22)
                    .periodTicks(2L)
                    .valueSupplier(dynamicLoadingText())
                    .build()),

    FILL("Fill Animation", Material.GREEN_STAINED_GLASS_PANE,
            "Center-out patterned region fill.",
            id -> FillAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(1L)
                    .pattern(FillPattern.CENTER_OUT)
                    .fillItem(AnimationItems.greenPane())
                    .emptyItem(AnimationItems.blackPane())
                    .build()),

    DRAIN("Drain Animation", Material.RED_STAINED_GLASS_PANE,
            "Outside-in patterned region drain.",
            id -> DrainAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(1L)
                    .pattern(FillPattern.OUTSIDE_IN)
                    .filledItem(AnimationItems.redPane())
                    .emptyItem(AnimationItems.blackPane())
                    .build()),

    RANDOM_FLICKER("Random Flicker", Material.WHITE_STAINED_GLASS_PANE,
            "Randomly toggled slot highlights.",
            id -> RandomFlickerAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(2L)
                    .changesPerFrame(4)
                    .onItem(AnimationItems.whitePane())
                    .offItem(AnimationItems.blackPane())
                    .build()),

    BOUNCE("Bounce Animation", Material.SLIME_BALL,
            "Highlight bouncing along a row.",
            id -> BounceAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.row(2))
                    .periodTicks(2L)
                    .bounceItem(AnimationItems.yellowPane())
                    .backgroundItem(AnimationItems.blackPane())
                    .build()),

    BREATHING("Breathing Animation", Material.CYAN_STAINED_GLASS_PANE,
            "Smooth ping-pong pulse.",
            id -> BreathingAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(2L)
                    .holdFrames(2)
                    .items(AnimationItems.loadingPanes())
                    .build()),

    MATRIX("Matrix Animation", Material.LIME_DYE,
            "Column-based cascading trails.",
            id -> MatrixAnimation.builder()
                    .id(id)
                    .rows(5)
                    .periodTicks(2L)
                    .trailLength(4)
                    .build()),

    FIRE_SPREAD("Fire Spread", Material.BLAZE_POWDER,
            "Fire effect expanding from the center.",
            id -> FireSpreadAnimation.builder()
                    .id(id)
                    .rows(5)
                    .originSlot(22)
                    .periodTicks(2L)
                    .spreadEveryFrames(1)
                    .build()),

    SPARKLE("Sparkle Animation", Material.NETHER_STAR,
            "Short-lived random sparkles.",
            id -> SparkleAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(2L)
                    .sparklesPerFrame(3)
                    .sparkleLifetimeFrames(4)
                    .backgroundItem(AnimationItems.blackPane())
                    .build()),

    CAROUSEL("Carousel Animation", Material.ENDER_PEARL,
            "Items rotating around a circle.",
            id -> CarouselAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.circle(22, 2, 5))
                    .periodTicks(2L)
                    .items(AnimationItems.rainbowPanes())
                    .build()),

    SLOT_SWAP("Slot Swap", Material.HOPPER,
            "Two slots flash and swap items.",
            id -> SlotSwapAnimation.builder()
                    .id(id)
                    .firstSlot(20)
                    .secondSlot(24)
                    .periodTicks(2L)
                    .durationFrames(16L)
                    .firstItem(AnimationItems.bluePane())
                    .secondItem(AnimationItems.purplePane())
                    .transitionItem(AnimationItems.whitePane())
                    .build()),

    SPIRAL("Spiral Animation", Material.COMPASS,
            "Trail moving through spiral traversal.",
            id -> SpiralAnimation.builder()
                    .id(id)
                    .rows(5)
                    .periodTicks(1L)
                    .trailLength(7)
                    .build()),

    BORDER_TRAIL("Border Trail", Material.GLOWSTONE_DUST,
            "Bright trailing light on border.",
            id -> BorderTrailAnimation.builder()
                    .id(id)
                    .rows(5)
                    .periodTicks(1L)
                    .trailLength(7)
                    .backgroundItem(AnimationItems.blackPane())
                    .build()),

    NOTIFICATION_FLASH("Notification Flash", Material.YELLOW_WOOL,
            "Finite full-region flash.",
            id -> NotificationFlashAnimation.builder()
                    .id(id)
                    .slots(all())
                    .periodTicks(1L)
                    .flashes(4)
                    .flashFrames(3)
                    .flashItem(AnimationItems.yellowPane())
                    .backgroundItem(AnimationItems.blackPane())
                    .build()),

    CONFIRM("Confirm Animation", Material.EMERALD,
            "Finite green confirmation pulse.",
            id -> ConfirmAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(1L)
                    .pulses(3)
                    .holdFrames(2)
                    .build()),

    ERROR("Error Animation", Material.BARRIER,
            "Red alternating validation flash.",
            id -> ErrorAnimation.builder()
                    .id(id)
                    .slots(center())
                    .periodTicks(1L)
                    .shakes(8)
                    .shakeFrames(2)
                    .backgroundItem(AnimationItems.blackPane())
                    .build()),

    COUNTDOWN("Countdown Animation", Material.CLOCK,
            "Large countdown using text plus capped amount.",
            id -> CountdownAnimation.builder()
                    .id(id)
                    .slot(22)
                    .periodTicks(1L)
                    .seconds(90)
                    .framesPerSecond(2)
                    .displayFormat("&eCountdown {time}")
                    .loreFormat("&7Exact remaining: &f{seconds}s")
                    .useAmount(true)
                    .build()),

    TIMER_BAR("Timer Bar", Material.GOLD_INGOT,
            "Finite timer bar draining fully empty.",
            id -> TimerBarAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.row(4))
                    .periodTicks(1L)
                    .durationFrames(80L)
                    .timeItem(AnimationItems.yellowPane())
                    .emptyItem(AnimationItems.blackPane())
                    .displayProgress(true)
                    .decimalPlaces(0)
                    .progressNameFormat("&eLoading... {progress}%")
                    .progressLoreFormat("&7Remaining: &f{remaining_seconds}s")
                    .build()),

    SLOT_MACHINE("Slot Machine", Material.DISPENSER,
            "Rolling reels landing on results.",
            id -> SlotMachineAnimation.builder()
                    .id(id)
                    .slots(SlotPatterns.rectangle(21, 3, 1))
                    .periodTicks(1L)
                    .durationFrames(60L)
                    .reelItems(AnimationItems.rainbowPanes())
                    .resultItems(new ItemStack[]{
                            AnimationItems.emerald(),
                            AnimationItems.sparkle(),
                            AnimationItems.emerald()
                    })
                    .build()),

    CHEST_OPEN("Chest Open", Material.CHEST,
            "Expanding reveal from the center.",
            id -> ChestOpenAnimation.builder()
                    .id(id)
                    .rows(5)
                    .centerSlot(22)
                    .periodTicks(1L)
                    .revealEveryFrames(2)
                    .build()),

    COLLAPSE("Collapse Animation", Material.ENDER_CHEST,
            "Reverse reveal collapsing inward.",
            id -> CollapseAnimation.builder()
                    .id(id)
                    .rows(5)
                    .centerSlot(22)
                    .periodTicks(1L)
                    .collapseEveryFrames(2)
                    .build());

    private final String displayName;
    private final Material icon;
    private final String description;
    private final Function<String, Animation> animationFactory;

    Animation createAnimation() {
        return animationFactory.apply(animationId());
    }

    String animationId() {
        return "animation-preview-" + name().toLowerCase();
    }

    private static int[] all() {
        return SlotPatterns.all(5);
    }

    private static int[] border() {
        return SlotPatterns.borderClockwise(5);
    }

    private static int[] center() {
        return SlotPatterns.borderlessCenter(5);
    }

    private static int[] spiral() {
        return SlotPatterns.spiral(5);
    }

    private static Supplier<String> dynamicLoadingText() {
        long startedAt = System.currentTimeMillis();
        return () -> {
            long elapsed = System.currentTimeMillis() - startedAt;
            double percent = Math.min(100.0D, elapsed / 40.0D);
            return "Loading " + String.format(Locale.US, "%.1f", percent) + "%";
        };
    }
}