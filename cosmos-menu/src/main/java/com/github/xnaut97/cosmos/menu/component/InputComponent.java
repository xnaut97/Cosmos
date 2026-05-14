package com.github.xnaut97.cosmos.menu.component;

import com.github.xnaut97.cosmos.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class InputComponent implements MenuComponent {

    private final Set<Integer> slots = new LinkedHashSet<>();
    private Predicate<ItemStack> validator = item -> true;
    private BiConsumer<Integer, ItemStack> onChange = (slot, item) -> {
    };
    private BiConsumer<Integer, ItemStack> onInvalid = (slot, item) -> {
    };

    public InputComponent(int... slots) {
        Arrays.stream(slots).forEach(this.slots::add);
    }

    public InputComponent validator(Predicate<ItemStack> validator) {
        this.validator = validator == null ? item -> true : validator;
        return this;
    }

    public InputComponent onChange(BiConsumer<Integer, ItemStack> onChange) {
        this.onChange = onChange == null ? (slot, item) -> {
        } : onChange;
        return this;
    }

    public InputComponent onInvalid(BiConsumer<Integer, ItemStack> onInvalid) {
        this.onInvalid = onInvalid == null ? (slot, item) -> {
        } : onInvalid;
        return this;
    }

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Collections.unmodifiableSet(slots);
    }

    @Override
    public void onAttach(Menu<?> menu) {
        for (Integer slot : slots) {
            menu.allowItemInput(slot);
        }
    }

    @Override
    public void onDetach(Menu<?> menu) {
        for (Integer slot : slots) {
            menu.denyItemInput(slot);
        }
    }

    @Override
    public void render(Menu<?> menu) {
        for (Integer slot : slots) {
            ItemStack item = menu.getInventory().getItem(slot);
            menu.renderSlot(slot, item == null ? new ItemStack(Material.AIR) : item);
        }
    }

    @Override
    public void onClick(Menu<?> menu, org.bukkit.event.inventory.InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        if (slots.contains(rawSlot)) {
            Bukkit.getScheduler().runTask(menu.getPlugin(), () -> inspect(menu, rawSlot));
        }
    }

    @Override
    public void onDrag(Menu<?> menu, InventoryDragEvent event) {
        for (Integer rawSlot : event.getRawSlots()) {
            if (!slots.contains(rawSlot)) {
                continue;
            }

            Bukkit.getScheduler().runTask(menu.getPlugin(), () -> inspect(menu, rawSlot));
        }
    }

    @Override
    public boolean canDragInto(Menu<?> menu, int slot) {
        return slots.contains(slot);
    }

    @Override
    public boolean canPlace(Menu<?> menu, int slot, ItemStack item) {
        return slots.contains(slot);
    }

    @Override
    public boolean canTake(Menu<?> menu, int slot) {
        return slots.contains(slot);
    }

    private void inspect(Menu<?> menu, int slot) {
        ItemStack item = menu.getInventory().getItem(slot);
        if (item != null && item.getType() != Material.AIR && !validator.test(item)) {
            menu.renderSlot(slot, null);
            onInvalid.accept(slot, item);
            return;
        }

        onChange.accept(slot, item);
    }
}
