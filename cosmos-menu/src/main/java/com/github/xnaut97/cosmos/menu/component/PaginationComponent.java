package com.github.xnaut97.cosmos.menu.component;

import com.github.xnaut97.cosmos.menu.Menu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class PaginationComponent implements MenuComponent {

    public enum ButtonType {
        FIRST,
        PREVIOUS,
        INFO,
        NEXT,
        LAST
    }

    private final Map<ButtonType, Integer> slots = new EnumMap<>(ButtonType.class);
    private IntSupplier currentPage = () -> 0;
    private IntSupplier maxPage = () -> 0;
    private IntConsumer pageUpdater = page -> {
    };
    private Function<ButtonType, ItemStack> itemFactory = this::defaultItem;

    public PaginationComponent slot(ButtonType type, int slot) {
        slots.put(type, slot);
        return this;
    }

    public PaginationComponent pages(IntSupplier currentPage, IntSupplier maxPage, IntConsumer pageUpdater) {
        this.currentPage = currentPage == null ? () -> 0 : currentPage;
        this.maxPage = maxPage == null ? () -> 0 : maxPage;
        this.pageUpdater = pageUpdater == null ? page -> {
        } : pageUpdater;
        return this;
    }

    public PaginationComponent itemFactory(Function<ButtonType, ItemStack> itemFactory) {
        this.itemFactory = itemFactory == null ? this::defaultItem : itemFactory;
        return this;
    }

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Collections.unmodifiableCollection(slots.values());
    }

    @Override
    public void render(Menu<?> menu) {
        int page = Math.max(0, currentPage.getAsInt());
        int max = Math.max(0, maxPage.getAsInt());

        for (Map.Entry<ButtonType, Integer> entry : slots.entrySet()) {
            ButtonType type = entry.getKey();
            int slot = entry.getValue();
            boolean visible = shouldShow(type, page, max);

            if (!visible) {
                menu.renderSlot(slot, null);
                continue;
            }

            menu.renderSlot(slot, itemFactory.apply(type));
        }
    }

    @Override
    public void onClick(Menu<?> menu, org.bukkit.event.inventory.InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        ButtonType clickedType = null;
        for (Map.Entry<ButtonType, Integer> entry : slots.entrySet()) {
            if (entry.getValue() == rawSlot) {
                clickedType = entry.getKey();
                break;
            }
        }

        if (clickedType == null) {
            return;
        }

        event.setCancelled(true);
        int page = Math.max(0, currentPage.getAsInt());
        int max = Math.max(0, maxPage.getAsInt());
        if (!shouldShow(clickedType, page, max)) {
            return;
        }

        pageUpdater.accept(nextPage(clickedType, page, max));
        menu.renderComponents();
    }

    private boolean shouldShow(ButtonType type, int page, int max) {
        switch (type) {
            case FIRST:
            case PREVIOUS:
                return page > 0;
            case NEXT: case LAST:
                return page < max;
            default:
                return true;
        }
    }

    private int nextPage(ButtonType type, int page, int max) {
        switch (type) {
            case FIRST: return 0;
            case PREVIOUS: return Math.max(0, page - 1);
            case NEXT: return Math.min(max, page + 1);
            case LAST: return max;
            case INFO: return page;
            default:
                return -1;
        }
    }

    private ItemStack defaultItem(ButtonType type) {
        Material material = type == ButtonType.INFO ? Material.PAPER : Material.ARROW;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color("&e" + type.name()));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String color(String value) {
        return value == null ? null : value.replace("&", "§");
    }
}
