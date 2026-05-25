package com.github.xnaut97.cosmos.utilities.bar;

import com.github.xnaut97.cosmos.utilities.color.ColorMap;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

@Getter
public class ArmorStandProgressBar {

    private static final String METADATA_KEY = "progress_bar";

    /**
     * Current progress value
     */
    private double current;

    /**
     * Maximum progress value
     */
    private double total = 100;

    /**
     * Amount of progress symbols
     */
    private int quantity = 10;

    /**
     * Completed progress symbol
     */
    private String completedSymbol = "❙";

    /**
     * Incomplete progress symbol
     */
    private String incompleteSymbol = "❙";

    /**
     * Gradient / Color map
     */
    private ColorMap completeColor;

    /**
     * Incomplete section color
     */
    private String incompleteColor = "&7";

    /**
     * Display percent text
     */
    private boolean displayPercent;

    /**
     * Cached rendered bar
     */
    private String cached;

    /**
     * Dirty state
     */
    private boolean dirty = true;

    /**
     * Active viewers/displays
     */
    private final Set<LivingEntity> displays = new HashSet<LivingEntity>();

    public ArmorStandProgressBar() {
    }

    /**
     * Build progress bar
     */
    public String build() {

        if (!dirty && cached != null) {
            return cached;
        }

        Preconditions.checkArgument(total > 0,
                "Total must be greater than 0");

        Preconditions.checkArgument(quantity > 0,
                "Quantity must be greater than 0");

        Preconditions.checkNotNull(completeColor,
                "Complete color cannot be null");

        float percent = (float) (current / total);

        percent = Math.max(0F, Math.min(1F, percent));

        int progressBars = (int) (quantity * percent);

        String currentColor = completeColor.getColorOf(
                (int) (percent * 100),
                "§c"
        );

        cached =
                Strings.repeat(
                        currentColor + completedSymbol,
                        progressBars
                )
                        +
                        Strings.repeat(
                                color(incompleteColor)
                                        + incompleteSymbol,
                                quantity - progressBars
                        )
                        +
                        (displayPercent
                                ? " §7[" + currentColor
                                  + ((int) (percent * 100))
                                  + "%§7]"
                                : "");

        dirty = false;

        return cached;
    }

    /**
     * Update all active displays
     */
    private void updateDisplays() {

        String built = build();

        Iterator<LivingEntity> iterator = displays.iterator();

        while (iterator.hasNext()) {

            LivingEntity entity = iterator.next();

            if (entity == null || entity.isDead()) {
                iterator.remove();
                continue;
            }

            entity.setCustomName(built);
            entity.setCustomNameVisible(true);
        }
    }

    /**
     * Mark dirty and refresh
     */
    private void refresh() {
        this.dirty = true;
        updateDisplays();
    }

    /**
     * Display on entity
     */
    public ArmorStandProgressBar display(LivingEntity entity) {

        if (entity == null) {
            return this;
        }

        displays.add(entity);

        entity.setCustomName(build());
        entity.setCustomNameVisible(true);

        return this;
    }

    /**
     * Display temporary hologram
     */
    public ArmorStand display(Location location,
                              Plugin plugin,
                              long durationTicks) {

        ArmorStand stand = spawnArmorStand(location, plugin);

        if (stand == null) {
            return null;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            displays.remove(stand);

            if (!stand.isDead()) {
                stand.remove();
            }

        }, durationTicks);

        return stand;
    }

    /**
     * Display until predicate returns true
     */
    public ArmorStand display(Location location,
                              Plugin plugin,
                              @Nonnull Predicate<ArmorStand> removeCondition) {

        Preconditions.checkNotNull(removeCondition,
                "removeCondition cannot be null");

        ArmorStand stand = spawnArmorStand(location, plugin);

        if (stand == null) {
            return null;
        }

        new BukkitRunnable() {

            @Override
            public void run() {

                if (stand.isDead()) {
                    displays.remove(stand);
                    cancel();
                    return;
                }

                if (removeCondition.test(stand)) {

                    displays.remove(stand);

                    stand.remove();

                    cancel();
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);

        return stand;
    }

    /**
     * Spawn armor stand display
     */
    private ArmorStand spawnArmorStand(Location location,
                                       Plugin plugin) {

        if (location == null || location.getWorld() == null) {
            return null;
        }

        ArmorStand stand = location.getWorld().spawn(
                location,
                ArmorStand.class
        );

        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);

        stand.setMetadata(
                METADATA_KEY,
                new FixedMetadataValue(plugin, true)
        );

        display(stand);

        return stand;
    }

    /**
     * Register global listener once in onEnable()
     */
    public static void registerListener(Plugin plugin) {

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onArmorStandManipulate(
                    PlayerArmorStandManipulateEvent event
            ) {

                ArmorStand stand = event.getRightClicked();

                if (stand.hasMetadata(METADATA_KEY)) {
                    event.setCancelled(true);
                }
            }

        }, plugin);
    }

    private static String color(String text) {
        return text == null
                ? ""
                : text.replace("&", "§");
    }

    /*
     * =========================
     * Fluent setters
     * =========================
     */

    public ArmorStandProgressBar current(double current) {
        this.current = current;
        refresh();
        return this;
    }

    public ArmorStandProgressBar total(double total) {
        this.total = total;
        refresh();
        return this;
    }

    public ArmorStandProgressBar quantity(int quantity) {
        this.quantity = quantity;
        refresh();
        return this;
    }

    public ArmorStandProgressBar completedSymbol(String completedSymbol) {
        this.completedSymbol = completedSymbol;
        refresh();
        return this;
    }

    public ArmorStandProgressBar incompleteSymbol(String incompleteSymbol) {
        this.incompleteSymbol = incompleteSymbol;
        refresh();
        return this;
    }

    public ArmorStandProgressBar completeColor(ColorMap completeColor) {
        this.completeColor = completeColor;
        refresh();
        return this;
    }

    public ArmorStandProgressBar incompleteColor(String incompleteColor) {
        this.incompleteColor = incompleteColor;
        refresh();
        return this;
    }

    public ArmorStandProgressBar incompleteColor(ChatColor color) {
        this.incompleteColor = color.toString();
        refresh();
        return this;
    }

    public ArmorStandProgressBar displayPercent(boolean displayPercent) {
        this.displayPercent = displayPercent;
        refresh();
        return this;
    }
}