package com.github.xnaut97.cosmos.command;

import com.github.xnaut97.cosmos.command.syntax.CommandSyntax;
import com.github.xnaut97.cosmos.command.syntax.SyntaxMatch;
import com.github.xnaut97.cosmos.command.syntax.SyntaxParser;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 @author TezVN
 */
@Getter
@Setter
public abstract class AbstractCommand<T extends Plugin> extends BukkitCommand {

    private final T plugin;

    private final UUID uniqueId = UUID.randomUUID();

    private final Map<String, CommandArgument> arguments = Maps.newHashMap();

    private final List<CommandSyntax> syntaxes = new ArrayList<>();

    private final SyntaxParser syntaxParser = new SyntaxParser();

    private String noPermissionsMessage = "&cYou don't have permission to access.";

    private String noSubCommandFoundMessage = "&cCommand not found, please use /" + getName() + " help for more.";

    private String noConsoleAllowMessage = "&cThis command is for console only.";

    private String helpHeader;

    private String helpFooter;

    private String helpCommandColor = "&3";

    private String helpParamColor = "&b";

    private String helpDescriptionColor = "&7";

    private int helpSuggestions = 5;

    private String requiredPermission;

    private final Set<String> registeredPermissions = new HashSet<>();

    public AbstractCommand(T plugin, String name, String description, String usageMessage, List<String> aliases) {
        super(name.toLowerCase(), description, usageMessage,
                aliases.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()));
        this.plugin = plugin;
        this.helpHeader = "&8- - - - - - - - - -=[ &6&l" + plugin.getName() + " &8]=- - - - - - - - - -";
        StringBuilder sb = new StringBuilder();
        sb.append(new String(new char[plugin.getName().length() + 1]).replace('\0', ' '));
        this.helpFooter = "&8- - - - - - - - - -=[ " + sb + " &8]=- - - - - - - - - -";
    }

    public void onSingleExecute(CommandSender sender) {
        this.arguments.values().stream()
                .filter(argument -> argument instanceof HelpCommand).findAny()
                .ifPresent(command -> {
                    if(sender instanceof ConsoleCommandSender) {
                        command.consoleExecute(sender, new String[0]);
                    }else {
                        command.playerExecute((Player) sender, new String[0]);
                    }
                });
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 0)
            onSingleExecute(sender);
        else {
            String name = args[0];
            CommandArgument command = this.arguments.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .map(Map.Entry::getValue).findAny().orElse(null);
            if (command == null) {
                SyntaxMatch match = findSyntax(sender, args);
                if (match != null) {
                    CommandSyntax syntax = match.getSyntax();
                    String permission = syntax.getPermission();
                    if (permission != null && !permission.isEmpty()) {
                        boolean hasPermission = !(sender instanceof Player) || sender.hasPermission(permission);
                        if (!hasPermission) {
                            sender.sendMessage(this.noPermissionsMessage.replace("&", "\u00A7"));
                            return true;
                        }
                    }
                    if (sender instanceof Player && syntax.getRequirement() != null) {
                        Player player = (Player) sender;
                        if (!syntax.getRequirement().test(player) && !player.isOp()) {
                            return true;
                        }
                    }
                    if (syntax.getExecutor() != null) {
                        syntax.getExecutor().execute(sender, match.getContext());
                        return true;
                    }
                }
                sender.sendMessage(this.noSubCommandFoundMessage.replace("&", "§"));
                return true;
            }
            String permission = command.getPermission();
            if (permission != null) {
                boolean hasPermission = !(sender instanceof Player) || sender.hasPermission(permission);
                if(!hasPermission) {
                    sender.sendMessage(this.noPermissionsMessage.replace("&", "§"));
                    return true;
                }
            }
            command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
            throws IllegalArgumentException {
        return new CommandCompleter(this).onTabComplete(sender, args);
    }

    /**
     * Add sub command to your main command
     *
     * @param arguments Sub command to add.
     */
    public AbstractCommand<T> addArguments(CommandArgument... arguments) {
        for (CommandArgument argument : arguments) {
            if(this.arguments.containsKey(argument.getName())) {
                getPlugin().getLogger().warning("Duplicate command " + argument.getName());
                continue;
            }

            if(argument.getName() == null || argument.getName().isEmpty())
                continue;
            this.arguments.putIfAbsent(argument.getName(), argument);
            if(argument.getAliases() != null && !argument.getAliases().isEmpty())
                argument.getAliases().forEach(alias -> this.arguments.putIfAbsent(alias, argument));
            if(argument.getPermission() == null)
                continue;
            registerPermission(argument);
        }
        return this;
    }

    public AbstractCommand<T> addSyntaxes(CommandSyntax... syntaxes) {
        for (CommandSyntax syntax : syntaxes) {
            if (syntax == null) {
                continue;
            }
            this.syntaxes.add(syntax);
            registerPermission(syntax);
        }
        return this;
    }

    private void registerPermission(CommandArgument command) {
        String permName = command.getPermission();
        if (permName == null || permName.isEmpty()) return;

        PluginManager pm = Bukkit.getPluginManager();

        Permission permission = pm.getPermission(permName);
        if (permission == null) {
            permission = new Permission(
                    permName,
                    command.getPermissionDescription(),
                    command.getPermissionDefault(),
                    command.getChildPermissions()
            );
            pm.addPermission(permission);
            registeredPermissions.add(permName);
        }
    }

    private void registerPermission(CommandSyntax syntax) {
        String permName = syntax.getPermission();
        if (permName == null || permName.isEmpty()) return;

        PluginManager pm = Bukkit.getPluginManager();

        Permission permission = pm.getPermission(permName);
        if (permission == null) {
            permission = new Permission(
                    permName,
                    syntax.getPermissionDescription(),
                    syntax.getPermissionDefault()
            );
            pm.addPermission(permission);
            registeredPermissions.add(permName);
        }
    }

    private SyntaxMatch findSyntax(CommandSender sender, String[] args) {
        for (CommandSyntax syntax : syntaxes) {
            SyntaxMatch match = syntaxParser.match(sender, syntax, args);
            if (match != null && match.isMatched()) {
                return match;
            }
        }
        return null;
    }

    private void registerMainPermission() {
        if (requiredPermission == null || requiredPermission.isEmpty()) return;

        PluginManager pm = Bukkit.getPluginManager();

        Permission permission = pm.getPermission(requiredPermission);
        if (permission == null) {
            permission = new Permission(
                    requiredPermission,
                    "",
                    PermissionDefault.OP
            );
            pm.addPermission(permission);
            registeredPermissions.add(requiredPermission);
        }
    }

    /**
     * Register command to server in {@code onEnable()} method
     */
    public AbstractCommand<T> register() {
        try {
            registerMainPermission();
            addArguments(new HelpCommand(this));

            CommandMap commandMap = getCommandMap();
            commandMap.register(plugin.getName().toLowerCase(), this);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Unregister command from server in {@code onDisable()} method
     */
    public AbstractCommand<T> unregister() {
        try {
            CommandMap commandMap = getCommandMap();
            this.unregister(commandMap);

            PluginManager pm = Bukkit.getPluginManager();
            for (String perm : registeredPermissions) {
                Permission permission = pm.getPermission(perm);
                if (permission != null) {
                    pm.removePermission(permission);
                }
            }
            registeredPermissions.clear();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }


    private CommandMap getCommandMap() throws Exception {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }


    public String getHelpCommandColor() {
        return helpCommandColor == null ? "&a" : this.helpCommandColor;
    }

    public String getHelpDescriptionColor() {
        return helpDescriptionColor == null ? "&7" : this.helpDescriptionColor;
    }

    /**
     * Get list of registered sub commands
     *
     * @return List of sub commands
     */
    public Map<String, CommandArgument> getArguments() {
        return Collections.unmodifiableMap(this.arguments);
    }

    public List<CommandSyntax> getSyntaxes() {
        return Collections.unmodifiableList(this.syntaxes);
    }

    SyntaxParser getSyntaxParser() {
        return syntaxParser;
    }

}
