package com.github.xnaut97.cosmos.menu.type;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class ConfirmMenu extends TransactionMenu {

    private int confirmSlot = 11;
    private int cancelSlot = 15;
    private ItemStack confirmItem;
    private ItemStack cancelItem;
    private Consumer<Player> confirmHandler = player -> {};
    private Consumer<Player> cancelHandler = player -> {};
    private boolean autoClose = true;
    private boolean confirmed;

    public ConfirmMenu(Plugin plugin, String title) {
        super(plugin, 3, title);
        this.confirmItem = createButton(Material.EMERALD_BLOCK, "&aConfirm", null, false);
        this.cancelItem = createButton(Material.REDSTONE_BLOCK, "&cCancel", null, false);
    }

    @Override
    protected void setup() {
        setConfirmButton(confirmSlot, confirmItem);
        setCancelButton(cancelSlot, cancelItem);
    }

    public ConfirmMenu confirmButton(int slot, ItemStack item) {
        this.confirmSlot = slot;
        this.confirmItem = item;
        return this;
    }

    public ConfirmMenu cancelButton(int slot, ItemStack item) {
        this.cancelSlot = slot;
        this.cancelItem = item;
        return this;
    }

    public ConfirmMenu onConfirm(Consumer<Player> confirmHandler) {
        this.confirmHandler = confirmHandler == null ? player -> {} : confirmHandler;
        return this;
    }

    public ConfirmMenu onCancel(Consumer<Player> cancelHandler) {
        this.cancelHandler = cancelHandler == null ? player -> {} : cancelHandler;
        return this;
    }

    public ConfirmMenu autoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    @Override
    protected void onConfirm(InventoryClickEvent event) {
        if (confirmed) {
            return;
        }
        confirmed = true;
        getState().readonly(true);
        confirmHandler.accept((Player) event.getWhoClicked());
        if (autoClose) {
            event.getWhoClicked().closeInventory();
        }
    }

    @Override
    protected void onCancel(InventoryClickEvent event) {
        cancelHandler.accept((Player) event.getWhoClicked());
        if (autoClose) {
            event.getWhoClicked().closeInventory();
        }
    }
}
