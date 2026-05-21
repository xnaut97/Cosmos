package com.github.xnaut97.cosmos.menu.trade;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public final class TradingSessionManager {

    private static final Map<UUID, TradingSession> SESSIONS = new HashMap<>();

    private TradingSessionManager() {
    }

    public static void registerListeners(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                TradingSession session = getSession(event.getPlayer());
                if (session != null) {
                    session.cancel();
                }
            }
        }, plugin);
    }

    public static List<TradingSession> getSessions() {
        return SESSIONS.values().stream().collect(Collectors.collectingAndThen(
                Collectors.toList(), Collections::unmodifiableList));
    }

    static void register(TradingSession session) {
        SESSIONS.put(session.getFirst().getUuid(), session);
        SESSIONS.put(session.getSecond().getUuid(), session);
    }

    static void unregister(TradingSession session) {
        SESSIONS.remove(session.getFirst().getUuid(), session);
        SESSIONS.remove(session.getSecond().getUuid(), session);
    }

    public static TradingSession getSession(Player player) {
        return player == null ? null : SESSIONS.get(player.getUniqueId());
    }
}
