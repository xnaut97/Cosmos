package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.state.PagedMenuState;
import com.github.xnaut97.cosmos.utilities.ItemCreator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class PaginationMenu extends Menu {

    private List<PagedContent> contents = new ArrayList<>();
    private final Map<PageButtonType, Integer> buttonSlots = new EnumMap<>(PageButtonType.class);
    private final Map<PageButtonType, ItemStack> buttonItems = new EnumMap<>(PageButtonType.class);
    private final List<Integer> itemSlots = new ArrayList<>();

    public PaginationMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
        setState(new PagedMenuState());
        if (getSize() > 18) {
            setSlot(10, Math.min(getSize() - 11, 43));
        } else if (getSize() > 0) {
            setSlot(0, getSize() - 1);
        }
        addDefaultPageButtons();
    }

    @Override
    public PagedMenuState getState() {
        return (PagedMenuState) super.getState();
    }

    public void setState(PagedMenuState state) {
        super.setState(state == null ? new PagedMenuState() : state);
    }

    private void addDefaultPageButtons() {
        int slot = 47;
        PageButtonType[] types = PageButtonType.values();
        for (int i = 0; i < types.length; i++) {
            PageButtonType type = types[i];
            int buttonSlot = slot + i;
            if (buttonSlot >= getSize()) {
                continue;
            }

            ItemCreator creator = getButton(type);
            setNavigationButton(type, buttonSlot, creator.build());
        }
    }

    private ItemCreator getButton(PageButtonType type) {
        switch (type) {
            case FIRST:
                return new ItemCreator(Material.ARROW)
                        .setDisplayName("&eFirst")
                        .setLore("&7Back to first page");
            case PREVIOUS:
                return new ItemCreator(Material.ARROW)
                        .setDisplayName("&ePrevious")
                        .setLore("&7Return to page " + getCurrentPage());
            case INFO:
                return new ItemCreator(Material.PAPER)
                        .setDisplayName("&ePage " + (getCurrentPage() + 1));
            case NEXT:
                return new ItemCreator(Material.ARROW)
                        .setDisplayName("&eNext")
                        .setLore("&7Go to page " + (getCurrentPage() + 2));
            case LAST:
                return new ItemCreator(Material.ARROW)
                        .setDisplayName("&eLast")
                        .setLore("&7Go to last page");
            default:
                throw new IllegalArgumentException("Could not create button by type " + type);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        super.onOpen(event);
        render();
    }

    public void refresh() {
        clearContents();
        setup();
        render();
        renderComponents();
    }

    public void setContents(List<PagedContent> contents) {
        this.contents = contents == null ? new ArrayList<>() : contents;
        clampCurrentPage();
        render();
    }

    public void addContent(PagedContent content) {
        if (content != null) {
            this.contents.add(content);
        }
    }

    public void addContent(ItemStack item, boolean cancelClick, Consumer<InventoryClickEvent> consumer) {
        this.contents.add(new PagedContent(item).event(event -> {
            event.setCancelled(cancelClick);
            if (consumer != null) {
                consumer.accept(event);
            }
        }));
    }

    public void setSlot(int startSlot, int endSlot) {
        setSlot(startSlot, endSlot, 0);
    }

    public void setSlot(int startSlot, int endSlot, int spacing) {
        if (startSlot > endSlot) {
            throw new IllegalArgumentException("startSlot must be <= endSlot");
        }

        this.itemSlots.clear();

        int startRow = startSlot / 9;
        int startCol = startSlot % 9;
        int endRow = endSlot / 9;
        int endCol = endSlot % 9;
        int position = 0;

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                int slot = row * 9 + col;
                if (slot >= getSize()) {
                    continue;
                }
                if (slot <= endSlot) {
                    if (spacing <= 0 || position % (spacing + 1) == 0) {
                        itemSlots.add(slot);
                    }
                    position++;
                }
            }
        }

        getState()
                .startSlot(startSlot)
                .endSlot(endSlot)
                .spacing(spacing)
                .itemsPerPage(itemSlots.size());
        clampCurrentPage();
    }

    public void setNavigationButton(PageButtonType type, int slot, ItemStack item) {
        buttonSlots.put(type, slot);
        buttonItems.put(type, item);
        setItem(slot, item).setOnClick(event -> {
            event.setCancelled(true);
            handleButtonClick(type);
        });
    }

    public void clearContents() {
        this.contents.clear();
    }

    private void handleButtonClick(PageButtonType type) {
        switch (type) {
            case FIRST:
                setCurrentPage(0);
                break;
            case PREVIOUS:
                setCurrentPage(Math.max(0, getCurrentPage() - 1));
                break;
            case NEXT:
                setCurrentPage(Math.min(getMaxPage(), getCurrentPage() + 1));
                break;
            case LAST:
                setCurrentPage(getMaxPage());
                break;
            default:
                break;
        }
        refresh();
    }

    public void render() {
        int itemsPerPage = getState().getItemsPerPage();
        for (int i = 0; i < itemSlots.size(); i++) {
            int slot = itemSlots.get(i);
            int index = i + getCurrentPage() * itemsPerPage;
            if (index >= contents.size()) {
                removeItem(slot);
                continue;
            }

            PagedContent content = contents.get(index);
            setItem(slot, content.getItem()).setOnClick(content.getEvent());
        }

        for (Map.Entry<PageButtonType, Integer> entry : buttonSlots.entrySet()) {
            PageButtonType type = entry.getKey();
            int slot = entry.getValue();
            ItemStack item = buttonItems.get(type);

            boolean shouldShow = false;
            switch (type) {
                case PREVIOUS:
                case FIRST:
                    shouldShow = getCurrentPage() > 0;
                case NEXT:
                case LAST:
                    shouldShow = getCurrentPage() < getMaxPage();
            }

            if (item != null && shouldShow) {
                setItem(slot, item).setOnClick(e -> {
                    e.setCancelled(true);
                    handleButtonClick(type);
                });
            } else {
                removeItem(slot);
            }
        }

        if (buttonSlots.containsKey(PageButtonType.INFO)) {
            int slot = buttonSlots.get(PageButtonType.INFO);
            ItemStack item = buttonItems.get(PageButtonType.INFO);
            if (item != null && item.getType() != Material.AIR) {
                ItemStack display = item.clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(String.format("&ePage %d/%d", getCurrentPage() + 1, getMaxPage() + 1)));
                    display.setItemMeta(meta);
                }
                setItem(slot, display).setOnClick(event -> event.setCancelled(true));
            }
        }
    }

    public int getMaxPage() {
        int itemsPerPage = getState().getItemsPerPage();
        if (itemsPerPage <= 0) {
            return 0;
        }
        return Math.max(0, (contents.size() - 1) / itemsPerPage);
    }

    public int getCurrentPage() {
        return getState().getCurrentPage();
    }

    public void setCurrentPage(int currentPage) {
        getState().currentPage(Math.min(Math.max(0, currentPage), getMaxPage()));
    }

    private void clampCurrentPage() {
        setCurrentPage(getCurrentPage());
    }

    @Getter
    public static class PagedContent {
        private final ItemStack item;
        private Consumer<InventoryClickEvent> event;

        public PagedContent(ItemStack item) {
            this.item = item;
        }

        public PagedContent event(Consumer<InventoryClickEvent> event) {
            this.event = event;
            return this;
        }

        public PagedContent setEvent(Consumer<InventoryClickEvent> event) {
            return event(event);
        }

    }

    public enum PageButtonType {
        FIRST,
        PREVIOUS,
        INFO,
        NEXT,
        LAST
    }
}

