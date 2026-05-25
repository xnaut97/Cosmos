package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.state.PagedMenuState;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@Getter
abstract class PagedTransactionMenu<T> extends TransactionMenu {

    private final PagedMenuState pageState = new PagedMenuState();
    private final List<Integer> itemSlots = new ArrayList<>();
    private int previousSlot = 45;
    private int infoSlot = 49;
    private int nextSlot = 53;

    PagedTransactionMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
        setContentSlots(10, Math.min(getSize() - 11, 43));
    }

    public void setContentSlots(int startSlot, int endSlot) {
        itemSlots.clear();
        for (int slot = startSlot; slot <= endSlot && slot < getSize(); slot++) {
            itemSlots.add(slot);
        }
        pageState.startSlot(startSlot).endSlot(endSlot).itemsPerPage(itemSlots.size());
        clampPage(getItemCount());
    }

    public void setPageButtons(int previousSlot, int infoSlot, int nextSlot) {
        this.previousSlot = previousSlot;
        this.infoSlot = infoSlot;
        this.nextSlot = nextSlot;
    }

    protected void renderPagedItems(List<T> items) {
        clampPage(items.size());
        int start = pageState.getCurrentPage() * Math.max(1, pageState.getItemsPerPage());
        for (int i = 0; i < itemSlots.size(); i++) {
            int slot = itemSlots.get(i);
            int index = start + i;
            if (index >= items.size()) {
                removeItem(slot);
            } else {
                renderPagedItem(slot, items.get(index));
            }
        }
        renderPageButtons(items.size());
    }

    protected abstract int getItemCount();

    protected abstract void renderPagedItem(int slot, T item);

    protected int getCurrentPage() {
        return pageState.getCurrentPage();
    }

    protected int getMaxPage(int count) {
        int perPage = pageState.getItemsPerPage();
        return perPage <= 0 ? 0 : Math.max(0, (count - 1) / perPage);
    }

    protected void setCurrentPage(int page) {
        pageState.currentPage(Math.min(Math.max(0, page), getMaxPage(getItemCount())));
    }

    private void clampPage(int count) {
        pageState.currentPage(Math.min(pageState.getCurrentPage(), getMaxPage(count)));
    }

    private void renderPageButtons(int count) {
        int max = getMaxPage(count);
        if (previousSlot >= 0 && previousSlot < getSize()) {
            if (getCurrentPage() > 0) {
                setItem(previousSlot, pageButton(Material.ARROW, "&ePrevious"))
                        .onClick(event -> {
                            event.setCancelled(true);
                            setCurrentPage(getCurrentPage() - 1);
                            refreshPage();
                        });
            } else {
                removeItem(previousSlot);
            }
        }
        if (infoSlot >= 0 && infoSlot < getSize()) {
            setItem(infoSlot, pageButton(Material.PAPER, "&ePage " + (getCurrentPage() + 1) + "/" + (max + 1)))
                    .onClick(event -> event.setCancelled(true));
        }
        if (nextSlot >= 0 && nextSlot < getSize()) {
            if (getCurrentPage() < max) {
                setItem(nextSlot, pageButton(Material.ARROW, "&eNext"))
                        .onClick(event -> {
                            event.setCancelled(true);
                            setCurrentPage(getCurrentPage() + 1);
                            refreshPage();
                        });
            } else {
                removeItem(nextSlot);
            }
        }
    }

    protected abstract void refreshPage();

    private ItemStack pageButton(Material material, String name) {
        return createButton(material, name, null, false);
    }
}
