package com.github.xnaut97.cosmos.menu.preview;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.Animation;
import com.github.xnaut97.cosmos.menu.animation.abstraction.AbstractRepeatingAnimation;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationItems;
import com.github.xnaut97.cosmos.utilities.item.ItemCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class AnimationPreviewMenu extends Menu {

    private static final int BACK_SLOT = 45;
    private static final int TOGGLE_SLOT = 47;
    private static final int INFO_SLOT = 49;
    private static final int STOP_SLOT = 50;
    private static final int REPLAY_SLOT = 53;

    private final AnimationDemo demo;
    private String status = "&aRunning";
    private String milestone = "&7No progress milestone reached yet.";
    private String result = "&7Animation is active.";
    private boolean animationActive;
    private boolean animationPaused;

    AnimationPreviewMenu(Plugin plugin, AnimationDemo demo) {
        super(plugin, 6, "&8Animation: " + demo.getDisplayName());
        this.demo = demo;
    }

    @Override
    protected void setup() {
        clearItems();
        renderControls();
        startPreview();
    }

    private void renderControls() {
        ItemStack background = AnimationItems.blackPane();
        for (int slot = 45; slot <= 53; slot++) {
            setItem(slot, background).onClick(event -> event.setCancelled(true));
        }

        setItem(BACK_SLOT, new ItemCreator(Material.ARROW)
                .setDisplayName("&eBack")
                .setLore("&7Return to the animation list.")
                .build())
                .onClick(event -> {
                    event.setCancelled(true);
                    new AnimationListMenu(getPlugin()).open(getPlayer());
                });

        renderLifecycleControls();
        renderStatus();
        renderReplayButton();
    }

    private void renderLifecycleControls() {
        if (!animationActive) {
            renderInactiveControl(TOGGLE_SLOT);
            renderInactiveControl(STOP_SLOT);
            return;
        }

        if (animationPaused) {
            setItem(TOGGLE_SLOT, new ItemCreator(Material.LIME_DYE)
                    .setDisplayName("&aResume")
                    .setLore("&7Continue this animation.")
                    .build())
                    .onClick(event -> {
                        event.setCancelled(true);
                        resumeAnimation(demo.animationId());
                    });
        } else {
            setItem(TOGGLE_SLOT, new ItemCreator(Material.REDSTONE_TORCH)
                    .setDisplayName("&ePause")
                    .setLore("&7Pause without advancing frames.")
                    .build())
                    .onClick(event -> {
                        event.setCancelled(true);
                        pauseAnimation(demo.animationId());
                    });
        }

        setItem(STOP_SLOT, new ItemCreator(Material.BARRIER)
                .setDisplayName("&cStop")
                .setLore("&7Cancel this animation instance.")
                .build())
                .onClick(event -> {
                    event.setCancelled(true);
                    stopAnimation(demo.animationId());
                });
    }

    private void renderInactiveControl(int slot) {
        setItem(slot, AnimationItems.blackPane()).onClick(event -> event.setCancelled(true));
    }

    private void renderReplayButton() {
        setItem(REPLAY_SLOT, new ItemCreator(Material.CLOCK)
                .setDisplayName("&aReplay")
                .setLore("&7Restart this animation preview.")
                .build())
                .onClick(event -> {
                    event.setCancelled(true);
                    stopAnimation(demo.animationId());
                    startPreview();
                });
    }

    private void renderStatus() {
        setItem(INFO_SLOT, new ItemCreator(demo.getIcon())
                .setDisplayName("&e" + demo.getDisplayName())
                .setLore(
                        "&7" + demo.getDescription(),
                        " ",
                        "&8State: " + status,
                        "&8Milestone: " + milestone,
                        "&8Result: " + result,
                        " ",
                        "&7Controls: &fPause &8/ &fResume &8/ &fStop &8/ &fReplay"
                )
                .build())
                .onClick(event -> event.setCancelled(true));
    }

    private void startPreview() {
        animationActive = true;
        animationPaused = false;
        status = "&aRunning";
        milestone = "&7No progress milestone reached yet.";
        result = "&7Animation is active.";
        renderLifecycleControls();
        renderStatus();

        Animation animation = demo.createAnimation();
        attachCallbacks(animation);
        registerAnimation(animation);
    }

    private void attachCallbacks(Animation animation) {
        if (!(animation instanceof AbstractRepeatingAnimation)) {
            return;
        }

        AbstractRepeatingAnimation repeating = (AbstractRepeatingAnimation) animation;

        repeating.onPause(response -> updateLifecycle(true, true, "&ePaused", milestone, "&7Frame " + response.getFrame() + " paused."))
                .onResume(response -> updateLifecycle(true, false, "&aRunning", milestone, "&7Frame " + response.getFrame() + " resumed."))
                .onCancel(response -> updateLifecycle(false, false, "&cCancelled", milestone, "&7Animation was stopped manually."))
                .onComplete(response -> updateLifecycle(false, false, "&bCompleted", "&a100%", "&7Animation finished naturally."))
                .onProgressPercent(25, response -> updateStatus(status, "&e25%", "&7Quarter milestone reached."))
                .onProgressPercent(50, response -> updateStatus(status, "&650%", "&7Halfway milestone reached."))
                .onProgressPercent(75, response -> updateStatus(status, "&e75%", "&7Three-quarter milestone reached."))
                .onProgressPercent(100, response -> updateStatus(status, "&a100%", "&7Final milestone reached."));
    }

    private void updateLifecycle(boolean active, boolean paused, String status, String milestone, String result) {
        this.animationActive = active;
        this.animationPaused = paused;
        renderLifecycleControls();
        updateStatus(status, milestone, result);
    }

    private void updateStatus(String status, String milestone, String result) {
        this.status = status;
        this.milestone = milestone;
        this.result = result;
        renderStatus();
    }
}
