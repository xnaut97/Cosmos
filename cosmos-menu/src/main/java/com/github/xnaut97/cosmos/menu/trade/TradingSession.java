package com.github.xnaut97.cosmos.menu.trade;

import com.github.xnaut97.cosmos.menu.type.TradingMenu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class TradingSession {

    private final Plugin plugin;
    private final TradingParticipant first;
    private final TradingParticipant second;
    private final TradingMenu firstMenu;
    private final TradingMenu secondMenu;
    private boolean closing;
    private boolean finalized;
    private boolean cleaned;
    private Sound placeSound = Sound.ENTITY_ITEM_PICKUP;
    private Sound takeSound = Sound.ENTITY_ITEM_PICKUP;
    private Sound opponentAcceptedSound = Sound.BLOCK_NOTE_BLOCK_PLING;
    private Sound successSound = Sound.ENTITY_PLAYER_LEVELUP;
    private Sound cancelledSound = Sound.BLOCK_ANVIL_LAND;
    private float soundVolume = 0.8F;
    private float soundPitch = 1.0F;

    public TradingSession(Plugin plugin, Player first, Player second, String title) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Trading players cannot be null");
        }
        if (first.getUniqueId().equals(second.getUniqueId())) {
            throw new IllegalArgumentException("A player cannot trade with themselves");
        }

        this.plugin = plugin;
        this.first = new TradingParticipant(first);
        this.second = new TradingParticipant(second);
        this.firstMenu = new TradingMenu(plugin, this, this.first, this.second, title);
        this.secondMenu = new TradingMenu(plugin, this, this.second, this.first, title);
    }

    public void open() {
        ensureMainThread();
        TradingSession existingFirst = TradingSessionManager.getSession(first.getPlayer());
        TradingSession existingSecond = TradingSessionManager.getSession(second.getPlayer());
        if (existingFirst != null && existingFirst != this) {
            existingFirst.cancel();
        }
        if (existingSecond != null && existingSecond != this && existingSecond != existingFirst) {
            existingSecond.cancel();
        }
        TradingSessionManager.register(this);
        firstMenu.open(first.getPlayer());
        secondMenu.open(second.getPlayer());
    }

    public TradingParticipant getParticipant(UUID uuid) {
        if (first.getUuid().equals(uuid)) {
            return first;
        }
        if (second.getUuid().equals(uuid)) {
            return second;
        }
        return null;
    }

    public TradingParticipant getOther(TradingParticipant participant) {
        return participant == first ? second : first;
    }

    public int getMaxPage(TradingParticipant participant, boolean allowEmptyTail) {
        int size = participant.getItems().size();
        if (size <= 0) {
            return 0;
        }

        int lastItemPage = (size - 1) / TradingMenu.TRADE_SLOTS_PER_PAGE;
        if (allowEmptyTail && size % TradingMenu.TRADE_SLOTS_PER_PAGE == 0) {
            return lastItemPage + 1;
        }
        return lastItemPage;
    }

    public void setPage(TradingParticipant participant, int page, boolean editableSide) {
        if (isEnded()) {
            return;
        }

        participant.page(Math.min(Math.max(0, page), getMaxPage(participant, editableSide)));
        syncMenus();
    }

    public void syncItemsFromMenu(TradingMenu menu) {
        if (isEnded()) {
            return;
        }

        TradingParticipant owner = menu.getSelf();
        if (owner.isAccepted()) {
            menu.renderTradeSlots();
            return;
        }

        boolean changed = applyVisibleItems(owner, menu);
        owner.page(Math.min(owner.getPage(), getMaxPage(owner, true)));
        if (changed) {
            getOther(owner).resetDecision();
        }
        syncMenus();
    }

    public void accept(TradingParticipant participant) {
        if (isEnded()) {
            return;
        }
        if (participant.isAccepted()) {
            return;
        }

        syncItemsFromOpenMenus();
        participant.accepted(true);
        syncMenus();

        if (first.isAccepted() && second.isAccepted()) {
            complete();
        } else {
            TradingParticipant other = getOther(participant);
            playSound(other.getPlayer(), opponentAcceptedSound);
        }
    }

    public void unaccept(TradingParticipant participant) {
        if (isEnded()) {
            return;
        }
        participant.accepted(false);
        syncMenus();
    }

    public boolean placeFromInventory(TradingMenu menu, Inventory sourceInventory, int sourceSlot) {
        if (isEnded() || menu == null || sourceInventory == null) {
            return false;
        }

        TradingParticipant owner = menu.getSelf();
        if (owner.isAccepted()) {
            owner.getPlayer().updateInventory();
            return false;
        }

        ItemStack sourceItem = normalize(sourceInventory.getItem(sourceSlot));
        if (sourceItem == null) {
            return false;
        }

        syncItemsFromMenu(menu);
        ItemStack moving = sourceItem.clone();
        int remaining = moving.getAmount();
        List<ItemStack> items = owner.getItems();
        int placedPage = -1;

        for (int index = 0; index < items.size() && remaining > 0; index++) {
            ItemStack existing = normalize(items.get(index));
            if (existing == null || !existing.isSimilar(moving)) {
                continue;
            }

            int maxStack = Math.min(existing.getMaxStackSize(), owner.getPlayer().getInventory().getMaxStackSize());
            int available = maxStack - existing.getAmount();
            if (available <= 0) {
                continue;
            }

            int amount = Math.min(remaining, available);
            ItemStack updated = existing.clone();
            updated.setAmount(existing.getAmount() + amount);
            items.set(index, updated);
            remaining -= amount;
            placedPage = index / TradingMenu.TRADE_SLOTS_PER_PAGE;
        }

        while (remaining > 0) {
            ItemStack placed = moving.clone();
            int amount = Math.min(remaining, placed.getMaxStackSize());
            placed.setAmount(amount);
            items.add(placed);
            remaining -= amount;
            placedPage = (items.size() - 1) / TradingMenu.TRADE_SLOTS_PER_PAGE;
        }

        if (placedPage >= 0) {
            owner.page(placedPage);
            getOther(owner).resetDecision();
            sourceInventory.clear(sourceSlot);
            owner.getPlayer().updateInventory();
            playSound(owner.getPlayer(), placeSound);
            syncMenus();
            return true;
        }
        return false;
    }

    public void cancel() {
        if (isEnded()) {
            return;
        }

        syncItemsFromOpenMenus();
        closing = true;
        returnItems(first.getPlayer(), first.getItems());
        returnItems(second.getPlayer(), second.getItems());
        clearSessionItems();
        closeMenus();
        playSound(first.getPlayer(), cancelledSound);
        playSound(second.getPlayer(), cancelledSound);
        cleanup();
    }

    public void handleClose(TradingMenu menu) {
        if (!isEnded() && !closing) {
            captureVisibleItems(menu);
            cancel();
        }

    }

    private void complete() {
        if (isEnded()) {
            return;
        }

        syncItemsFromOpenMenus();
        if (!canFit(first.getPlayer().getInventory(), second.getItems())
                || !canFit(second.getPlayer().getInventory(), first.getItems())) {
            first.accepted(false);
            second.accepted(false);
            syncMenus();
            return;
        }

        finalized = true;
        closing = true;
        addItems(first.getPlayer(), second.getItems());
        addItems(second.getPlayer(), first.getItems());
        clearSessionItems();
        closeMenus();
        playSound(first.getPlayer(), successSound);
        playSound(second.getPlayer(), successSound);
        cleanup();
    }

    private boolean applyVisibleItems(TradingParticipant owner, TradingMenu menu) {
        List<ItemStack> items = owner.getItems();
        int startIndex = owner.getPage() * TradingMenu.TRADE_SLOTS_PER_PAGE;
        List<ItemStack> updated = new ArrayList<>(items.size() + TradingMenu.TRADE_SLOTS_PER_PAGE);

        for (int index = 0; index < Math.min(startIndex, items.size()); index++) {
            updated.add(items.get(index));
        }

        for (int index = 0; index < TradingMenu.TRADE_SLOTS_PER_PAGE; index++) {
            ItemStack item = normalize(menu.getInventory().getItem(TradingMenu.SELF_TRADE_SLOTS[index]));
            if (item != null) {
                updated.add(item.clone());
            }
        }

        int suffixStart = Math.min(items.size(), startIndex + TradingMenu.TRADE_SLOTS_PER_PAGE);
        for (int index = suffixStart; index < items.size(); index++) {
            updated.add(items.get(index));
        }

        boolean changed = !itemListEquals(items, updated);
        if (changed) {
            items.clear();
            items.addAll(updated);
        }
        return changed;
    }

    private void syncItemsFromOpenMenus() {
        if (firstMenu.isViewing()) {
            captureVisibleItems(firstMenu);
        }
        if (secondMenu.isViewing()) {
            captureVisibleItems(secondMenu);
        }
    }

    private void captureVisibleItems(TradingMenu menu) {
        TradingParticipant owner = menu.getSelf();
        if (!owner.isAccepted()) {
            applyVisibleItems(owner, menu);
        }
    }

    private void syncMenus() {
        firstMenu.syncFromSession();
        secondMenu.syncFromSession();
    }

    private void closeMenus() {
        firstMenu.renderTradeSlots();
        secondMenu.renderTradeSlots();
        if (firstMenu.isViewing()) {
            first.getPlayer().closeInventory();
        }
        if (secondMenu.isViewing()) {
            second.getPlayer().closeInventory();
        }
    }

    private void cleanup() {
        if (cleaned) {
            return;
        }
        cleaned = true;
        TradingSessionManager.unregister(this);
    }

    private boolean isEnded() {
        return finalized || cleaned;
    }

    private void clearSessionItems() {
        first.getItems().clear();
        second.getItems().clear();
    }

    private boolean canFit(Inventory inventory, List<ItemStack> items) {
        ItemStack[] contents = inventory.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            contents[i] = contents[i] == null ? null : contents[i].clone();
        }
        for (ItemStack item : items) {
            ItemStack moving = item == null ? null : item.clone();
            if (moving == null || moving.getType() == Material.AIR || moving.getAmount() <= 0) {
                continue;
            }

            int remaining = moving.getAmount();
            for (ItemStack content : contents) {
                if (content == null || content.getType() == Material.AIR) {
                    continue;
                }
                if (!content.isSimilar(moving)) {
                    continue;
                }

                int max = Math.min(content.getMaxStackSize(), inventory.getMaxStackSize());
                int move = Math.min(remaining, max - content.getAmount());
                if (move > 0) {
                    content.setAmount(content.getAmount() + move);
                    remaining -= move;
                }
                if (remaining <= 0) {
                    break;
                }
            }

            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack content = contents[i];
                if (content != null && content.getType() != Material.AIR) {
                    continue;
                }

                ItemStack placed = moving.clone();
                int move = Math.min(remaining, placed.getMaxStackSize());
                placed.setAmount(move);
                contents[i] = placed;
                remaining -= move;
            }

            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private void addItems(Player player, List<ItemStack> items) {
        List<ItemStack> clones = cloneItems(items);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(clones.toArray(new ItemStack[clones.size()]));
        dropLeftover(player, leftover);
        player.updateInventory();
    }

    private void returnItems(Player player, List<ItemStack> items) {
        addItems(player, items);
    }

    private List<ItemStack> cloneItems(List<ItemStack> items) {
        List<ItemStack> clones = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                clones.add(item.clone());
            }
        }
        return clones;
    }

    private void dropLeftover(Player player, Map<Integer, ItemStack> leftover) {
        if (leftover == null || leftover.isEmpty()) {
            return;
        }
        for (ItemStack item : leftover.values()) {
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    private ItemStack normalize(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0 ? null : item;
    }

    private boolean itemEquals(ItemStack first, ItemStack second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.isSimilar(second) && first.getAmount() == second.getAmount();
    }

    private boolean itemListEquals(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int index = 0; index < first.size(); index++) {
            if (!itemEquals(first.get(index), second.get(index))) {
                return false;
            }
        }
        return true;
    }

    public void playPlaceSound(Player player) {
        playSound(player, placeSound);
    }

    public void playTakeSound(Player player) {
        playSound(player, takeSound);
    }

    public TradingSession placeSound(Sound sound) {
        this.placeSound = sound;
        return this;
    }

    public TradingSession takeSound(Sound sound) {
        this.takeSound = sound;
        return this;
    }

    public TradingSession opponentAcceptedSound(Sound sound) {
        this.opponentAcceptedSound = sound;
        return this;
    }

    public TradingSession successSound(Sound sound) {
        this.successSound = sound;
        return this;
    }

    public TradingSession cancelledSound(Sound sound) {
        this.cancelledSound = sound;
        return this;
    }

    public TradingSession soundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
        return this;
    }

    public TradingSession soundPitch(float soundPitch) {
        this.soundPitch = soundPitch;
        return this;
    }

    private void playSound(Player player, Sound sound) {
        if (player != null && sound != null) {
            player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
        }
    }

    private void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Trading sessions must be opened on the Bukkit main thread");
        }
    }

    public void shutdown() {
        if (isEnded()) {
            return;
        }

        finalized = true;
        closing = true;

        syncItemsFromOpenMenus();

        returnItems(first.getPlayer(), first.getItems());
        returnItems(second.getPlayer(), second.getItems());

        clearSessionItems();

        cleanup();
    }
}
