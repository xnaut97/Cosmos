package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DashboardMenu extends Menu {

    private final List<MenuComponent> refreshComponents = new ArrayList<>();
    private long refreshIntervalTicks = 20L;
    private BukkitTask refreshTask;
    private Supplier<Object> asyncDataLoader;
    private Consumer<Object> dataHandler = data -> {};

    public DashboardMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
    }

    @Override
    protected void setup() {
    }

    public DashboardMenu refreshIntervalTicks(long refreshIntervalTicks) {
        this.refreshIntervalTicks = Math.max(1L, refreshIntervalTicks);
        return this;
    }

    public DashboardMenu refreshing(MenuComponent component) {
        if (component != null) {
            refreshComponents.add(component);
            addComponent(component);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> DashboardMenu asyncData(Supplier<T> loader, Consumer<T> handler) {
        this.asyncDataLoader = () -> loader == null ? null : loader.get();
        this.dataHandler = data -> {
            if (handler != null) {
                handler.accept((T) data);
            }
        };
        return this;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        super.onOpen(event);
        startRefresh();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        stopRefresh();
        super.onClose(event);
    }

    private void startRefresh() {
        stopRefresh();
        refreshTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::refreshDashboard, refreshIntervalTicks, refreshIntervalTicks);
    }

    private void stopRefresh() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    private void refreshDashboard() {
        for (MenuComponent component : new ArrayList<>(refreshComponents)) {
            component.render(this);
            component.onUpdate(this);
        }
        if (asyncDataLoader != null && !getState().isLoading()) {
            loadAsync(asyncDataLoader, dataHandler);
        }
    }
}
