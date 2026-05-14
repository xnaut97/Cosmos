package com.github.xnaut97.cosmos.menu.component;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonComponent extends SlotComponent {

    public ButtonComponent(int slot, ItemStack item) {
        super(slot, item);
    }

    public ButtonComponent(int slot, Supplier<ItemStack> itemSupplier) {
        super(slot, itemSupplier);
    }

    public static ButtonComponent of(int slot, ItemStack item) {
        return new ButtonComponent(slot, item);
    }

    public ButtonComponent item(ItemStack item) {
        super.item(item);
        return this;
    }

    public ButtonComponent item(Supplier<ItemStack> itemSupplier) {
        super.item(Objects.requireNonNull(itemSupplier, "itemSupplier"));
        return this;
    }

    public ButtonComponent onClick(Consumer<InventoryClickEvent> clickHandler) {
        super.onClick(clickHandler);
        return this;
    }

    public ButtonComponent cancelClick(boolean cancelClick) {
        super.cancelClick(cancelClick);
        return this;
    }
}
