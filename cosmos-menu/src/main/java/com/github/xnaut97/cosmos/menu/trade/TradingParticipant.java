package com.github.xnaut97.cosmos.menu.trade;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class TradingParticipant {

    private final UUID uuid;
    private final Player player;
    private final List<ItemStack> items = new ArrayList<>();
    private int page;
    private boolean accepted;

    TradingParticipant(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    void page(int page) {
        this.page = Math.max(0, page);
    }

    void accepted(boolean accepted) {
        this.accepted = accepted;
    }

    void resetDecision() {
        this.accepted = false;
    }
}
