package com.github.xnaut97.cosmos.menu.component;

import com.github.xnaut97.cosmos.menu.Menu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

public class SlotComponent implements MenuComponent {

    private final int slot;
    private Supplier<ItemStack> itemSupplier;
    private Consumer<InventoryClickEvent> clickHandler;
    private Consumer<InventoryCloseEvent> closeHandler;
    private Consumer<InventoryDragEvent> dragHandler;
    private boolean cancelClick = true;
    private IntPredicate dragPredicate = ignored -> false;
    private BiPredicate<Integer, ItemStack> placePredicate = (ignored, item) -> false;
    private IntPredicate takePredicate = ignored -> false;
    private Sound sound;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private String customSlotId;
    private final Map<String, Object> metadata = new HashMap<>();

    public SlotComponent(int slot, ItemStack item) {
        this(slot, () -> item);
    }

    public SlotComponent(int slot, Supplier<ItemStack> itemSupplier) {
        this.slot = slot;
        this.itemSupplier = Objects.requireNonNull(itemSupplier, "itemSupplier");
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return itemSupplier.get();
    }

    public String getCustomSlotId() {
        return customSlotId;
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Collections.singleton(slot);
    }

    public SlotComponent item(ItemStack item) {
        this.itemSupplier = () -> item;
        return this;
    }

    public SlotComponent item(Supplier<ItemStack> itemSupplier) {
        this.itemSupplier = Objects.requireNonNull(itemSupplier, "itemSupplier");
        return this;
    }

    public SlotComponent onClick(Consumer<InventoryClickEvent> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    public SlotComponent setOnClick(Consumer<InventoryClickEvent> clickHandler) {
        return onClick(clickHandler);
    }

    public SlotComponent clickHandler(Consumer<InventoryClickEvent> clickHandler) {
        return onClick(clickHandler);
    }

    public SlotComponent setClickHandler(Consumer<InventoryClickEvent> clickHandler) {
        return onClick(clickHandler);
    }

    public SlotComponent onClose(Consumer<InventoryCloseEvent> closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    public SlotComponent setOnClose(Consumer<InventoryCloseEvent> closeHandler) {
        return onClose(closeHandler);
    }

    public SlotComponent closeHandler(Consumer<InventoryCloseEvent> closeHandler) {
        return onClose(closeHandler);
    }

    public SlotComponent setCloseHandler(Consumer<InventoryCloseEvent> closeHandler) {
        return onClose(closeHandler);
    }

    public SlotComponent onDrag(Consumer<InventoryDragEvent> dragHandler) {
        this.dragHandler = dragHandler;
        return this;
    }

    public SlotComponent cancelClick(boolean cancelClick) {
        this.cancelClick = cancelClick;
        return this;
    }

    public SlotComponent allowDrag(boolean allowDrag) {
        this.dragPredicate = ignored -> allowDrag;
        return this;
    }

    public SlotComponent allowPlace(boolean allowPlace) {
        this.placePredicate = (ignored, item) -> allowPlace;
        return this;
    }

    public SlotComponent allowTake(boolean allowTake) {
        this.takePredicate = ignored -> allowTake;
        return this;
    }

    public SlotComponent canDragInto(IntPredicate dragPredicate) {
        this.dragPredicate = dragPredicate == null ? ignored -> false : dragPredicate;
        return this;
    }

    public SlotComponent canPlace(BiPredicate<Integer, ItemStack> placePredicate) {
        this.placePredicate = placePredicate == null ? (ignored, item) -> false : placePredicate;
        return this;
    }

    public SlotComponent canTake(IntPredicate takePredicate) {
        this.takePredicate = takePredicate == null ? ignored -> false : takePredicate;
        return this;
    }

    public SlotComponent sound(Sound sound) {
        this.sound = sound;
        return this;
    }

    public SlotComponent setSound(Sound sound) {
        return sound(sound);
    }

    public SlotComponent volume(float volume) {
        this.volume = volume;
        return this;
    }

    public SlotComponent setVolume(float volume) {
        return volume(volume);
    }

    public SlotComponent pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public SlotComponent setPitch(float pitch) {
        return pitch(pitch);
    }

    public SlotComponent customSlotId(String customSlotId) {
        this.customSlotId = customSlotId;
        return this;
    }

    public SlotComponent setCustomSlotId(String customSlotId) {
        return customSlotId(customSlotId);
    }

    public SlotComponent metadata(String key, Object value) {
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

    @Override
    public void render(Menu menu) {
        menu.renderSlot(slot, itemSupplier.get());
    }

    @Override
    public void onClick(Menu menu, InventoryClickEvent event) {
        if (cancelClick) {
            event.setCancelled(true);
        }
        if (sound != null) {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }

    @Override
    public void onClose(Menu menu, InventoryCloseEvent event) {
        if (closeHandler != null) {
            closeHandler.accept(event);
        }
    }

    @Override
    public void onDrag(Menu menu, InventoryDragEvent event) {
        if (dragHandler != null && event.getRawSlots().contains(slot)) {
            dragHandler.accept(event);
        }
    }

    @Override
    public boolean canDragInto(Menu menu, int slot) {
        return this.slot == slot && dragPredicate.test(slot);
    }

    @Override
    public boolean canPlace(Menu menu, int slot, ItemStack item) {
        return this.slot == slot && placePredicate.test(slot, item);
    }

    @Override
    public boolean canTake(Menu menu, int slot) {
        return this.slot == slot && takePredicate.test(slot);
    }
}
