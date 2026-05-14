package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.SlotComponent;
import com.github.xnaut97.cosmos.menu.state.TransactionState;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TransactionMenu<P extends Plugin> extends Menu<P> {

    private final Set<Integer> loadingSlots = new LinkedHashSet<>();
    private final Map<Integer, ItemStack> lockSnapshot = new HashMap<>();
    private BukkitTask lockTimeoutTask;

    public TransactionMenu(P plugin, int rows, String title) {
        super(plugin, rows, title);
        setState(new TransactionState());
    }

    @Override
    public TransactionState getState() {
        return (TransactionState) super.getState();
    }

    public void setState(TransactionState state) {
        super.setState(state == null ? new TransactionState() : state);
    }

    public void lock() {
        lock(0L);
    }

    public void lock(long timeoutTicks) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(getPlugin(), () -> lock(timeoutTicks));
            return;
        }

        cancelLockTimeout();
        if (!getState().isLocked()) {
            lockSnapshot.clear();
        }
        getState().locked(true);
        if (timeoutTicks > 0L) {
            getState().lockExpiresAtMillis(System.currentTimeMillis() + timeoutTicks * 50L);
            lockTimeoutTask = Bukkit.getScheduler().runTaskLater(getPlugin(), this::unlock, timeoutTicks);
        }
        renderLoadingState();
    }

    public void unlock() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(getPlugin(), this::unlock);
            return;
        }

        cancelLockTimeout();
        getState().locked(false).lockExpiresAtMillis(0L);
        restoreLockSnapshot();
        renderComponents();
    }


    public TransactionMenu<P> loadingItem(ItemStack item) {
        getState().loadingItem(item);
        return this;
    }

    public TransactionMenu<P> loadingSlot(int slot) {
        loadingSlots.add(slot);
        return this;
    }

    public SlotComponent setConfirmButton(int slot, ItemStack item) {
        return setItem(slot, item).onClick(event -> {
            event.setCancelled(true);
            if (!getState().isLocked()) {
                onConfirm(event);
            }
        });
    }

    public SlotComponent setCancelButton(int slot, ItemStack item) {
        return setItem(slot, item).onClick(event -> {
            event.setCancelled(true);
            if (!getState().isLocked()) {
                onCancel(event);
            }
        });
    }

    public <T> void confirmAsync(Supplier<T> asyncWork, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        lock();
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                T result = asyncWork.get();
                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    unlock();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                });
            } catch (Throwable throwable) {
                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    unlock();
                    if (onFailure != null) {
                        onFailure.accept(throwable);
                    }
                });
            }
        });
    }

    protected void onConfirm(InventoryClickEvent event) {
    }

    protected void onCancel(InventoryClickEvent event) {
        event.getWhoClicked().closeInventory();
    }

    @Override
    protected void onLockedClickDenied(InventoryClickEvent event) {
        renderLoadingState();
    }

    protected void renderLoadingState() {
        ItemStack loadingItem = getState().getLoadingItem();
        if (loadingItem == null) {
            return;
        }

        if (loadingSlots.isEmpty()) {
            for (int slot = 0; slot < getSize(); slot++) {
                renderLoadingSlot(slot, loadingItem);
            }
            return;
        }

        for (Integer slot : loadingSlots) {
            renderLoadingSlot(slot, loadingItem);
        }
    }

    private void renderLoadingSlot(int slot, ItemStack loadingItem) {
        if (!lockSnapshot.containsKey(slot)) {
            ItemStack current = getInventory().getItem(slot);
            lockSnapshot.put(slot, current == null ? null : current.clone());
        }
        renderSlot(slot, loadingItem);
    }

    private void restoreLockSnapshot() {
        for (Map.Entry<Integer, ItemStack> entry : lockSnapshot.entrySet()) {
            renderSlot(entry.getKey(), entry.getValue());
        }
        lockSnapshot.clear();
    }

    private void cancelLockTimeout() {
        if (lockTimeoutTask != null) {
            lockTimeoutTask.cancel();
            lockTimeoutTask = null;
        }
    }
}
