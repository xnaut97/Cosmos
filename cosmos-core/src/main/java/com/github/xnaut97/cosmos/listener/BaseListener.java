package com.github.xnaut97.cosmos.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@Getter
@AllArgsConstructor
public abstract class BaseListener implements Listener {

    private final Plugin plugin;

}
