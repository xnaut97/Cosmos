package com.github.xnaut97.cosmos.menu.trade.component;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import com.github.xnaut97.cosmos.utilities.ItemCreator;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DividerComponent implements MenuComponent {

    private final int[] slots;

    @Override
    public Collection<Integer> getOwnedSlots() {
        List<Integer> ownedSlots = new ArrayList<>(slots.length);
        for (int slot : slots) {
            ownedSlots.add(slot);
        }
        return ownedSlots;
    }

    @Override
    public void render(Menu menu) {
        ItemStack item = new ItemCreator(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("&8")
                .setAmount(1)
                .build();
        for (int slot : slots) {
            menu.renderSlot(slot, item);
        }
    }

    @Override
    public void onClick(Menu menu, InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
