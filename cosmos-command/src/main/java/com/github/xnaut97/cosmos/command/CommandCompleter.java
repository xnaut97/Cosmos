package com.github.xnaut97.cosmos.command;

import com.github.xnaut97.cosmos.command.param.ArrayParam;
import com.github.xnaut97.cosmos.command.param.CommandParam;
import com.github.xnaut97.cosmos.command.param.ParamType;
import com.github.xnaut97.cosmos.command.syntax.CommandSyntax;
import com.github.xnaut97.cosmos.command.syntax.SyntaxMatch;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

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

    public @NotNull List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 0)
            return Collections.emptyList();

        List<CommandArgument> arguments = getArguments(sender, args[0]);
        // ---- FIRST ARGUMENT (COMMAND ARGUMENT NAME) ----
        if (args.length == 1) {
            Set<String> suggestions = new LinkedHashSet<>(arguments.stream()
                    .map(CommandArgument::getName)
                    .collect(Collectors.toList()));
            suggestions.addAll(getSyntaxSuggestions(sender, args));
            return new ArrayList<>(suggestions);
        }

        CommandArgument argument = arguments.stream()
                .filter(arg -> arg.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);

        if (argument == null) {
            return getSyntaxSuggestions(sender, args);
        }

        if (argument.getParams().isEmpty()) {
            List<String> syntaxSuggestions = getSyntaxSuggestions(sender, args);
            return syntaxSuggestions.isEmpty() ? Collections.emptyList() : syntaxSuggestions;
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

    private List<String> getSyntaxSuggestions(CommandSender sender, String[] args) {
        Set<String> suggestions = new LinkedHashSet<>();
        for (CommandSyntax syntax : handle.getSyntaxes()) {
            if (syntax.getPermission() != null && !syntax.getPermission().isEmpty() && !sender.hasPermission(syntax.getPermission())) {
                continue;
            }
            SyntaxMatch match = handle.getSyntaxParser().partial(sender, syntax, args);
            if (match == null) {
                continue;
            }
            if (match.getActiveParam() == null) {
                if (match.getActiveInput() != null) {
                    suggestions.add(match.getActiveInput());
                }
                continue;
            }
            suggestions.addAll(match.getActiveParam().apply(match.getActiveInput()));
        }
        return new ArrayList<>(suggestions);
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
