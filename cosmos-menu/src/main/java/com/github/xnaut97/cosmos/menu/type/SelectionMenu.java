package com.github.xnaut97.cosmos.menu.type;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SelectionMenu<T> extends PaginationMenu {

    private final List<T> entries = new ArrayList<>();
    private final Set<T> selected = new LinkedHashSet<>();
    private Function<T, ItemStack> itemFactory = value -> createButton(Material.PAPER, String.valueOf(value), null, false);
    private Predicate<T> filter = value -> true;
    private boolean multiSelect;
    private BiConsumer<Player, T> selectHandler = (player, value) -> {};
    private BiConsumer<Player, T> deselectHandler = (player, value) -> {};

    public SelectionMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
    }

    @Override
    protected void setup() {
        rebuildContents();
    }

    public SelectionMenu<T> entries(List<T> entries) {
        this.entries.clear();
        if (entries != null) {
            this.entries.addAll(entries);
        }
        return this;
    }

    public SelectionMenu<T> itemFactory(Function<T, ItemStack> itemFactory) {
        this.itemFactory = itemFactory == null ? this.itemFactory : itemFactory;
        return this;
    }

    public SelectionMenu<T> filter(Predicate<T> filter) {
        this.filter = filter == null ? value -> true : filter;
        return this;
    }

    public SelectionMenu<T> search(String query, BiSearchPredicate<T> searchPredicate) {
        if (searchPredicate == null || query == null || query.isEmpty()) {
            return filter(value -> true);
        }
        return filter(value -> searchPredicate.test(query, value));
    }

    public SelectionMenu<T> multiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        return this;
    }

    public SelectionMenu<T> onSelect(BiConsumer<Player, T> selectHandler) {
        this.selectHandler = selectHandler == null ? (player, value) -> {} : selectHandler;
        return this;
    }

    public SelectionMenu<T> onDeselect(BiConsumer<Player, T> deselectHandler) {
        this.deselectHandler = deselectHandler == null ? (player, value) -> {} : deselectHandler;
        return this;
    }

    public Set<T> getSelected() {
        return Collections.unmodifiableSet(selected);
    }

    public void refreshSelection() {
        refresh();
    }

    private void rebuildContents() {
        clearContents();
        for (T entry : entries) {
            if (!filter.test(entry)) {
                continue;
            }
            addContent(itemFactory.apply(entry), true, event -> toggle((Player) event.getWhoClicked(), entry));
        }
    }

    private void toggle(Player player, T entry) {
        if (selected.contains(entry)) {
            selected.remove(entry);
            deselectHandler.accept(player, entry);
            refresh();
            return;
        }
        if (!multiSelect) {
            selected.clear();
        }
        selected.add(entry);
        getState().getSelectedItems().clear();
        selectHandler.accept(player, entry);
        refresh();
    }

    public interface BiSearchPredicate<T> {
        boolean test(String query, T value);
    }
}
