package com.github.xnaut97.cosmos.menu.type;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AuctionHouseMenu extends PagedTransactionMenu<AuctionHouseMenu.Listing> {

    private final List<Listing> listings = new ArrayList<>();
    private Supplier<List<Listing>> listingLoader;
    private Predicate<Listing> filter = listing -> true;
    private Comparator<Listing> sorter;
    private int previewSlot = 4;
    private boolean loaded;

    public AuctionHouseMenu(Plugin plugin, String title) {
        super(plugin, 6, title);
    }

    public AuctionHouseMenu(Plugin plugin, int row, String title) {
        super(plugin, row, title);
    }

    @Override
    protected void setup() {
        if (!loaded && listingLoader != null) {
            loaded = true;
            loadAsync(listingLoader, this::setListings);
            return;
        }
        refreshPage();
    }

    public AuctionHouseMenu loadListings(Supplier<List<Listing>> listingLoader) {
        this.listingLoader = listingLoader;
        this.loaded = false;
        return this;
    }

    public AuctionHouseMenu filter(Predicate<Listing> filter) {
        this.filter = filter == null ? listing -> true : filter;
        return this;
    }

    public AuctionHouseMenu sorter(Comparator<Listing> sorter) {
        this.sorter = sorter;
        return this;
    }

    public AuctionHouseMenu previewSlot(int previewSlot) {
        this.previewSlot = previewSlot;
        return this;
    }

    @Override
    protected int getItemCount() {
        return visible().size();
    }

    @Override
    protected void renderPagedItem(int slot, Listing listing) {
        setItem(slot, listing.getDisplayItem())
                .onClick(event -> {
                    event.setCancelled(true);
                    if (event.isRightClick()) {
                        setItem(previewSlot, listing.getPreviewItem()).onClick(click -> click.setCancelled(true));
                        return;
                    }
                    if (!getState().isLocked()) {
                        listing.buy((Player) event.getWhoClicked(), this);
                    }
                });
    }

    @Override
    protected void refreshPage() {
        renderPagedItems(visible());
    }

    private void setListings(List<Listing> listings) {
        this.listings.clear();
        if (listings != null) {
            this.listings.addAll(listings);
        }
        refreshPage();
    }

    private List<Listing> visible() {
        List<Listing> visible = new ArrayList<>();
        for (Listing listing : listings) {
            if (filter.test(listing)) {
                visible.add(listing);
            }
        }
        if (sorter != null) {
            Collections.sort(visible, sorter);
        }
        return visible;
    }

    @Getter
    public static class Listing {
        private final ItemStack displayItem;
        private ItemStack previewItem;
        private BiConsumer<Player, AuctionHouseMenu> buyHandler = (player, menu) -> {};

        public Listing(ItemStack displayItem) {
            this.displayItem = displayItem == null ? new ItemStack(Material.AIR) : displayItem;
            this.previewItem = this.displayItem;
        }

        public Listing previewItem(ItemStack previewItem) {
            this.previewItem = previewItem == null ? displayItem : previewItem;
            return this;
        }

        public Listing onBuy(BiConsumer<Player, AuctionHouseMenu> buyHandler) {
            this.buyHandler = buyHandler == null ? (player, menu) -> {} : buyHandler;
            return this;
        }

        private void buy(Player player, AuctionHouseMenu menu) {
            buyHandler.accept(player, menu);
        }
    }
}
