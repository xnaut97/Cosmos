package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class FormMenu extends Menu {

    private final Map<String, Object> values = new LinkedHashMap<>();
    private final List<Field> fields = new ArrayList<>();
    private int submitSlot = 49;
    private int cancelSlot = 45;
    private Predicate<FormMenu> validator = form -> true;
    private BiConsumer<Player, Map<String, Object>> submitHandler = (player, values) -> {};
    private BiConsumer<Player, Map<String, Object>> cancelHandler = (player, values) -> {};

    public FormMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
    }

    @Override
    protected void setup() {
        for (Field field : fields) {
            field.render(this);
        }
        setItem(submitSlot, createButton(Material.EMERALD_BLOCK, "&aSubmit", null, false))
                .onClick(event -> {
                    event.setCancelled(true);
                    if (!validator.test(this)) {
                        return;
                    }
                    submitHandler.accept((Player) event.getWhoClicked(), values());
                });
        setItem(cancelSlot, createButton(Material.BARRIER, "&cCancel", null, false))
                .onClick(event -> {
                    event.setCancelled(true);
                    cancelHandler.accept((Player) event.getWhoClicked(), values());
                });
    }

    public FormMenu controls(int submitSlot, int cancelSlot) {
        this.submitSlot = submitSlot;
        this.cancelSlot = cancelSlot;
        return this;
    }

    public FormMenu toggle(String key, int slot, ItemStack enabled, ItemStack disabled, boolean initial) {
        values.put(key, initial);
        fields.add(menu -> menu.setItem(slot, Boolean.TRUE.equals(values.get(key)) ? enabled : disabled)
                .onClick(event -> {
                    event.setCancelled(true);
                    values.put(key, !Boolean.TRUE.equals(values.get(key)));
                    menu.renderComponents();
                }));
        return this;
    }

    public FormMenu button(String key, int slot, ItemStack item, Object value) {
        fields.add(menu -> menu.setItem(slot, item)
                .onClick(event -> {
                    event.setCancelled(true);
                    values.put(key, value);
                    menu.renderComponents();
                }));
        return this;
    }

    public FormMenu select(String key, int slot, List<?> options, ItemStack item) {
        values.put(key, options == null || options.isEmpty() ? null : options.get(0));
        fields.add(menu -> menu.setItem(slot, item)
                .onClick(event -> {
                    event.setCancelled(true);
                    advance(key, options);
                    menu.renderComponents();
                }));
        return this;
    }

    public FormMenu input(String key, int slot, ItemStack item) {
        values.put(key, null);
        fields.add(menu -> menu.setItem(slot, item).onClick(event -> event.setCancelled(true)));
        return this;
    }

    public FormMenu value(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public FormMenu validator(Predicate<FormMenu> validator) {
        this.validator = validator == null ? form -> true : validator;
        return this;
    }

    public FormMenu onSubmit(BiConsumer<Player, Map<String, Object>> submitHandler) {
        this.submitHandler = submitHandler == null ? (player, values) -> {} : submitHandler;
        return this;
    }

    public FormMenu onCancel(BiConsumer<Player, Map<String, Object>> cancelHandler) {
        this.cancelHandler = cancelHandler == null ? (player, values) -> {} : cancelHandler;
        return this;
    }

    public Map<String, Object> values() {
        return new LinkedHashMap<>(values);
    }

    private void advance(String key, List<?> options) {
        if (options == null || options.isEmpty()) {
            values.put(key, null);
            return;
        }
        int index = options.indexOf(values.get(key));
        values.put(key, options.get((index + 1) % options.size()));
    }

    private interface Field {
        void render(FormMenu menu);
    }
}
