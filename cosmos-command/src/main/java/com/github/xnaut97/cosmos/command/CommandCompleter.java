package com.github.xnaut97.cosmos.command;

import com.github.xnaut97.cosmos.command.param.ArrayParam;
import com.github.xnaut97.cosmos.command.param.CommandParam;
import com.github.xnaut97.cosmos.command.param.ParamType;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Getter
final class CommandCompleter {

    private final Map<String, CommandArgument> commands;
    private final AbstractCommand<?> handle;
    private final Plugin plugin;

    CommandCompleter(AbstractCommand<?> command) {
        this.handle = command;
        this.plugin = command.getPlugin();
        this.commands = command.getArguments();
    }

    public @Nonnull List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 0)
            return Collections.emptyList();

        List<CommandArgument> arguments = getArguments(sender, args[0]);
        // ---- FIRST ARGUMENT (COMMAND ARGUMENT NAME) ----
        if (args.length == 1) {
            return arguments.stream()
                    .map(CommandArgument::getName)
                    .collect(Collectors.toList());
        }

        CommandArgument argument = arguments.stream()
                .filter(arg -> arg.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);

        if (argument == null || argument.getParams().isEmpty()) {
            return Collections.emptyList();
        }

        // Extract only parameters after argument name
        String[] inputParams = Arrays.copyOfRange(args, 1, args.length);

        // ---- ARRAY PARAM SUPPORT ----
        Optional<ArrayParam> arrayParamOpt = argument.getParams().stream()
                .filter(p -> p.getType() == ParamType.ARRAY)
                .map(ArrayParam.class::cast)
                .findFirst();

        if (arrayParamOpt.isPresent()) {
            ArrayParam arrayParam = arrayParamOpt.get();
            return arrayParam.apply(String.join(" ", args));
        }

        int paramIndex = 0;

        for (int i = 0; i < inputParams.length - 1; i++) {
            if (!inputParams[i].isEmpty()) {
                paramIndex++;
            }
        }

        String lastInput = inputParams[inputParams.length - 1];

        if (paramIndex >= argument.getParams().size())
            return Collections.emptyList();

        CommandParam activeParam = argument.getParams().get(paramIndex);

        return activeParam.apply(lastInput);
    }

    private List<CommandArgument> getArguments(CommandSender sender, String start) {
        return this.commands.values().stream()
                .filter(cmd -> cmd.getName().startsWith(start))
                .filter(cmd -> cmd.getPermission() == null ||
                        cmd.getPermission().isEmpty() ||
                        sender.hasPermission(cmd.getPermission()))
                .collect(Collectors.toList());
    }
}
