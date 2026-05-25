package com.github.xnaut97.cosmos.command;

import com.github.xnaut97.cosmos.command.param.CommandParam;
import com.github.xnaut97.cosmos.command.param.ParamPriority;
import com.github.xnaut97.cosmos.command.param.StringParam;
import com.github.xnaut97.cosmos.command.syntax.CommandSyntax;
import com.github.xnaut97.cosmos.utilities.text.ClickableText;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelpCommand extends CommandArgument {

    private final Map<String, CommandArgument> subCommands;

    private final AbstractCommand<?> handle;

    HelpCommand(AbstractCommand<?> handle) {
        this.handle = handle;
        this.subCommands = handle.getArguments();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return handle.getName() + ".command.help";
    }

    @Override
    public String getPermissionDescription() {
        return "Access help command.";
    }

    @Override
    public PermissionDefault getPermissionDefault() {
        return PermissionDefault.TRUE;
    }

    @Override
    public String getDescription() {
        return "View available commands";
    }

    @Override
    public List<CommandParam> getParams() {
        int size = Math.toIntExact(handle.getArguments().values().stream().distinct().count()) + handle.getSyntaxes().size();
        int perPage = handle.getHelpSuggestions();

        // Compute total pages using integer math trick: ceil(size / perPage)
        int totalPages = (size + perPage - 1) / perPage;

        String[] pages = IntStream.rangeClosed(1, totalPages)
                .mapToObj(String::valueOf)
                .toArray(String[]::new);

        return Collections.singletonList(
                new StringParam("page", "page number")
                        .setPlaceholders(pages)
        );
    }


    @Override
    public List<String> getAliases() {
        return Collections.singletonList("?");
    }

    @Override
    public void playerExecute(Player player, String[] args) {
        if (args.length == 0) {
            handleCommands(player, 0);
            return;
        }
        int page = getPage(args[0]);
        handleCommands(player, page);
    }

    @Override
    public void consoleExecute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            handleCommands(sender, 0);
            return;
        }
        int page = getPage(args[0]);
        handleCommands(sender, page);
    }

    private int getPage(String str) {
        try {
            int page = Integer.parseInt(str);
            return Math.max(page - 1, 0);
        } catch (Exception e) {
            return 0;
        }
    }


    private void handleCommands(CommandSender sender, int page) {
        List<CommandArgument> filter = Lists.newArrayList();
        subCommands.values().stream()
                .distinct()
                .sorted(Comparator.comparing(CommandArgument::getName))
                .forEachOrdered(command -> {
                    boolean hasPermission = command.getPermission() != null && !command.getPermission().isEmpty();
                    if (!hasPermission || sender.hasPermission(command.getPermission())) {
                        Optional<CommandArgument> existing = filter.stream()
                                .filter(c -> c.getName().equalsIgnoreCase(command.getName()))
                                .findFirst();

                        if (!existing.isPresent()) {
                            filter.add(command);
                        }
                    }
                });
        List<Object> entries = Lists.newArrayList();
        entries.addAll(filter);
        handle.getSyntaxes().stream()
                .filter(syntax -> syntax.getPermission() == null ||
                        syntax.getPermission().isEmpty() ||
                        sender.hasPermission(syntax.getPermission()))
                .sorted(Comparator.comparing(CommandSyntax::getUsage))
                .forEachOrdered(entries::add);
        int perPage = handle.getHelpSuggestions();
        int totalPages = (entries.size() + perPage - 1) / perPage;

        if (totalPages == 0) totalPages = 1;
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        int max = Math.min(perPage * (page + 1), entries.size());

        if (handle.getHelpHeader() != null)
            sender.sendMessage(handle.getHelpHeader().replace("&", "§"));
        sender.sendMessage(" ");
        sender.sendMessage(("&7Aliases: " + handle.getAliases().stream()
                .map(s -> handle.getHelpCommandColor() + "/" + s)
                .collect(Collectors.joining("&7, "))).replace("&", "§"));
        sender.sendMessage("§a[ ]§7: required params   §2{ }§7: optional params");
        sender.sendMessage("§7Hover to see command information.");
        sender.sendMessage(" ");
        for (int i = page * handle.getHelpSuggestions(); i < max; i++) {
            Object entry = entries.get(i);
            if (entry instanceof CommandSyntax) {
                CommandSyntax syntax = (CommandSyntax) entry;
                if (sender instanceof Player) {
                    ((Player) sender).spigot().sendMessage(createClickableSyntax(syntax));
                } else {
                    sender.sendMessage((handle.getHelpCommandColor() + "/" + handle.getName() + " " + syntax.getUsage()
                            + ": " + handle.getHelpDescriptionColor() + syntax.getDescription())
                            .replace("&", "\u00A7"));
                }
                continue;
            }

            CommandArgument command = (CommandArgument) entry;
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (command.require(player) || player.isOp())
                    player.spigot().sendMessage(createClickableCommand(command));
            } else {
                sender.sendMessage((handle.getHelpCommandColor() + "/" + handle.getName() + " " + command.getName()
                        + " " + handle.getHelpParamColor() + buildParams(command) + ": "
                        + handle.getHelpDescriptionColor() + command.getDescription())
                        .replace("&", "§"));
            }

            if (command.getAliases() != null && !command.getAliases().isEmpty()) {
                sender.sendMessage((" &7└ Aliases: " + command.getAliases().stream()
                        .map(alias -> "&d" + alias).collect(Collectors.joining("&7, ")))
                        .replace("&", "§"));
            }
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            TextComponent previousPage = createClickableButton("&e&l«",
                    "/" + handle.getName() + " help " + (page - 1),
                    "&7Previous page");
            TextComponent nextPage = createClickableButton("&e&l»",
                    "/" + handle.getName() + " help " + (page + 2),
                    "&7Next page");
            TextComponent pageInfo = createClickableButton(" &e&l" + (page + 1) + " ",
                    null, "&7You're in page " + (page + 1));
            ClickableText spacing = new ClickableText("                       ");
            boolean canNextPage = handle.getHelpSuggestions() * (page + 1) < entries.size();
            if (page < 1) {
                if (canNextPage)
                    player.spigot().sendMessage(spacing.build(), pageInfo, nextPage);
                else
                    player.spigot().sendMessage(spacing.build(), pageInfo);

            } else {
                if (canNextPage)
                    player.spigot().sendMessage(spacing.build(), previousPage, pageInfo, nextPage);
                else
                    player.spigot().sendMessage(spacing.build(), previousPage, pageInfo);
            }
        }
        if (handle.getHelpFooter() != null)
            sender.sendMessage(handle.getHelpFooter().replace("&", "§"));
    }

    private BaseComponent createClickableCommand(CommandArgument argument) {
        List<String> hover = Lists.newArrayList();
        if (!argument.getDescription().isEmpty()) {
            hover.add((handle.getHelpDescriptionColor() + argument.getDescription()).replace("&", "§"));
            hover.add("\n \n");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(handle.getHelpCommandColor().replace("&", "§"))
                .append("/").append(handle.getName()).append(" ").append(argument.getName());

        if (!argument.getParams().isEmpty()) {
            // Build command with params
            builder.append(" ").append(handle.getHelpParamColor().replace("&", "§"))
                    .append(buildParams(argument));

            hover.addAll(argument.getParams().stream().map(this::buildHoverParam).collect(Collectors.toList()));
            hover.add(" \n");
        }

        hover.add("§eClick to get this command");

        ClickableText clickableText = new ClickableText(builder.toString());
        clickableText.setHoverAction(HoverEvent.Action.SHOW_TEXT, hover.toArray(new String[0]));
        clickableText.setClickAction(ClickEvent.Action.SUGGEST_COMMAND, "/" + handle.getName() + " " + argument.getName());
        return clickableText.build();
    }

    private BaseComponent createClickableSyntax(CommandSyntax syntax) {
        List<String> hover = Lists.newArrayList();
        if (!syntax.getDescription().isEmpty()) {
            hover.add((handle.getHelpDescriptionColor() + syntax.getDescription()).replace("&", "\u00A7"));
            hover.add("\n \n");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(handle.getHelpCommandColor().replace("&", "\u00A7"))
                .append("/").append(handle.getName());

        String usage = syntax.getUsage();
        if (!usage.isEmpty()) {
            builder.append(" ").append(handle.getHelpParamColor().replace("&", "\u00A7"))
                    .append(usage);
        }

        if (!syntax.getParams().isEmpty()) {
            hover.addAll(syntax.getParams().stream().map(this::buildHoverParam).collect(Collectors.toList()));
            hover.add(" \n");
        }

        hover.add("\u00A7eClick to get this command");

        ClickableText clickableText = new ClickableText(builder.toString());
        clickableText.setHoverAction(HoverEvent.Action.SHOW_TEXT, hover.toArray(new String[0]));
        clickableText.setClickAction(ClickEvent.Action.SUGGEST_COMMAND, "/" + handle.getName() + " " + buildSyntaxSuggestion(syntax));
        return clickableText.build();
    }

    private String buildSyntaxSuggestion(CommandSyntax syntax) {
        String usage = syntax.getUsage();
        int paramIndex = usage.indexOf('[');
        int optionalIndex = usage.indexOf('{');
        int index = -1;
        if (paramIndex >= 0 && optionalIndex >= 0) {
            index = Math.min(paramIndex, optionalIndex);
        } else if (paramIndex >= 0) {
            index = paramIndex;
        } else if (optionalIndex >= 0) {
            index = optionalIndex;
        }
        return index < 0 ? usage : usage.substring(0, index).trim();
    }

    @NotNull
    private String buildParams(CommandArgument command) {
        return command.getParams().stream()
                .filter(param -> param.getName() != null && !param.getName().isEmpty())
                .map(param -> {
                    String bracketLeft = param.getPriority() == ParamPriority.PRIMARY ? "[" : "{";
                    String bracketRight = param.getPriority() == ParamPriority.PRIMARY ? "]" : "}";

                    return bracketLeft + param.getName() + bracketRight;
                }).collect(Collectors.joining(" "));
    }

    @NotNull
    private String buildHoverParam(CommandParam param) {
        String rawName = param.getName();
        String name = (rawName == null) ? "" : rawName.trim();
        boolean nameInvalid = name.isEmpty();

        // No wrapping left/right brackets anymore — keep name as-is
        String finalName = "";
        if (!nameInvalid) {
            // Collapse multiple spaces inside the param name
            finalName = String.join(" ", name.split("\\s+"));
        }

        StringBuilder sb = new StringBuilder();
        if (!nameInvalid) {
            sb.append("§9")
                    .append(finalName)
                    .append(" §7- ");
        }

        if (param instanceof StringParam) {
            StringParam stringParam = (StringParam) param;
            sb.append(stringParam.getStyle().getCode());
        }

        sb.append("§7").append(param.getDescription()).append("\n");
        return sb.toString();
    }


    private TextComponent createClickableButton(String name, String clickAction, String... hoverAction) {
        ClickableText clickableText = new ClickableText(name);
        if (hoverAction.length > 0)
            clickableText.setHoverAction(HoverEvent.Action.SHOW_TEXT, hoverAction);
        if (clickAction != null)
            clickableText.setClickAction(ClickEvent.Action.RUN_COMMAND, clickAction);
        ComponentBuilder builder = new ComponentBuilder(name.replace("&", "§"));
        if (clickAction != null)
            builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickAction));
        if (hoverAction.length > 0) {
            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Arrays.stream(hoverAction)
                    .map(s -> new TextComponent(s.replace("&", "§")))
                    .toArray(TextComponent[]::new)));
        }
        return clickableText.build();
    }
}
