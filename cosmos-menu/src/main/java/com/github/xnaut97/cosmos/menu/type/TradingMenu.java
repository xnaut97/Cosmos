package com.github.xnaut97.cosmos.menu.type;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.trade.TradingParticipant;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import com.github.xnaut97.cosmos.utilities.ItemCreator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Getter
public class TradingMenu extends Menu {

    public static final int[] SELF_TRADE_SLOTS = {
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30,
            36, 37, 38, 39
    };
    public static final int[] TARGET_TRADE_SLOTS = {
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35,
            41, 42, 43, 44
    };
    public static final int TRADE_SLOTS_PER_PAGE = SELF_TRADE_SLOTS.length;

    private static final int CANCEL_SLOT = 45;
    private static final int ACTION_SLOT = 48;
    private static final int SELF_PREVIOUS_SLOT = 1;
    private static final int SELF_INFO_SLOT = 2;
    private static final int SELF_NEXT_SLOT = 3;
    private static final int TARGET_INFO_SLOT = 6;
    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40, 49};

    private final TradingSession session;
    private final TradingParticipant self;
    private final TradingParticipant target;
    private boolean initialized;

    public TradingMenu(Plugin plugin, TradingSession session, TradingParticipant self, TradingParticipant target, String title) {
        super(plugin, 6, title == null ? "Trading" : title);
        this.session = session;
        this.self = self;
        this.target = target;
    }

    @Override
    protected void setup() {
        if (initialized) {
            return;
        }

        initialized = true;
        for (int slot : SELF_TRADE_SLOTS) {
            allowItemInput(slot);
        }
        canPlace((slot, item) -> isSelfTradeSlot(slot) && !self.isConfirmed());
        canTake(slot -> isSelfTradeSlot(slot) && !self.isConfirmed());
        canDragInto(slot -> isSelfTradeSlot(slot) && !self.isConfirmed());
        syncFromSession();
    }

    public void syncFromSession() {
        renderStaticSlots();
        renderTradeSlots();
        renderControls();
    }

    public void renderTradeSlots() {
        renderItemPage(self, SELF_TRADE_SLOTS, self.getPage());
        renderItemPage(target, TARGET_TRADE_SLOTS, target.getPage());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        if (rawSlot == CANCEL_SLOT) {
            event.setCancelled(true);
            session.cancel();
            return;
        }
        if (rawSlot == ACTION_SLOT) {
            event.setCancelled(true);
            if (self.isConfirmed()) {
                session.accept(self);
            } else {
                session.confirm(self);
            }
            return;
        }
        if (rawSlot == SELF_PREVIOUS_SLOT) {
            event.setCancelled(true);
            session.setPage(self, self.getPage() - 1);
            return;
        }
        if (rawSlot == SELF_NEXT_SLOT) {
            event.setCancelled(true);
            session.setPage(self, self.getPage() + 1);
            return;
        }
        if (isTargetTradeSlot(rawSlot) || isDivider(rawSlot) || isStaticControl(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        super.onClick(event);
    }

    @Override
    protected void afterAllowedClick(InventoryClickEvent event, int rawSlot) {
        scheduleSessionSync();
    }

    @Override
    protected void afterShiftClickPlaced(java.util.Collection<Integer> changedSlots) {
        scheduleSessionSync();
    }

    @Override
    protected void afterAllowedDrag(InventoryDragEvent event) {
        scheduleSessionSync();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        session.handleClose(this);
    }

    private void scheduleSessionSync() {
        Bukkit.getScheduler().runTask(getPlugin(), () -> session.syncItemsFromMenu(this));
    }

    private void renderItemPage(TradingParticipant participant, int[] slots, int page) {
        List<ItemStack> items = participant.getItems();
        int startIndex = page * TRADE_SLOTS_PER_PAGE;
        for (int i = 0; i < slots.length; i++) {
            int itemIndex = startIndex + i;
            ItemStack item = itemIndex < items.size()
                    ? items.get(itemIndex).clone()
                    : null;

            renderSlot(slots[i], item);
        }
    }

    private void renderStaticSlots() {
        renderSlot(0, playerHead(self.getPlayer(), "&a" + self.getPlayer().getName()));
        renderSlot(8, playerHead(target.getPlayer(), "&c" + target.getPlayer().getName()));
        ItemStack divider = new ItemCreator(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("&8")
                .setAmount(1)
                .build();
        for (int slot : DIVIDER_SLOTS) {
            renderSlot(slot, divider);
        }
    }

    private void renderControls() {
        renderSlot(CANCEL_SLOT, new ItemCreator(Material.BARRIER)
                .setDisplayName("&cCancel")
                .setLore("&7Cancel this trade and return all items.")
                .build());
        renderSlot(ACTION_SLOT, actionItem());
        renderSlot(SELF_PREVIOUS_SLOT, self.getPage() > 0 ? pageItem("&ePrevious Page") : null);
        renderSlot(SELF_INFO_SLOT, pageInfo(self, true));
        renderSlot(SELF_NEXT_SLOT, self.getPage() < session.getMaxPage(self, true) ? pageItem("&eNext Page") : null);
        renderSlot(TARGET_INFO_SLOT, pageInfo(target, false));
    }

    private ItemStack actionItem() {
        if (self.isAccepted()) {
            return new ItemCreator(Material.EMERALD_BLOCK)
                    .setDisplayName("&aAccepted")
                    .setLore("&7Waiting for " + target.getPlayer().getName() + ".")
                    .setGlow(true)
                    .build();
        }
        if (self.isConfirmed()) {
            return new ItemCreator(Material.EMERALD)
                    .setDisplayName("&aAccept")
                    .setLore("&7Accept the current trade.")
                    .build();
        }
        return new ItemCreator(Material.LIME_DYE)
                .setDisplayName("&eConfirm")
                .setLore("&7Lock your offered items.")
                .setAmount(10)
                .build();
    }

    private ItemStack pageItem(String name) {
        return new ItemCreator(Material.ARROW)
                .setDisplayName(name)
                .build();
    }

    private ItemStack pageInfo(TradingParticipant participant, boolean editableSide) {
        int maxPage = session.getMaxPage(participant, editableSide);
        return new ItemCreator(Material.PAPER)
                .setDisplayName("&ePage " + (participant.getPage() + 1) + "/" + (maxPage + 1))
                .build();
    }

    private ItemStack playerHead(Player player, String name) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (head == null) {
            head = new ItemStack(Material.PLAYER_HEAD);
        }
        return new ItemCreator(head)
                .setTexture(player)
                .setDisplayName(name)
                .build();
    }

    private boolean isSelfTradeSlot(int slot) {
        return contains(SELF_TRADE_SLOTS, slot);
    }

    private boolean isTargetTradeSlot(int slot) {
        return contains(TARGET_TRADE_SLOTS, slot);
    }

    private boolean isDivider(int slot) {
        return contains(DIVIDER_SLOTS, slot);
    }

    private boolean isStaticControl(int slot) {
        return slot == 0 || slot == 8 || slot == SELF_INFO_SLOT || slot == TARGET_INFO_SLOT
                || slot == 5 || slot == 7 || slot == 46 || slot == 47 || slot == 50
                || slot == 51 || slot == 52 || slot == 53;
    }

    private boolean contains(int[] slots, int slot) {
        for (int value : slots) {
            if (value == slot) {
                return true;
            }
        }
        return false;
    }
}
