package com.github.xnaut97.cosmos.menu.component;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.MenuLayout;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BackgroundComponent implements MenuComponent {

    private final List<Integer> slots = new ArrayList<>();
    private Supplier<ItemStack> itemSupplier;

    public BackgroundComponent(Collection<Integer> slots, ItemStack item) {
        this(slots, () -> item);
    }

    public BackgroundComponent(Collection<Integer> slots, Supplier<ItemStack> itemSupplier) {
        if (slots != null) {
            this.slots.addAll(slots);
        }
        this.itemSupplier = itemSupplier;
    }

    public static BackgroundComponent border(Menu<?> menu, ItemStack item) {
        return new BackgroundComponent(MenuLayout.border(menu.getSize()), item);
    }

    public BackgroundComponent item(ItemStack item) {
        this.itemSupplier = () -> item;
        return this;
    }

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Collections.unmodifiableList(slots);
    }

    @Override
    public void render(Menu<?> menu) {
        for (Integer slot : slots) {
            menu.renderSlot(slot, itemSupplier == null ? null : itemSupplier.get());
        }
    }

    @Override
    public void onClick(Menu<?> menu, org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
