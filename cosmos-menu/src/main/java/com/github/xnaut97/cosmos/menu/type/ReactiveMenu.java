package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class ReactiveMenu extends Menu {

    private final ObservableState reactiveState = new ObservableState();
    private final Map<String, List<MenuComponent>> bindings = new HashMap<>();

    public ReactiveMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
    }

    @Override
    protected void setup() {
    }

    public ObservableState reactiveState() {
        return reactiveState;
    }

    public ReactiveMenu bind(String key, MenuComponent component) {
        addComponent(component);
        bindings.computeIfAbsent(key, ignored -> new ArrayList<>()).add(component);
        return this;
    }

    public void setReactive(String key, Object value) {
        reactiveState.set(key, value);
        rerender(key);
    }

    public void rerender(String key) {
        List<MenuComponent> components = bindings.get(key);
        if (components == null || components.isEmpty()) {
            return;
        }
        Runnable render = () -> {
            for (MenuComponent component : new ArrayList<>(components)) {
                component.render(this);
                component.onUpdate(this);
            }
        };
        if (Bukkit.isPrimaryThread()) {
            render.run();
        } else {
            Bukkit.getScheduler().runTask(getPlugin(), render);
        }
    }

    public static class ObservableState {
        private final Map<String, Object> values = new HashMap<>();

        public synchronized Object get(String key) {
            return values.get(key);
        }

        public synchronized String getString(String key) {
            Object value = values.get(key);
            return value == null ? null : String.valueOf(value);
        }

        public synchronized int getInt(String key, int fallback) {
            Object value = values.get(key);
            return value instanceof Number ? ((Number) value).intValue() : fallback;
        }

        public synchronized void set(String key, Object value) {
            if (value == null) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
        }

        public synchronized Map<String, Object> snapshot() {
            return new HashMap<>(values);
        }
    }
}
