package com.github.xnaut97.cosmos.utilities.item;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * A class that manages functions relating to {@link ItemStack} that floating in
 * {@link Item} form.
 */
@Getter
public class FloatingItem {

    /**
     * Find a {@link FloatingItem} with given ID near the given {@link Location}.
     */
    public static Optional<FloatingItem> find(Location location, String id) {
        World world = location.getWorld();
        if (world == null) {
            return Optional.empty();
        }

        for (Object obj : world.getNearbyEntities(location, 1, 1, 1)) {
            if (!(obj instanceof Item)) {
                continue;
            }

            Item item = (Item) obj;

            if (!item.hasMetadata("id")) {
                continue;
            }

            if (item.getMetadata("id").isEmpty()) {
                continue;
            }

            String metaId = item.getMetadata("id").get(0).asString();
            if (!id.equals(metaId)) {
                continue;
            }

            return Optional.of(new FloatingItem(id, item.getItemStack()));
        }

        return Optional.empty();
    }

    /**
     * Id c{@link FloatingItem} n(phbivc{@link ItemStack} r
     * trbth
     */
    private final String id;

    /**
     * {@link Item} entity of this {@link FloatingItem}.
     */
    private Item item;

    /**
     * {@link ItemStack} form of this {@link FloatingItem}.
     */
    private final ItemStack itemStack;

    public FloatingItem(String id, ItemStack itemStack) {
        this.id = id;
        this.itemStack = itemStack;
    }

    private void setItem(Item item) {
        this.item = item;
    }

    /**
     * Summon {@link FloatingItem} above the given {@link Block}.
     */
    public void summon(Block block, Plugin plugin) {
        World world = block.getWorld();
        Location location = block.getLocation().add(0.5, 1.2, 0.5);

        ItemStack clone = itemStack.clone();
        clone.setAmount(1);

        Item itemEntity = world.dropItem(location, clone);

        itemEntity.setVelocity(new Vector(0, 0.1, 0));
        itemEntity.setPickupDelay(Integer.MAX_VALUE);
        itemEntity.setCustomNameVisible(true);
        itemEntity.setCustomName(getDisplayName());

        itemEntity.setMetadata("id", new FixedMetadataValue(plugin, id));

        this.item = itemEntity;

        world.playSound(location, Sound.ENTITY_ITEM_PICKUP, 0.3F, 0.3F);
    }

    /**
     * Remove the {@link FloatingItem}.
     */
    public void remove() {
        if (getItem() != null)
            getItem().remove();
    }

    private String getDisplayName() {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }

        String name = itemStack.getType().name();
        StringBuilder builder = new StringBuilder();

        boolean upper = true;

        for (char c : name.toCharArray()) {
            if (c == '_') {
                builder.append(' ');
                upper = true;
            } else {
                builder.append(upper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upper = false;
            }
        }

        return builder.toString();
    }

}

