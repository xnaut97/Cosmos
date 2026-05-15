package com.github.xnaut97.cosmos.menu.template;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.type.PaginationMenu;
import com.github.xnaut97.cosmos.utilities.ItemCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Getter(AccessLevel.PROTECTED)
@Setter
@Accessors(fluent = true)
public abstract class SelectorMenu<T, P extends Plugin> extends PaginationMenu {

    private final Menu menu;
    private BiPredicate<ClickType, T> onClick;
    private Predicate<T> onCheck;
    private Predicate<T> onValidate;
    private Predicate<T> onFilter;

    public SelectorMenu(P plugin, int rows, String title, Menu menu) {
        super(plugin, rows, title);
        this.menu = menu;

        setSlot(10, 43);
        setupExitButton();
    }

    private void setupExitButton() {
        setItem(45, new ItemCreator(XMaterial.OAK_DOOR.get())
                .setDisplayName("&eReturn")
                .build())
                .setOnClick(event -> {
                    event.setCancelled(true);

                    if (menu == null)
                        getPlayer().closeInventory();
                    else
                        menu.open(getPlayer());
                });
    }

    @Override
    protected void setup() {
        listContents();
    }

    private void listContents() {
        Stream<T> stream = getObjects().stream();
        if(onFilter != null)
            stream = stream.filter(onFilter);

        stream.forEach(obj -> {
            ItemCreator creator = getItem(obj);
            boolean nullBefore = creator == null;
            if (creator == null) {
                creator = new ItemCreator(XMaterial.BARRIER.get())
                        .setDisplayName("&cUnidentified")
                        .setLore("&7Object " + obj);
            }

            addContent(creator.build(), true, event -> {
                if (nullBefore) return;
                event.setCancelled(true);

                if (onValidate != null && !onValidate.test(obj))
                    return;

                if (onClick.test(event.getClick(), obj))
                    refresh();
            });
        });
    }

    protected abstract List<T> getObjects();

    protected abstract ItemCreator getItem(T obj);

}
