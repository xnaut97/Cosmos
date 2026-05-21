package com.github.xnaut97.cosmos.utilities;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.aggregate.VersionHandle;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemCreator implements Cloneable {

    private final ItemStack item;

    private ItemMeta meta;

    private List<String> lore;

    private String texture;

    private GameProfile gameProfile;

    public ItemCreator(ItemStack item) {
        this.item = item;
        if (item.getType() != Material.AIR) {
            this.meta = item.getItemMeta();
            assert this.meta != null;
            this.lore = this.meta.hasLore() ? this.meta.getLore() : Lists.newArrayList();
        }
    }

    public ItemCreator(Material material) {
        this.item = new ItemStack(material);
        if (material != Material.AIR) {
            this.meta = this.item.getItemMeta();
            assert this.meta != null;
            this.lore = this.meta.hasLore() ? this.meta.getLore() : Lists.newArrayList();
        }
    }

    public ItemCreator setTexture(String texture) {
        if (!(this.meta instanceof SkullMeta))
            return this;
        if (texture == null || texture.isEmpty())
            return this;
        this.texture = texture;
        return this;
    }

    public ItemCreator setTexture(OfflinePlayer player) {
        if (!(this.meta instanceof SkullMeta) || player == null)
            return this;
        GameProfile cachedProfile = SkullProfileService.getCachedProfile(player);
        if (cachedProfile != null) {
            this.gameProfile = cachedProfile;
        }
        return this;
    }

    public ItemCreator setTexture(GameProfile profile) {
        if (!(this.meta instanceof SkullMeta) || profile == null)
            return this;
        this.gameProfile = profile;
        return this;
    }

    public ItemCreator setDisplayName(String displayName) {
        this.meta.setDisplayName(displayName.replace("&", "§"));
        return this;
    }

    public ItemCreator setAmount(int amount) {
        this.item.setAmount(Math.max(0, Math.min(64, amount)));
        return this;
    }

    public ItemCreator addLore(String... str) {
        for (String s : str) {
            this.lore.add(s.replace("&", "§"));
        }
        return this;
    }

    public ItemCreator removeLore(int index) {
        if (index < 0 || index >= lore.size())
            return this;
        this.lore.remove(index);
        return this;
    }

    public ItemCreator clearLore() {
        this.lore.clear();
        return this;
    }

    public ItemCreator setLore(List<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore.stream().map(e -> e.replace("&", "§"))
                .collect(Collectors.toList()));
        return this;
    }

    public ItemCreator setLore(String... str) {
        return this.setLore(Lists.newArrayList(str));
    }

    public ItemCreator setLore(int index, String str) {
        if (index < 0 || index >= lore.size())
            return this;
        this.lore.set(index, str.replace("&", "§"));
        return this;
    }

    public ItemCreator addEnchant(Enchantment enchantment, int level, boolean ignoreRestriction) {
        if (level < 1)
            level = 1;
        this.meta.addEnchant(enchantment, level, ignoreRestriction);
        return this;
    }

    public ItemCreator removeEnchant(Enchantment enchantment) {
        this.meta.removeEnchant(enchantment);
        return this;
    }

    public ItemCreator setUnbreakable(boolean unbreakable) {
        if (getVersionNumber() < 11)
            return this;
        try {
            meta.getClass().getDeclaredMethod("setUnbreakable", boolean.class).invoke(meta, unbreakable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public ItemCreator setCustomModelData(int modelData) {
        try {
            meta.getClass().getDeclaredMethod("setCustomModelData", int.class)
                    .invoke(meta, modelData);
        } catch (Exception e) {
            System.out.println("Current version is not support setCustomModelData");
        }
        return this;
    }

    public ItemCreator addFlag(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemCreator removeFlag(ItemFlag... flags) {
        this.meta.removeItemFlags(flags);
        return this;
    }

    public ItemCreator setGlow(boolean glow) {
        if (glow) {
            addEnchant(XEnchantment.UNBREAKING.get(), 1, true);
            addFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            removeEnchant(XEnchantment.UNBREAKING.get());
            removeFlag(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemStack build() {
        return buildItem();
    }

    private ItemStack buildItem() {
        if (this.item.getType() != Material.AIR) {
            if (this.texture != null)
                applyGameProfile(SkullProfileService.getOrCreateTextureProfile(this.texture));
            if (this.gameProfile != null)
                applyGameProfile(this.gameProfile);
            this.meta.setLore(this.lore);
            this.item.setItemMeta(meta);
        }
        return this.item;
    }

    private void applyGameProfile(GameProfile gameProfile) {
        SkullProfileService.applyGameProfile((SkullMeta) meta, gameProfile);
    }

    private String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1);
    }

    private int getVersionNumber() {
        return Integer.parseInt(getVersion().split("_")[1]);
    }

    @Override
    public ItemCreator clone() {
        try {
            return (ItemCreator) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
