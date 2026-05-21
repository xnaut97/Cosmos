package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.trade.TradingParticipant;
import com.github.xnaut97.cosmos.menu.trade.TradingSession;
import com.github.xnaut97.cosmos.menu.trade.component.DividerComponent;
import com.github.xnaut97.cosmos.menu.trade.component.PlayerHeadComponent;
import com.github.xnaut97.cosmos.menu.trade.component.TradeControlComponent;
import com.github.xnaut97.cosmos.menu.trade.component.TradeGridComponent;
import com.github.xnaut97.cosmos.menu.trade.component.TradePageComponent;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

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

    private static final int SELF_HEAD_SLOT = 0;
    private static final int TARGET_HEAD_SLOT = 8;
    private static final int CANCEL_SLOT = 45;
    private static final int ACTION_SLOT = 48;
    private static final int SELF_PREVIOUS_SLOT = 46;
    private static final int SELF_NEXT_SLOT = 47;
    private static final int TARGET_PREVIOUS_SLOT = 51;
    private static final int TARGET_NEXT_SLOT = 52;
    private static final int TARGET_ACCEPTED_SLOT = 53;
    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40, 49};

    private final TradingSession session;
    private final TradingParticipant self;
    private final TradingParticipant target;
    private TradeGridComponent selfGrid;
    private TradeGridComponent targetGrid;
    private int targetPage;
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
        selfGrid = new TradeGridComponent(session, self, SELF_TRADE_SLOTS, self::getPage, true);
        targetGrid = new TradeGridComponent(session, target, TARGET_TRADE_SLOTS, this::getTargetPage, false);

        addComponent(new PlayerHeadComponent(SELF_HEAD_SLOT, self.getPlayer(), "&a" + self.getPlayer().getName()));
        addComponent(new PlayerHeadComponent(TARGET_HEAD_SLOT, target.getPlayer(), "&c" + target.getPlayer().getName()));
        addComponent(new DividerComponent(DIVIDER_SLOTS));
        addComponent(selfGrid);
        addComponent(targetGrid);
        addComponent(new TradeControlComponent(session, self, target, CANCEL_SLOT, ACTION_SLOT, TARGET_ACCEPTED_SLOT));
        addComponent(new TradePageComponent(session, self, SELF_PREVIOUS_SLOT, self::getPage,
                page -> session.setPage(self, page, true), false, true));
        addComponent(new TradePageComponent(session, self, SELF_NEXT_SLOT, self::getPage,
                page -> session.setPage(self, page, true), true, true));
        addComponent(new TradePageComponent(session, target, TARGET_PREVIOUS_SLOT, this::getTargetPage,
                this::setTargetPage, false, false));
        addComponent(new TradePageComponent(session, target, TARGET_NEXT_SLOT, this::getTargetPage,
                this::setTargetPage, true, false));
    }

    public void syncFromSession() {
        targetPage = Math.min(targetPage, session.getMaxPage(target, false));
        renderComponents();
    }

    public void renderTradeSlots() {
        if (selfGrid != null) {
            selfGrid.render(this);
        }
        if (targetGrid != null) {
            targetGrid.render(this);
        }
    }

    public void setTargetPage(int page) {
        targetPage = Math.min(Math.max(0, page), session.getMaxPage(target, false));
        renderComponents();
    }

    @Override
    protected boolean handleExternalShiftClickIntoMenu(InventoryClickEvent event) {
        return selfGrid != null && selfGrid.handleExternalShiftClick(this, event);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        session.handleClose(this);
    }
}
