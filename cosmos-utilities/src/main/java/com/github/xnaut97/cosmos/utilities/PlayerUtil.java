package com.github.xnaut97.cosmos.utilities;

import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerUtil {

    public static void sendMessages(CommandSender sender, String... msg) {
        if(msg == null) return;

        for (String s : msg) {
            sender.sendMessage(s.replace("&", "§"));
        }
    }

    public static void broadcast(String... messages) {
        broadcast(null, messages);
    }

    public static void broadcast(String[] messages, Predicate<Player> predicate) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(predicate != null && !predicate.test(player)) return;
            sendMessages(player, messages);
        });
    }

    public static void broadcast(List<UUID> excludes, String... messages) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (excludes != null && excludes.contains(player.getUniqueId()))
                continue;
            sendMessages(player, messages);
        }
    }


    public static void sendTitle(Player player, String title) {
        sendTitle(player, title, "");
    }

    public static void sendTitle(Player player, String title, String description) {
        player.sendTitle(color(title), color(description));
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(color(message)));
    }

    public static String color(String str) {
        return str.replace("&", "§");
    }

    public static List<String> color(String... msg) {
        return Arrays.stream(msg).map(PlayerUtil::color).collect(Collectors.toList());
    }

    public static String stripColor(String msg) {
        return ChatColor.stripColor(color(msg));
    }

    public static List<String> color(List<String> list) {
        return color(list.toArray(new String[0]));
    }

    public static boolean checkSpecialCharacters(String str) {
        return !getSpecialCharacters(str).isEmpty();
    }

    public static List<String> getSpecialCharacters(String str) {
        return Arrays.stream(str.split(""))
                .filter(s -> !s.matches("[a-zA-Z0-9]*"))
                .collect(Collectors.toList());
    }

    public static String filterSpecialCharacters(String str) {
        return Arrays.stream(str.split(""))
                .filter(s -> s.matches("[a-zA-Z0-9]*"))
                .collect(Collectors.joining());
    }

    public static String capitalize(String str, CapitalizeMode mode) {
        String[] split = str.split("[\\s!@#$%^&*()=+-_,]+");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            switch (mode) {
                case FIRST:
                    sb.append(s.substring(0, 1).toUpperCase());
                    if (s.length() > 1)
                        sb.append(s.substring(1).toLowerCase());
                    break;
                case ALL:
                    sb.append(s.toUpperCase());
                    break;
            }
        }
        return sb.toString();
    }

    public static String format(Enum<?> e) {
        String name = e.name().toLowerCase().replace('_', ' ');
        return Arrays.stream(name.split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }


    public static String format(Material material) {
        StringBuilder sb = new StringBuilder();
        String[] name = material.name().split("_");
        for (String s : name) {
            sb.append(s.charAt(0)).append(s.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static String format(Location location) {
        return format(location, true);
    }

    public static String format(Location location, boolean includeBrackets) {
        return (includeBrackets ? "[" : "")
                + (location.getWorld() == null ? "none" : location.getWorld().getName()) + ", " +
                location.getBlockX() + ", " +
                location.getBlockY() + ", " +
                location.getBlockZ() + "" +
                (includeBrackets ? "]" : "");
    }

    public enum CapitalizeMode {
        FIRST,
        ALL;
    }

}
