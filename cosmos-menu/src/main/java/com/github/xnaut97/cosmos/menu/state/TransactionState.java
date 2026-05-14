package com.github.xnaut97.cosmos.menu.state;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class TransactionState extends MenuState {

    private volatile boolean locked;
    private volatile long lockExpiresAtMillis;
    private String transactionId;
    private ItemStack loadingItem;

    public TransactionState locked(boolean locked) {
        this.locked = locked;
        return this;
    }

    public TransactionState lockExpiresAtMillis(long lockExpiresAtMillis) {
        this.lockExpiresAtMillis = lockExpiresAtMillis;
        return this;
    }

    public TransactionState transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionState loadingItem(ItemStack loadingItem) {
        this.loadingItem = loadingItem;
        return this;
    }
}
