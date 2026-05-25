package com.github.xnaut97.cosmos.listener;

import com.github.xnaut97.cosmos.utilities.java.ClassScanner;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class ListenerManager {

    private final Plugin plugin;
    private final Set<String> packages = new HashSet<>();
    private final List<BaseListener> listeners = new ArrayList<>();
    private boolean registered;

    public ListenerManager(Plugin plugin) {
        this.plugin = plugin;

        registerListeners();
    }

    public ListenerManager addPackage(String packageName) {
        packages.add(packageName);
        return this;
    }

    public void registerListeners() {
        if (registered) return;

        ClassScanner scanner = new ClassScanner(getPlugin());

        packages.forEach(scanner::filterPackage);

        scanner.loadClasses(
                BaseListener.class,
                new Class<?>[]{Plugin.class},
                new Object[]{getPlugin()}, listener -> {
                    if(!listener.canRegister()) return;

                    Bukkit.getPluginManager().registerEvents(listener, getPlugin());
                    this.listeners.add(listener);
                }
        );

        registered = true;
    }

    public void unregisterListeners() {
        if (registered)
            HandlerList.unregisterAll(this.getPlugin());
    }

}
