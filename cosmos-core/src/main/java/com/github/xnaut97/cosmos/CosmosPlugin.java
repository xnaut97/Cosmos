package com.github.xnaut97.cosmos;

import com.github.xnaut97.cosmos.library.LibraryLoaderManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    private LibraryLoaderManager libraryLoaderManager;

    @Override
    public void onLoad() {
        libraryLoaderManager = new LibraryLoaderManager(this);
    }

    @Override
    public void onEnable() {
        new Metrics(this, 31294);
    }

    @Override
    public void onDisable() {

    }
}
