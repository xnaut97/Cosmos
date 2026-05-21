package com.github.xnaut97.cosmos.menu.trade.component;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import com.github.xnaut97.cosmos.menu.trade.TradingParticipant;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import com.github.xnaut97.cosmos.utilities.ItemCreator;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;

@RequiredArgsConstructor
public class TradeControlComponent implements MenuComponent {

    private final TradingSession session;
    private final TradingParticipant self;
    private final TradingParticipant target;
    private final int cancelSlot;
    private final int acceptSlot;
    private final int opponentAcceptedSlot;

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Arrays.asList(cancelSlot, acceptSlot, opponentAcceptedSlot);
    }

    @Override
    public void render(Menu menu) {
        menu.renderSlot(cancelSlot, cancelItem());
        menu.renderSlot(acceptSlot, acceptItem());
        menu.renderSlot(opponentAcceptedSlot, target.isAccepted() ? opponentAcceptedItem() : null);
    }

    @Override
    public void onClick(Menu menu, InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot == cancelSlot) {
            if (self.isAccepted()) {
                session.unaccept(self);
            } else {
                session.cancel();
            }
            return;
        }
        if (slot == acceptSlot) {
            session.accept(self);
        }
    }

    private ItemStack cancelItem() {
        return new ItemCreator(Material.BARRIER)
                .setDisplayName(self.isAccepted() ? "&eUnaccept" : "&cCancel")
                .setLore(self.isAccepted() ? "&7Reopen your offer for editing." : "&7Cancel this trade and return all items.")
                .setGlow(self.isAccepted())
                .build();
    }

    private ItemStack acceptItem() {
        if (self.isAccepted()) {
            return new ItemCreator(Material.EMERALD_BLOCK)
                    .setDisplayName("&aAccepted")
                    .setLore("&7Waiting for " + target.getPlayer().getName() + ".")
                    .setGlow(true)
                    .build();
        }
        return new ItemCreator(Material.EMERALD)
                .setDisplayName("&aAccept")
                .setLore("&7Accept and lock your offer.")
                .build();
    }

    private ItemStack opponentAcceptedItem() {
        return new ItemCreator(Material.LIME_CONCRETE)
                .setDisplayName("&aOpponent Accepted")
                .setLore("&7Waiting for you.")
                .setGlow(true)
                .build();
    }
}
