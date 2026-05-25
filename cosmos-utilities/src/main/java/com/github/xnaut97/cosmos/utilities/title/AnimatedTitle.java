package com.github.xnaut97.cosmos.utilities.title;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class AnimatedTitle {

    private final Plugin plugin;

    private Player player;

    private String title = "";

    private String subtitle = "";

    private int fadeIn = 10;

    private int stay = 40;

    private int fadeOut = 10;

    private int interval = 2;

    private AnimationType type = AnimationType.STATIC;

    private boolean running;

    private BukkitRunnable task;

    private int tick;

    public AnimatedTitle(Plugin plugin) {
        this.plugin = plugin;
    }

    public static AnimatedTitle create(Plugin plugin) {
        return new AnimatedTitle(plugin);
    }

    public AnimatedTitle start() {

        if (running || player == null) {
            return this;
        }

        running = true;

        task = new BukkitRunnable() {

            @Override
            public void run() {

                if (!running
                        || player == null
                        || !player.isOnline()) {

                    stop();

                    return;
                }

                render();

                tick++;
            }

        };

        task.runTaskTimer(plugin, 0L, interval);

        return this;
    }

    public AnimatedTitle stop() {

        running = false;

        if (task != null) {
            task.cancel();
            task = null;
        }

        return this;
    }

    private void render() {

        String renderedTitle =
                type.render(title, tick);

        String renderedSubtitle =
                type.render(subtitle, tick);

        player.sendTitle(
                color(renderedTitle),
                color(renderedSubtitle),
                fadeIn,
                stay,
                fadeOut
        );
    }

    private String color(String text) {

        return text == null
                ? ""
                : ChatColor.translateAlternateColorCodes('&', text);
    }

}