package com.github.xnaut97.cosmos.main;

import com.github.xnaut97.cosmos.command.AbstractCommand;
import com.github.xnaut97.cosmos.command.param.PlayerParam;
import com.github.xnaut97.cosmos.command.syntax.CommandSyntax;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TradeCommand extends AbstractCommand<CosmosPlugin> {

    public TradeCommand(CosmosPlugin plugin) {
        super(plugin, "trade", "Trade command", "/trade", Arrays.asList("tr", "trd"));

        addSyntaxes(
                new CommandSyntax()
                        .param(new PlayerParam("player", "Player to trade with"), false, false)
                        .executor((sender, context) -> {
                            if(!(sender instanceof Player)) return;

                            System.out.println("1");
                            Player target = context.get("player", Player.class);
                            if(target == null) return;

                            System.out.println("3");
                            new TradingSession(getPlugin(), (Player) sender, target, "Test Trading").open();
                        })
        );

        register();
    }
}
