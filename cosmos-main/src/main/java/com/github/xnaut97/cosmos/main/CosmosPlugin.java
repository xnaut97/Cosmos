package com.github.xnaut97.cosmos.main;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.LibraryManager;
import com.github.xnaut97.cosmos.library.Libraries;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import com.github.xnaut97.cosmos.menu.trade.TradingSessionManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@Getter
public final class CosmosPlugin extends JavaPlugin {

    private TradeCommand tradeCommand;
    private LibraryManager libraryManager;

    @Override
    public void onLoad() {
        this.libraryManager = new BukkitLibraryManager(this, "/libraries");

        libraryManager.addJCenter();
        libraryManager.addJitPack();
        libraryManager.addMavenCentral();
        libraryManager.addMavenLocal();
        libraryManager.addSonatype();

        Arrays.stream(Libraries.values()).forEach(library -> {
            if(library.name().contains("COSMOS")) return;

            Library.Builder builder = Library.builder()
                    .groupId(library.getGroupId())
                    .artifactId(library.getArtifactId())
                    .loaderId(library.getId())
                    .version(library.getVersion());

            if (library.getRepository() != null)
                builder.repository(library.getRepository());

            libraryManager.downloadLibrary(builder.build());
        });
    }

    @Override
    public void onEnable() {
        new Metrics(this, 31294);

        Menu.registerListeners(this);
        TradingSessionManager.registerListeners(this);

        this.tradeCommand = new TradeCommand(this);
    }

    @Override
    public void onDisable() {
        TradingSessionManager.getSessions().forEach(TradingSession::shutdown);

        Menu.forceCloseAll();
        HandlerList.unregisterAll(this);

        if(this.tradeCommand != null)
            this.tradeCommand.unregister();

    }
}
