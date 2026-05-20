package com.github.xnaut97.cosmos.command.syntax;

import org.bukkit.command.CommandSender;

public interface SyntaxExecutor {

    void execute(CommandSender sender, ArgumentContext context);
}
