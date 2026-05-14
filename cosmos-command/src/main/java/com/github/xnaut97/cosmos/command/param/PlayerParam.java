package com.github.xnaut97.cosmos.command.param;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
public class PlayerParam extends CommandParam {

    private boolean includeOffline = false;

    public PlayerParam(String placeholder, String description) {
        super(placeholder, description, ParamType.PLAYER);
    }

    @Override
    public List<String> test(String value) {
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        if (includeOffline)
            players.addAll(Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(name -> !players.contains(name))
                    .collect(Collectors.toList()));

        return players.stream()
                .filter(name -> name.toLowerCase().startsWith(value.toLowerCase()))
                .collect(Collectors.toList());
    }

}
