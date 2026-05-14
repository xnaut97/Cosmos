package com.github.xnaut97.cosmos.menu;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class MenuContent {
    private final int slot;
    private final ItemStack item;
    private final Map<String, Object> metadata = new HashMap<>();
    @Deprecated
    private Consumer<InventoryClickEvent> onClick;
    @Deprecated
    private Consumer<InventoryCloseEvent> onClose;
    @Deprecated
    private Sound sound;
    @Deprecated
    private float volume = 1.0f;
    @Deprecated
    private float pitch = 1.0f;
    private String customSlotId;

    public MenuContent(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    @Deprecated
    protected void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }

        if (onClick != null) {
            onClick.accept(event);
        }
    }

    @Deprecated
    protected void handleClose(InventoryCloseEvent event) {
        if (onClose != null) {
            onClose.accept(event);
        }
    }

    @Deprecated
    public Consumer<InventoryClickEvent> getClickHandler() {
        return onClick;
    }

    @Deprecated
    public MenuContent onClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
        return this;
    }

    @Deprecated
    public MenuContent setOnClick(Consumer<InventoryClickEvent> onClick) {
        return onClick(onClick);
    }

    @Deprecated
    public MenuContent clickHandler(Consumer<InventoryClickEvent> clickHandler) {
        return onClick(clickHandler);
    }

    @Deprecated
    public MenuContent setClickHandler(Consumer<InventoryClickEvent> clickHandler) {
        return onClick(clickHandler);
    }

    @Deprecated
    public Consumer<InventoryCloseEvent> getCloseHandler() {
        return onClose;
    }

    @Deprecated
    public MenuContent onClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
        return this;
    }

    @Deprecated
    public MenuContent setOnClose(Consumer<InventoryCloseEvent> onClose) {
        return onClose(onClose);
    }

    @Deprecated
    public MenuContent closeHandler(Consumer<InventoryCloseEvent> closeHandler) {
        return onClose(closeHandler);
    }

    @Deprecated
    public MenuContent setCloseHandler(Consumer<InventoryCloseEvent> closeHandler) {
        return onClose(closeHandler);
    }

    @Deprecated
    public MenuContent sound(Sound sound) {
        this.sound = sound;
        return this;
    }

    @Deprecated
    public MenuContent setSound(Sound sound) {
        return sound(sound);
    }

    @Deprecated
    public MenuContent volume(float volume) {
        this.volume = volume;
        return this;
    }

    @Deprecated
    public MenuContent setVolume(float volume) {
        return volume(volume);
    }

    @Deprecated
    public MenuContent pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    @Deprecated
    public MenuContent setPitch(float pitch) {
        return pitch(pitch);
    }

    public MenuContent customSlotId(String customSlotId) {
        this.customSlotId = customSlotId;
        return this;
    }

    public MenuContent setCustomSlotId(String customSlotId) {
        return customSlotId(customSlotId);
    }

    public MenuContent metadata(String key, Object value) {
        if (key == null) {
            return this;
        }
        if (value == null) {
            metadata.remove(key);
        } else {
            metadata.put(key, value);
        }
        return this;
    }
}
