package com.github.xnaut97.cosmos.menu.component;

import com.github.xnaut97.cosmos.menu.Menu;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

public interface MenuComponent {

    default Collection<Integer> getOwnedSlots() {
        return Collections.emptySet();
    }

    default void onAttach(Menu<?> menu) {
    }

    default void onDetach(Menu<?> menu) {
    }

    default void render(Menu<?> menu) {
    }

    default void onRender(Menu<?> menu) {
    }

    default void onUpdate(Menu<?> menu) {
    }

    default void onClick(Menu<?> menu, InventoryClickEvent event) {
    }

    default void onDrag(Menu<?> menu, InventoryDragEvent event) {
    }

    default void onClose(Menu<?> menu, InventoryCloseEvent event) {
    }

    default boolean canDragInto(Menu<?> menu, int slot) {
        return false;
    }

    default boolean canPlace(Menu<?> menu, int slot, ItemStack item) {
        return false;
    }

    default boolean canTake(Menu<?> menu, int slot) {
        return false;
    }
}
