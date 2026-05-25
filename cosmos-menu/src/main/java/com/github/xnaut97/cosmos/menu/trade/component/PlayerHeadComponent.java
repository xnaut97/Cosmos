package com.github.xnaut97.cosmos.menu.trade.component;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import com.github.xnaut97.cosmos.utilities.item.ItemCreator;
import com.github.xnaut97.cosmos.utilities.SkullProfileService;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class PlayerHeadComponent implements MenuComponent {

    private final int slot;
    private final Player player;
    private final String displayName;

    @Override
    public Collection<Integer> getOwnedSlots() {
        return Collections.singleton(slot);
    }

    @Override
    public void render(Menu menu) {
        menu.renderSlot(slot, playerHead());
    }

    @Override
    public void onClick(Menu menu, InventoryClickEvent event) {
        event.setCancelled(true);
    }

    private ItemStack playerHead() {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (head == null) {
            head = new ItemStack(Material.PLAYER_HEAD);
        }

        GameProfile cachedProfile = SkullProfileService.getCachedProfile(player);
        ItemCreator creator = new ItemCreator(head);
        if (cachedProfile != null) {
            creator.setTexture(cachedProfile);
        }
        return creator.setDisplayName(displayName).build();
    }
}
