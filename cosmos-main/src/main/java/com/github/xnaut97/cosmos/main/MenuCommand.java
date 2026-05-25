package com.github.xnaut97.cosmos.main;

import com.github.xnaut97.cosmos.command.AbstractCommand;
import com.github.xnaut97.cosmos.menu.MenuPreviewHub;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MenuCommand extends AbstractCommand<CosmosPlugin> {

    public MenuCommand(CosmosPlugin plugin) {
        super(plugin, "menu", "", "", new ArrayList<>());

        register();
    }

    @Override
    public void onSingleExecute(CommandSender sender) {
        new MenuPreviewHub(getPlugin(), getPlugin().getMenuRegistry())
                .open((Player) sender);
    }
}
