package com.github.xnaut97.cosmos;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.trade.TradingSessionManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new Metrics(this, 31294);

        Menu.registerListeners(this);
        TradingSessionManager.registerListeners(this);


    }

    @Override
    public void onDisable() {

    }
}
