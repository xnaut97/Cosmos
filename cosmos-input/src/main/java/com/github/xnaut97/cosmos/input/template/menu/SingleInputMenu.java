package com.github.xnaut97.cosmos.input.template.menu;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.utilities.item.ItemCreator;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Setter
@Accessors(fluent = true)
public class SingleInputMenu extends Menu {

    private int exitSlot = 11;
    private int inputSlot = 13;
    private int confirmSlot = 15;

    private Consumer<Player> onExit = player -> {};
    private BiConsumer<Player, ItemStack> onConfirm = (player, item) -> {};
    private BiConsumer<Player, ItemStack> onInputChange =
            (player, item) -> {};
    private Predicate<ItemStack> validator = item -> item != null && item.getType() != Material.AIR;

    private ItemStack activeBackgroundItem = new ItemStack(Material.AIR);
    private ItemStack inactiveBackgroundItem = new ItemStack(Material.AIR);

    private ItemStack input = null;

    public SingleInputMenu(Plugin plugin, String title) {
        super(plugin, 3, title);
    }

    @Override
    protected void setup() {
        allowItemInput(inputSlot);

        refreshBackground(input != null);

        setItem(exitSlot, new ItemCreator(Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem()))
                .setDisplayName("&cReturn")
                .build())
                .onClick(event -> {
                    event.setCancelled(true);
                    onExit.accept(getPlayer());
                });

        setItem(inputSlot, new ItemStack(Material.AIR))
                .onClick(event -> {
                    Bukkit.getScheduler().runTask(getPlugin(), this::inspectInput);
                });

        setItem(confirmSlot, new ItemCreator(Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem()))
                .setDisplayName("&aConfirm")
                .build())
                .onClick(event -> {
                    event.setCancelled(true);

                    if(input == null) return;

                    onConfirm.accept(getPlayer(), getInventory().getItem(inputSlot));
                });

    }

    @Override
    protected void afterAllowedDrag(InventoryDragEvent event) {
        if (event.getRawSlots().contains(inputSlot)) {
            Bukkit.getScheduler().runTask(getPlugin(), this::inspectInput);
        }
    }

    @Override
    protected void afterShiftClickPlaced(Collection<Integer> changedSlots) {
        if (changedSlots.contains(inputSlot)) {
            inspectInput();
        }
    }

    private void inspectInput() {
        ItemStack item = getInventory().getItem(inputSlot);
        boolean hasInput = validator.test(item);
        refreshBackground(hasInput);
        onInputChange.accept(getPlayer(), item);
        this.input = item;
    }

    private void refreshBackground(boolean active) {

        ItemStack background = active ? activeBackgroundItem : inactiveBackgroundItem;

        for (int i = 0; i < getInventory().getSize(); i++) {

            if (i == exitSlot || i == inputSlot || i == confirmSlot) {
                continue;
            }

            renderSlot(i, background);
        }
    }

}
