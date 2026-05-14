package com.github.xnaut97.cosmos.menu.component;

import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public class StaticItemComponent extends SlotComponent {

    public StaticItemComponent(int slot, ItemStack item) {
        super(slot, item);
    }

    public StaticItemComponent(int slot, Supplier<ItemStack> itemSupplier) {
        super(slot, itemSupplier);
    }

    public static StaticItemComponent of(int slot, ItemStack item) {
        return new StaticItemComponent(slot, item);
    }
}
