package com.github.xnaut97.cosmos.menu.trade.component;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import com.github.xnaut97.cosmos.menu.trade.TradingParticipant;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import com.github.xnaut97.cosmos.utilities.item.ItemCreator;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@RequiredArgsConstructor
public class TradePageComponent implements MenuComponent {

    private final TradingSession session;
    private final TradingParticipant participant;
    private final int slot;
    private final IntSupplier pageSupplier;
    private final IntConsumer pageConsumer;
    private final boolean next;
    private final boolean allowEmptyTail;

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Collections.singleton(slot);
    }

    @Override
    public void render(Menu menu) {
        menu.renderSlot(slot, isAvailable() ? pageItem() : null);
    }

    @Override
    public void onClick(Menu menu, InventoryClickEvent event) {
        event.setCancelled(true);
        if (!isAvailable()) {
            return;
        }
        int maxPage = session.getMaxPage(participant, allowEmptyTail);
        int page = pageSupplier.getAsInt();
        if (event.isShiftClick()) {
            pageConsumer.accept(next ? maxPage : 0);
        } else {
            pageConsumer.accept(next ? page + 1 : page - 1);
        }
    }

    private boolean isAvailable() {
        int page = pageSupplier.getAsInt();
        int maxPage = session.getMaxPage(participant, allowEmptyTail);
        return next ? page < maxPage : page > 0;
    }

    private ItemStack pageItem() {
        if (next) {
            return new ItemCreator(Material.ARROW)
                    .setDisplayName("&eActions")
                    .setLore("&7\u2514 Click: &eNext \u00BB",
                            "&7\u2514 Shift-click: &eLast \u00BB")
                    .build();
        }
        return new ItemCreator(Material.ARROW)
                .setDisplayName("&eActions")
                .setLore("&7\u2514 Click: &e\u00AB Previous",
                        "&7\u2514 Shift-click: &e\u00AB First")
                .build();
    }
}
