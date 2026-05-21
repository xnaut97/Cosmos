package com.github.xnaut97.cosmos.menu.trade.component;

import com.cryptomorin.xseries.XEnchantment;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import com.github.xnaut97.cosmos.menu.trade.TradingParticipant;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import com.github.xnaut97.cosmos.menu.type.TradingMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;

@RequiredArgsConstructor
public class TradeGridComponent implements MenuComponent {

    private final TradingSession session;
    private final TradingParticipant participant;
    private final int[] slots;
    private final IntSupplier pageSupplier;
    private final boolean editable;

    @Override
    public Collection<Integer> getOwnedSlots() {
        List<Integer> ownedSlots = new ArrayList<>(slots.length);
        for (int slot : slots) {
            ownedSlots.add(slot);
        }
        return ownedSlots;
    }

    @Override
    public void render(Menu menu) {
        List<ItemStack> items = participant.getItems();
        int startIndex = pageSupplier.getAsInt() * TradingMenu.TRADE_SLOTS_PER_PAGE;
        for (int index = 0; index < slots.length; index++) {
            int itemIndex = startIndex + index;
            ItemStack item = itemIndex < items.size() ? items.get(itemIndex).clone() : null;
            if (item != null && participant.isAccepted()) {
                applyAcceptedGlow(item);
            }
            menu.renderSlot(slots[index], item);
        }
    }

    @Override
    public void onClick(Menu menu, InventoryClickEvent event) {
        if (!editable || event.isCancelled()) {
            return;
        }
        playMutationSound(event);
        scheduleSessionSync(menu);
    }

    @Override
    public void onDrag(Menu menu, InventoryDragEvent event) {
        if (!editable || event.isCancelled()) {
            return;
        }
        session.playPlaceSound(participant.getPlayer());
        scheduleSessionSync(menu);
    }

    @Override
    public boolean canDragInto(Menu menu, int slot) {
        return editable && canModify(slot);
    }

    @Override
    public boolean canPlace(Menu menu, int slot, ItemStack item) {
        return editable && canModify(slot);
    }

    @Override
    public boolean canTake(Menu menu, int slot) {
        return editable && canModify(slot);
    }

    public boolean handleExternalShiftClick(TradingMenu menu, InventoryClickEvent event) {
        event.setCancelled(true);
        if (!editable || participant.isAccepted()) {
            participant.getPlayer().updateInventory();
            return true;
        }

        Inventory clickedInventory = event.getClickedInventory();
        int sourceSlot = event.getSlot();
        Bukkit.getScheduler().runTask(menu.getPlugin(), () -> session.placeFromInventory(menu, clickedInventory, sourceSlot));
        return true;
    }

    private boolean canModify(int slot) {
        if (participant.isAccepted()) {
            return false;
        }
        for (int value : slots) {
            if (value == slot) {
                return true;
            }
        }
        return false;
    }

    private void scheduleSessionSync(Menu menu) {
        Bukkit.getScheduler().runTask(menu.getPlugin(), () -> session.syncItemsFromMenu((TradingMenu) menu));
    }

    private void playMutationSound(InventoryClickEvent event) {
        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
                session.playTakeSound(participant.getPlayer());
                break;
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                session.playPlaceSound(participant.getPlayer());
                break;
            case SWAP_WITH_CURSOR:
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                session.playTakeSound(participant.getPlayer());
                session.playPlaceSound(participant.getPlayer());
                break;
            default:
                break;
        }
    }

    private void applyAcceptedGlow(ItemStack item) {
        if (item.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.addEnchant(Objects.requireNonNull(XEnchantment.UNBREAKING.get()), 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}
