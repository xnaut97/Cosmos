package com.github.xnaut97.cosmos.menu;

import com.github.xnaut97.cosmos.menu.type.PaginationMenu;
import com.github.xnaut97.cosmos.utilities.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class MenuPreviewHub extends PaginationMenu {

    private final MenuRegistry registry;

    public MenuPreviewHub(Plugin plugin, MenuRegistry registry) {
        super(plugin, 6, "&8Menu Preview");
        this.registry = registry;
    }

    @Override
    protected void setup() {

        Bukkit.getLogger().info("[MenuPreviewHub] setup() called");

        System.out.println("Menu: " + registry.getMenus().size());

        for (MenuRegistry.RegisteredMenu registeredMenu : registry.getMenus()) {

            Bukkit.getLogger().info("[MenuPreviewHub] Adding menu: "
                    + registeredMenu.getName());

            addContent(new PagedContent(createMenuItem(registeredMenu))
                    .event(event -> {
                        event.setCancelled(true);

                        registry.openMenu(
                                registeredMenu.getId(),
                                (Player) event.getWhoClicked(),
                                getPlugin()
                        );
                    }));
        }

        Bukkit.getLogger().info("[MenuPreviewHub] setup() done");
    }

    private ItemStack createMenuItem(MenuRegistry.RegisteredMenu registeredMenu) {
        List<String> lore = new ArrayList<>();
        if (registeredMenu.getDescription() != null && !registeredMenu.getDescription().isEmpty()) {
            lore.add("&7" + registeredMenu.getDescription());
        }
        lore.add("&8ID: " + registeredMenu.getId());
        lore.add("&eClick to open");

        return new ItemCreator(Material.CHEST)
                .setDisplayName("&a" + registeredMenu.getName())
                .setLore(lore)
                .build();
    }
}
