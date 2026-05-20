package com.github.xnaut97.cosmos.command.syntax;

import com.github.xnaut97.cosmos.command.param.CommandParam;
import org.bukkit.command.CommandSender;

public interface CommandParameterResolver<P extends CommandParam> {

    Object resolve(CommandSender sender, P param, String input) throws SyntaxParseException;
}
