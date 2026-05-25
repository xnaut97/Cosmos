package com.github.xnaut97.cosmos.menu;

import com.github.xnaut97.cosmos.utilities.java.ClassScanner;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class MenuRegistry {

    public static final String MENU_TYPE_PACKAGE = "com.github.xnaut97.cosmos.menu.type";

    private final Map<String, RegisteredMenu> menus = new ConcurrentHashMap<>();
    private final AtomicBoolean discovered = new AtomicBoolean(false);
    private volatile Plugin schedulerPlugin;

    public MenuRegistry() {
    }

    public MenuRegistry(Plugin schedulerPlugin) {
        this.schedulerPlugin = schedulerPlugin;

        discover(schedulerPlugin);
    }

    public void register(String id, String description, Supplier<Menu> supplier) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(supplier, "supplier");

        String normalizedId = normalizeId(id);
        menus.put(normalizedId, new RegisteredMenu(normalizedId, id, description, supplier));

        schedulerPlugin.getLogger().info(String.format("Registered menu with id '%s'", normalizedId));
    }

    public boolean openMenu(String id, Player player) {
        RegisteredMenu registeredMenu = menus.get(normalizeId(id));
        if (registeredMenu == null || player == null) {
            return false;
        }

        Runnable openTask = () -> {
            Menu menu = registeredMenu.getSupplier().get();
            if (menu != null) {
                menu.open(player);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            openTask.run();
        } else {
            Plugin plugin = schedulerPlugin;
            if (plugin == null) {
                throw new IllegalStateException("A scheduler plugin is required to open menus asynchronously");
            }
            Bukkit.getScheduler().runTask(plugin, openTask);
        }
        return true;
    }

    public boolean openMenu(String id, Player player, Plugin plugin) {
        RegisteredMenu registeredMenu = menus.get(normalizeId(id));
        if (registeredMenu == null || player == null || plugin == null) {
            return false;
        }

        Runnable openTask = () -> {
            Menu menu = registeredMenu.getSupplier().get();
            if (menu != null) {
                menu.open(player);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            openTask.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, openTask);
        }
        return true;
    }

    public void discover(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.schedulerPlugin = plugin;
        if (!discovered.compareAndSet(false, true)) {
            return;
        }

        Set<Class<?>> classes = new ClassScanner(plugin).scan(MENU_TYPE_PACKAGE);
        for (Class<?> clazz : classes) {
            registerDiscovered(plugin, clazz);
        }
    }

    public List<RegisteredMenu> getMenus() {
        List<RegisteredMenu> snapshot = new ArrayList<>(menus.values());
        Collections.sort(snapshot, Comparator.comparing(RegisteredMenu::getId));
        return Collections.unmodifiableList(snapshot);
    }

    public Map<String, RegisteredMenu> asMap() {
        Map<String, RegisteredMenu> snapshot = new LinkedHashMap<>();
        for (RegisteredMenu menu : getMenus()) {
            snapshot.put(menu.getId(), menu);
        }
        return Collections.unmodifiableMap(snapshot);
    }

    private void registerDiscovered(Plugin plugin, Class<?> clazz) {
        if (!isDiscoverableMenu(clazz)) {
            return;
        }

        Supplier<Menu> supplier = supplierFor(plugin, clazz);
        if (supplier == null) {
            return;
        }

        register(clazz.getSimpleName(), "Auto-discovered menu type", supplier);
    }

    private boolean isDiscoverableMenu(Class<?> clazz) {
        if (clazz == null || !Menu.class.isAssignableFrom(clazz)) {
            return false;
        }
        int modifiers = clazz.getModifiers();
        return !clazz.equals(MenuPreviewHub.class)
                && !clazz.getName().contains("$")
                && !clazz.isInterface()
                && !clazz.isAnonymousClass()
                && !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers);
    }

    @SuppressWarnings("unchecked")
    private Supplier<Menu> supplierFor(Plugin plugin, Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor(Plugin.class, String.class);
            return () -> newMenu((Constructor<? extends Menu>) constructor, plugin, clazz.getSimpleName());
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Constructor<?> constructor = clazz.getConstructor(Plugin.class, int.class, String.class);
            return () -> newMenu((Constructor<? extends Menu>) constructor, plugin, 6, clazz.getSimpleName());
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private Menu newMenu(Constructor<? extends Menu> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create menu preview", exception);
        }
    }

    private String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    @Getter
    public static final class RegisteredMenu {
        private final String id;
        private final String name;
        private final String description;
        private final Supplier<Menu> supplier;

        private RegisteredMenu(String id, String name, String description, Supplier<Menu> supplier) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.supplier = supplier;
        }

    }
}
