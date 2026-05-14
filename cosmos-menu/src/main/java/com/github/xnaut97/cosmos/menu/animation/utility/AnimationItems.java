package com.github.xnaut97.cosmos.menu.animation.utility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AnimationItems {

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private static final ItemStack WHITE = pane(Material.WHITE_STAINED_GLASS_PANE);
    private static final ItemStack ORANGE = pane(Material.ORANGE_STAINED_GLASS_PANE);
    private static final ItemStack MAGENTA = pane(Material.MAGENTA_STAINED_GLASS_PANE);
    private static final ItemStack LIGHT_BLUE = pane(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    private static final ItemStack YELLOW = pane(Material.YELLOW_STAINED_GLASS_PANE);
    private static final ItemStack LIME = pane(Material.LIME_STAINED_GLASS_PANE);
    private static final ItemStack PINK = pane(Material.PINK_STAINED_GLASS_PANE);
    private static final ItemStack GRAY = pane(Material.GRAY_STAINED_GLASS_PANE);
    private static final ItemStack LIGHT_GRAY = pane(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    private static final ItemStack CYAN = pane(Material.CYAN_STAINED_GLASS_PANE);
    private static final ItemStack PURPLE = pane(Material.PURPLE_STAINED_GLASS_PANE);
    private static final ItemStack BLUE = pane(Material.BLUE_STAINED_GLASS_PANE);
    private static final ItemStack GREEN = pane(Material.GREEN_STAINED_GLASS_PANE);
    private static final ItemStack RED = pane(Material.RED_STAINED_GLASS_PANE);
    private static final ItemStack BLACK = pane(Material.BLACK_STAINED_GLASS_PANE);
    private static final ItemStack EMERALD = named(new ItemStack(Material.EMERALD), "&aConfirm");
    private static final ItemStack BARRIER = named(new ItemStack(Material.BARRIER), "&cError");
    private static final ItemStack CLOCK = named(new ItemStack(Material.CLOCK), "&eLoading");
    private static final ItemStack CHEST = named(new ItemStack(Material.CHEST), "&6Loading");
    private static final ItemStack SPARKLE = named(new ItemStack(Material.NETHER_STAR), "&f*");
    private static final ItemStack FIRE = named(new ItemStack(Material.BLAZE_POWDER), "&6Fire");

    private static final ItemStack[] RAINBOW = {
            RED, ORANGE, YELLOW, LIME, GREEN, CYAN, LIGHT_BLUE, BLUE, PURPLE, MAGENTA, PINK
    };
    private static final ItemStack[] PREMIUM = {
            BLACK, PURPLE, MAGENTA, LIGHT_BLUE, CYAN, WHITE
    };
    private static final ItemStack[] LOADING = {
            LIGHT_GRAY, GRAY, BLACK, GRAY
    };
    private static final List<ItemStack> RAINBOW_LIST = Collections.unmodifiableList(Arrays.asList(RAINBOW));

    private AnimationItems() {
    }

    public static ItemStack pane(Material material) {
        return named(new ItemStack(material), " ");
    }

    public static ItemStack named(ItemStack item, String displayName) {
        ItemStack result = item == null ? AIR.clone() : item.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(displayName));
            meta.addItemFlags(ItemFlag.values());
            result.setItemMeta(meta);
        }
        return result;
    }

    public static ItemStack namedWithLore(ItemStack item, String displayName, List<String> lore) {
        ItemStack result = item == null ? AIR.clone() : item.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(displayName));
            if (lore == null || lore.isEmpty()) {
                meta.setLore(null);
            } else {
                String[] coloredLore = new String[lore.size()];
                for (int i = 0; i < lore.size(); i++) {
                    coloredLore[i] = color(lore.get(i));
                }
                meta.setLore(Arrays.asList(coloredLore));
            }
            meta.addItemFlags(ItemFlag.values());
            result.setItemMeta(meta);
        }
        return result;
    }

    public static ItemStack withAmount(ItemStack item, int amount) {
        ItemStack result = item == null ? AIR.clone() : item.clone();
        result.setAmount(Math.max(1, Math.min(amount, result.getMaxStackSize())));
        return result;
    }

    public static ItemStack[] rainbowPanes() {
        return RAINBOW.clone();
    }

    public static ItemStack[] premiumPanes() {
        return PREMIUM.clone();
    }

    public static ItemStack[] loadingPanes() {
        return LOADING.clone();
    }

    public static List<ItemStack> rainbowPaneList() {
        return RAINBOW_LIST;
    }

    public static ItemStack whitePane() {
        return WHITE;
    }

    public static ItemStack orangePane() {
        return ORANGE;
    }

    public static ItemStack yellowPane() {
        return YELLOW;
    }

    public static ItemStack limePane() {
        return LIME;
    }

    public static ItemStack greenPane() {
        return GREEN;
    }

    public static ItemStack cyanPane() {
        return CYAN;
    }

    public static ItemStack bluePane() {
        return BLUE;
    }

    public static ItemStack purplePane() {
        return PURPLE;
    }

    public static ItemStack pinkPane() {
        return PINK;
    }

    public static ItemStack redPane() {
        return RED;
    }

    public static ItemStack grayPane() {
        return GRAY;
    }

    public static ItemStack lightGrayPane() {
        return LIGHT_GRAY;
    }

    public static ItemStack blackPane() {
        return BLACK;
    }

    public static ItemStack emerald() {
        return EMERALD;
    }

    public static ItemStack barrier() {
        return BARRIER;
    }

    public static ItemStack clock() {
        return CLOCK;
    }

    public static ItemStack chest() {
        return CHEST;
    }

    public static ItemStack sparkle() {
        return SPARKLE;
    }

    public static ItemStack fire() {
        return FIRE;
    }

    public static String color(String value) {
        return value == null ? null : value.replace("&", "\u00A7");
    }
}
