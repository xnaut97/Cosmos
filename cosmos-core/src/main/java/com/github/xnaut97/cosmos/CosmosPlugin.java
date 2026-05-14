package com.github.xnaut97.cosmos;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new Metrics(this, 31294);
    }

    @Override
    public void onDisable() {

    }
}
