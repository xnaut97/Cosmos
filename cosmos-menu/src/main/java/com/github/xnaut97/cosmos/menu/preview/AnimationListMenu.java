package com.github.xnaut97.cosmos.menu.preview;

import com.github.xnaut97.cosmos.menu.type.PagedMenu;
import com.github.xnaut97.cosmos.utilities.ItemCreator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class AnimationListMenu<P extends Plugin> extends PagedMenu<P> {

    public AnimationListMenu(P plugin) {
        super(plugin, 6, "&8Animation Gallery");
        setSlot(10, 43);
    }

    @Override
    protected void setup() {
        for (AnimationDemo demo : AnimationDemo.values()) {
            addContent(new PagedContent(createButton(demo)).event(event -> {
                event.setCancelled(true);
                new AnimationPreviewMenu<P>(getPlugin(), demo).open(getPlayer());
            }));
        }
    }

    private ItemStack createButton(AnimationDemo demo) {
        return new ItemCreator(demo.getIcon())
                .setDisplayName("&e" + demo.getDisplayName())
                .setLore(
                        "&7" + demo.getDescription(),
                        "&8Type: &f" + demo.name(),
                        " ",
                        "&aClick to preview."
                )
                .build();
    }
}


