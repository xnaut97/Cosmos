package com.github.xnaut97.cosmos.library.platform;

import com.alessiodp.libby.BukkitLibraryManager;
import org.bukkit.plugin.Plugin;

public class BukkitLibraryLoader extends LibraryLoader<BukkitLibraryManager> {
    public BukkitLibraryLoader(Plugin plugin) {
        super(new BukkitLibraryManager(plugin, "../../libraries"));
    }
}
