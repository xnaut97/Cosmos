package com.github.xnaut97.cosmos.menu.type;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InputMenu extends com.github.xnaut97.cosmos.menu.Menu {

    public enum InputType {
        CHAT,
        ANVIL,
        SIGN
    }

    private final Map<String, String> values = new LinkedHashMap<>();
    private int startSlot = 11;
    private int completeSlot = 15;
    private int cancelSlot = 22;
    private BiConsumer<Player, InputMenu> inputStarter = (player, menu) -> {};
    private BiConsumer<Player, Map<String, String>> completeHandler = (player, values) -> {};
    private Consumer<Player> cancelHandler = player -> {};

    public InputMenu(Plugin plugin, String title) {
        super(plugin, 3, title);
    }

    @Override
    protected void setup() {
        setItem(startSlot, createButton(Material.PAPER, "&eInput", null, false))
                .onClick(event -> {
                    event.setCancelled(true);
                    inputStarter.accept((Player) event.getWhoClicked(), this);
                });
        setItem(completeSlot, createButton(Material.EMERALD_BLOCK, "&aComplete", null, false))
                .onClick(event -> {
                    event.setCancelled(true);
                    completeHandler.accept((Player) event.getWhoClicked(), new LinkedHashMap<>(values));
                });
        setItem(cancelSlot, createButton(Material.BARRIER, "&cCancel", null, false))
                .onClick(event -> {
                    event.setCancelled(true);
                    cancelHandler.accept((Player) event.getWhoClicked());
                });
    }

    public InputMenu buttons(int startSlot, int completeSlot, int cancelSlot) {
        this.startSlot = startSlot;
        this.completeSlot = completeSlot;
        this.cancelSlot = cancelSlot;
        return this;
    }

    public InputMenu value(String key, String value) {
        if (key != null) {
            values.put(key, value);
        }
        return this;
    }

    public InputMenu onInputStart(BiConsumer<Player, InputMenu> inputStarter) {
        this.inputStarter = inputStarter == null ? (player, menu) -> {} : inputStarter;
        return this;
    }

    public InputMenu chatInput(BiConsumer<Player, InputMenu> inputStarter) {
        return onInputStart(inputStarter);
    }

    public InputMenu anvilInput(BiConsumer<Player, InputMenu> inputStarter) {
        return onInputStart(inputStarter);
    }

    public InputMenu signInput(BiConsumer<Player, InputMenu> inputStarter) {
        return onInputStart(inputStarter);
    }

    public InputMenu onComplete(BiConsumer<Player, Map<String, String>> completeHandler) {
        this.completeHandler = completeHandler == null ? (player, values) -> {} : completeHandler;
        return this;
    }

    public InputMenu onCancel(Consumer<Player> cancelHandler) {
        this.cancelHandler = cancelHandler == null ? player -> {} : cancelHandler;
        return this;
    }

    public Map<String, String> getValues() {
        return new LinkedHashMap<>(values);
    }
}
