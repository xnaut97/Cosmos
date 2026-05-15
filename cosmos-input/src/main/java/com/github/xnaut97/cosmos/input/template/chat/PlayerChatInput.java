package com.github.xnaut97.cosmos.input.template.chat;

import com.github.xnaut97.cosmos.input.InputPipeline;
import com.github.xnaut97.cosmos.utilities.PlayerUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class PlayerChatInput<T> implements Listener {

    private final Plugin plugin;
    private final Player player;

    private Consumer<Player> onStart;

    private Consumer<Player> onExit;

    private InputPipeline<T> pipeline;

    // Called when chat input parsing succeeds.
    private BiConsumer<Player, T> onSuccess;

    public void start() {
        if (onStart != null)
            onStart.accept(player);

        player.closeInventory();
        PlayerUtil.sendMessages(player, "&6Press &aF &6to exit.");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    protected void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId()))
            return;

        event.setCancelled(true);

        T value = pipeline.process(player, event.getMessage());
        if (value == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (onSuccess != null)
                    onSuccess.accept(player, value);
            }
        }.runTask(plugin);

        unregisterListener();
    }

    @EventHandler
    protected void onPlayerExit(PlayerSwapHandItemsEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId()))
            return;

        event.setCancelled(true);
        unregisterListener();
        if (onExit != null)
            onExit.accept(player);
    }

    private void unregisterListener() {
        HandlerList.unregisterAll(this);
    }
}

