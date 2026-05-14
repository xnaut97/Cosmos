package com.github.xnaut97.cosmos.command;

import com.github.xnaut97.cosmos.command.param.CommandParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public abstract class CommandArgument {

    private final Map<String, Boolean> childPermissions = Maps.newHashMap();

    public CommandArgument() {
    }

    public Map<String, Boolean> getChildPermissions() {
        return Collections.unmodifiableMap(this.childPermissions);
    }

    public void addChildPermission(String permission, boolean child) {
        this.childPermissions.put(permission, child);
    }

    public void removeChildPermission(String permission) {
        this.childPermissions.remove(permission);
    }

    /**
     * Get name of sub command
     *
     * @return Sub command name
     */
    public abstract String getName();

    /**
     * Get permission of sub command
     *
     * @return Sub command permission
     */
    public abstract String getPermission();

    /**
     * Get description of permission.
     *
     * @return Permission description.
     */
    public abstract String getPermissionDescription();

    /**
     * Get permission default of command.
     *
     * @return Permission default mode.
     */
    public abstract PermissionDefault getPermissionDefault();

    /**
     * Get description of sub command
     *
     * @return Sub command description
     */
    public abstract String getDescription();

    public boolean require(Player player) {
        return true;
    }

    public List<CommandParam> getParams() {
        return new ArrayList<>();
    }

    /**
     * Get list of aliases of sub command
     *
     * @return Sub command aliases
     */
    public List<String> getAliases() {
        return Lists.newArrayList();
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (require(player) || player.isOp())
                playerExecute(player, args);
        } else {
            consoleExecute(sender, args);
        }
    }

    protected abstract void playerExecute(Player player, String[] args);

    protected void consoleExecute(CommandSender sender, String[] args) {
        sender.sendMessage("§cThis command is for players only.");
    }

}
