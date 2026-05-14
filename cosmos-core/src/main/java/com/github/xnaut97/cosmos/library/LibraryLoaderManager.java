package com.github.xnaut97.cosmos.library;

import com.github.xnaut97.cosmos.library.platform.BukkitLibraryLoader;
import com.github.xnaut97.cosmos.library.platform.LibraryLoader;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public class LibraryLoaderManager {

    private final Plugin plugin;

    private final Map<LibraryPlatform, LibraryLoader<?>> loaders = new HashMap<>();

    public LibraryLoaderManager(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        Arrays.stream(LibraryPlatform.values()).forEach(platform -> {
            LibraryLoader<?> loader = null;

            switch (platform) {
                case BUKKIT:
                    loader = new BukkitLibraryLoader(getPlugin());
                    break;
            }

            if(loader == null) return;

            getLoaders().put(platform, loader);

        });
    }
}
