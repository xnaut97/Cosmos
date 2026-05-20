package com.github.xnaut97.cosmos;

import com.github.xnaut97.cosmos.command.AbstractCommand;
import com.github.xnaut97.cosmos.command.CommandArgument;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class TradeCommand extends AbstractCommand<CosmosPlugin> {

    public TradeCommand(CosmosPlugin plugin) {
        super(plugin, "trade", "Trade command", "/trade", Arrays.asList("tr", "trd"));

    }

}
