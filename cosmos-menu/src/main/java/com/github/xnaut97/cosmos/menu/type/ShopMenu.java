package com.github.xnaut97.cosmos.menu.type;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ShopMenu extends PagedTransactionMenu<ShopMenu.Product> {

    private final List<Product> products = new ArrayList<>();
    private Supplier<List<Product>> productLoader;
    private Predicate<Product> categoryFilter = product -> true;
    private boolean loaded;

    public ShopMenu(Plugin plugin, String title) {
        super(plugin, 6, title);
    }

    @Override
    protected void setup() {
        if (!loaded && productLoader != null) {
            loaded = true;
            loadAsync(productLoader, this::setProducts);
            return;
        }
        refreshPage();
    }

    public ShopMenu products(List<Product> products) {
        setProducts(products);
        return this;
    }

    public ShopMenu loadProducts(Supplier<List<Product>> productLoader) {
        this.productLoader = productLoader;
        this.loaded = false;
        return this;
    }

    public ShopMenu category(String category) {
        this.categoryFilter = product -> category == null || category.equals(product.getCategory());
        return this;
    }

    public ShopMenu filter(Predicate<Product> filter) {
        this.categoryFilter = filter == null ? product -> true : filter;
        return this;
    }

    @Override
    protected int getItemCount() {
        return filtered().size();
    }

    @Override
    protected void renderPagedItem(int slot, Product product) {
        setItem(slot, product.getDisplayItem())
                .onClick(event -> {
                    event.setCancelled(true);
                    if (getState().isLocked()) {
                        return;
                    }
                    Player player = (Player) event.getWhoClicked();
                    if (event.isRightClick()) {
                        product.sell(player, this);
                    } else {
                        product.buy(player, this);
                    }
                });
    }

    @Override
    protected void refreshPage() {
        renderPagedItems(filtered());
    }

    private void setProducts(List<Product> products) {
        this.products.clear();
        if (products != null) {
            this.products.addAll(products);
        }
        refreshPage();
    }

    private List<Product> filtered() {
        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (categoryFilter.test(product)) {
                filtered.add(product);
            }
        }
        return filtered;
    }

    @Getter
    public static class Product {
        private final ItemStack displayItem;
        private String category;
        private BiConsumer<Player, ShopMenu> buyHandler = (player, menu) -> {};
        private BiConsumer<Player, ShopMenu> sellHandler = (player, menu) -> {};

        public Product(ItemStack displayItem) {
            this.displayItem = displayItem == null ? new ItemStack(Material.AIR) : displayItem;
        }

        public Product category(String category) {
            this.category = category;
            return this;
        }

        public Product onBuy(BiConsumer<Player, ShopMenu> buyHandler) {
            this.buyHandler = buyHandler == null ? (player, menu) -> {} : buyHandler;
            return this;
        }

        public Product onSell(BiConsumer<Player, ShopMenu> sellHandler) {
            this.sellHandler = sellHandler == null ? (player, menu) -> {} : sellHandler;
            return this;
        }

        private void buy(Player player, ShopMenu menu) {
            buyHandler.accept(player, menu);
        }

        private void sell(Player player, ShopMenu menu) {
            sellHandler.accept(player, menu);
        }
    }
}
