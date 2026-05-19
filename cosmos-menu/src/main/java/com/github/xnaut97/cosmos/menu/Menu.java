package com.github.xnaut97.cosmos.menu;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.animation.Animation;
import com.github.xnaut97.cosmos.menu.animation.MenuAnimator;
import com.github.xnaut97.cosmos.menu.component.MenuComponent;
import com.github.xnaut97.cosmos.menu.component.SlotComponent;
import com.github.xnaut97.cosmos.menu.component.StaticItemComponent;
import com.github.xnaut97.cosmos.menu.state.MenuState;
import com.github.xnaut97.cosmos.menu.state.TransactionState;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

@Getter(AccessLevel.PROTECTED)
public abstract class Menu implements InventoryHolder {

    private final UUID uuid = UUID.randomUUID();
    private final Plugin plugin;
    private final int size;
    private final String title;
    private final Inventory inventory;

    private final Map<Integer, ItemStack> previousRenderMap = new HashMap<>();
    private final List<MenuComponent> components = new ArrayList<>();
    private final Map<Integer, MenuComponent> componentSlots = new HashMap<>();
    private final Map<Integer, SlotComponent> staticSlotComponents = new HashMap<>();

    private final Set<Integer> placeableSlots = new HashSet<>();
    private final Set<Integer> takeableSlots = new HashSet<>();
    private final Set<Integer> draggableSlots = new HashSet<>();

    private final MenuAnimator animator = new MenuAnimator(this);

    private IntPredicate customCanDragInto;
    private BiPredicate<Integer, ItemStack> customCanPlace;
    private IntPredicate customCanTake;

    private MenuState state = new MenuState();
    private Player player;

    public Menu(Plugin plugin, int rows, String title) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.size = 9 * Math.max(0, Math.min(6, rows));
        this.title = title;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public int getSize() {
        return size;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void registerListeners(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClose(InventoryCloseEvent event) {
                Menu menu = menuFrom(event.getInventory());
                if (menu != null) {
                    menu.onClose(event);
                }
            }

            @EventHandler
            public void onOpen(InventoryOpenEvent event) {
                Menu menu = menuFrom(event.getInventory());
                if (menu != null) {
                    menu.onOpen(event);
                }
            }

            @EventHandler
            public void onClick(InventoryClickEvent event) {
                Inventory topInventory = event.getView().getTopInventory();
                Menu menu = menuFrom(topInventory);
                if (menu != null) {
                    menu.onClick(event);
                }
            }

            @EventHandler
            public void onDrag(InventoryDragEvent event) {
                Inventory topInventory = event.getView().getTopInventory();
                Menu menu = menuFrom(topInventory);
                if (menu != null) {
                    menu.onDrag(event);
                }
            }
        }, plugin);
    }

    private static Menu menuFrom(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        InventoryHolder holder = inventory.getHolder();
        if(holder instanceof Menu)
            return (Menu) holder;

        return null;
    }

    protected abstract void setup();

    public void open(Player player) {
        ensureMainThread();
        this.player = Objects.requireNonNull(player, "player");

        setup();
        renderComponents();
        player.openInventory(getInventory());
    }

    public void onOpen(InventoryOpenEvent event) {
        this.player = (Player) event.getPlayer();
        this.player.setMetadata("menu_instance", new FixedMetadataValue(plugin, this));

        if (state.isLoading()) {
            renderLoadingPlaceholder();
        }

        animator.startAll();
    }

    public void onClose(InventoryCloseEvent event) {
        Player closingPlayer = (Player) event.getPlayer();
        for (MenuComponent component : new ArrayList<>(components)) {
            component.onClose(this, event);
        }
        closingPlayer.removeMetadata("menu_instance", plugin);
        animator.stopRunningTasks();
    }

    public void onClick(InventoryClickEvent event) {
        if (isLocked()) {
            event.setCancelled(true);
            onLockedClickDenied(event);
            return;
        }

        if (state.isLoading()) {
            event.setCancelled(true);
            return;
        }

        boolean readonly = state.isReadonly();
        if (readonly) {
            event.setCancelled(true);
        }

        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        if (!readonly && handleShiftClickIntoMenu(event)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (!isMenuSlot(rawSlot)) {
            return;
        }

        boolean allowedMutation = !readonly && isDirectMutationAllowed(event, rawSlot);
        if (!allowedMutation) {
            event.setCancelled(true);
        }

        MenuComponent component = componentSlots.get(rawSlot);
        if (component != null) {
            component.onClick(this, event);
        }

        if (readonly) {
            event.setCancelled(true);
        }

        if (!event.isCancelled() && allowedMutation) {
            scheduleRenderedSlotCapture(rawSlot);
            afterAllowedClick(event, rawSlot);
        }
    }

    public void onDrag(InventoryDragEvent event) {
        if (isLocked()) {
            event.setCancelled(true);
            return;
        }

        if (state.isLoading()) {
            event.setCancelled(true);
            return;
        }

        if (state.isReadonly()) {
            event.setCancelled(true);
            return;
        }

        boolean touchesMenu = false;
        for (Integer rawSlot : event.getRawSlots()) {
            if (!isMenuSlot(rawSlot)) {
                continue;
            }

            touchesMenu = true;
            ItemStack item = event.getNewItems().get(rawSlot);
            if (!canDragInto(rawSlot) || !canPlace(rawSlot, item)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!touchesMenu) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Integer rawSlot : event.getRawSlots()) {
                if (isMenuSlot(rawSlot)) {
                    captureRenderedSlot(rawSlot);
                }
            }
        });

        routeDrag(event);
        afterAllowedDrag(event);
    }

    public SlotComponent setItem(int slot, ItemStack item) {
        validateSlot(slot);

        SlotComponent component = new StaticItemComponent(slot, item == null ? new ItemStack(Material.AIR) : item);
        replaceStaticSlotComponent(slot, component);
        return component;
    }

    public boolean renderSlot(int slot, ItemStack item) {
        ensureMainThread();
        validateSlot(slot);

        ItemStack normalized = normalize(item);
        ItemStack previous = previousRenderMap.get(slot);
        ItemStack current = normalize(inventory.getItem(slot));

        if (renderEquals(previous, normalized) && renderEquals(current, normalized)) {
            return false;
        }

        if (isAir(normalized)) {
            inventory.clear(slot);
            previousRenderMap.remove(slot);
            return true;
        }

        inventory.setItem(slot, normalized);
        previousRenderMap.put(slot, normalized.clone());
        return true;
    }

    public void removeItem(int slot) {
        validateSlot(slot);
        renderSlot(slot, null);
        removeStaticSlotComponent(slot);
    }

    public void clearItems() {
        ensureMainThread();
        inventory.clear();
        removeStaticSlotComponents();
        previousRenderMap.clear();
    }

    public Menu addComponent(MenuComponent component) {
        Objects.requireNonNull(component, "component");
        components.add(component);
        component.onAttach(this);
        registerComponentSlots(component);

        if (isViewing()) {
            component.render(this);
            component.onRender(this);
        }
        return this;
    }

    public Menu removeComponent(MenuComponent component) {
        if (component == null) {
            return this;
        }

        if (components.remove(component)) {
            unregisterComponentSlots(component);
            component.onDetach(this);
        }
        return this;
    }

    public void renderComponents() {
        rebuildComponentLookup();
        for (MenuComponent component : new ArrayList<>(components)) {
            component.render(this);
            component.onRender(this);
        }
    }

    public void updateComponents() {
        for (MenuComponent component : new ArrayList<>(components)) {
            component.onUpdate(this);
        }
    }

    public Menu allowItemInput(int slot) {
        allowPlace(slot);
        allowTake(slot);
        allowDrag(slot);
        return this;
    }

    public Menu denyItemInput(int slot) {
        placeableSlots.remove(slot);
        takeableSlots.remove(slot);
        draggableSlots.remove(slot);
        return this;
    }

    public Menu allowPlace(int slot) {
        validateSlot(slot);
        placeableSlots.add(slot);
        return this;
    }

    public Menu allowTake(int slot) {
        validateSlot(slot);
        takeableSlots.add(slot);
        return this;
    }

    public Menu allowDrag(int slot) {
        validateSlot(slot);
        draggableSlots.add(slot);
        return this;
    }

    public Menu canDragInto(IntPredicate predicate) {
        this.customCanDragInto = predicate;
        return this;
    }

    public Menu canPlace(BiPredicate<Integer, ItemStack> predicate) {
        this.customCanPlace = predicate;
        return this;
    }

    public Menu canTake(IntPredicate predicate) {
        this.customCanTake = predicate;
        return this;
    }

    public boolean canDragInto(int slot) {
        MenuComponent component = componentSlots.get(slot);
        return isMenuSlot(slot) && (component != null && component.canDragInto(this, slot)
                || draggableSlots.contains(slot)
                || (customCanDragInto != null && customCanDragInto.test(slot)));
    }

    public boolean canPlace(int slot, ItemStack item) {
        MenuComponent component = componentSlots.get(slot);
        return isMenuSlot(slot) && (component != null && component.canPlace(this, slot, item)
                || placeableSlots.contains(slot)
                || (customCanPlace != null && customCanPlace.test(slot, item)));
    }

    public boolean canTake(int slot) {
        MenuComponent component = componentSlots.get(slot);
        return isMenuSlot(slot) && (component != null && component.canTake(this, slot)
                || takeableSlots.contains(slot)
                || (customCanTake != null && customCanTake.test(slot)));
    }

    public <T> CompletableFuture<T> loadAsync(Supplier<T> supplier) {
        return loadAsync(supplier, data -> {
        });
    }

    public <T> CompletableFuture<T> loadAsync(Supplier<T> supplier, Consumer<T> syncConsumer) {
        Objects.requireNonNull(supplier, "supplier");
        CompletableFuture<T> future = new CompletableFuture<>();

        runSync(() -> {
            state.loading(true);
            renderLoadingPlaceholder();
        });

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                T data = supplier.get();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        state.loading(false);
                        onDataLoaded(data);
                        if (syncConsumer != null) {
                            syncConsumer.accept(data);
                        }
                        renderComponents();
                        future.complete(data);
                    } catch (Throwable throwable) {
                        future.completeExceptionally(throwable);
                    }
                });
            } catch (Throwable throwable) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    state.loading(false);
                    onDataLoadFailure(throwable);
                    future.completeExceptionally(throwable);
                });
            }
        });

        return future;
    }

    public void renderLoadingPlaceholder() {
        ItemStack placeholder = state.getLoadingPlaceholder();
        if (placeholder == null) {
            return;
        }

        for (int slot = 0; slot < size; slot++) {
            renderSlot(slot, placeholder);
        }
    }

    public ItemStack getRenderedItem(int slot) {
        validateSlot(slot);
        ItemStack item = inventory.getItem(slot);
        return item == null ? null : item.clone();
    }

    public MenuComponent getComponentAt(int slot) {
        validateSlot(slot);
        return componentSlots.get(slot);
    }

    public void registerAnimation(Animation animation) {
        animator.registerAnimation(animation);
    }

    public void stopAnimation(String id) {
        animator.stopAnimation(id);
    }

    public void pauseAnimation(String id) {
        animator.pauseAnimation(id);
    }

    public void resumeAnimation(String id) {
        animator.resumeAnimation(id);
    }

    public void stopAllAnimations() {
        animator.stopAllAnimations();
    }

    public Menu fillSlots(Collection<Integer> slots, ItemStack item) {
        for (Integer slot : slots) {
            if (isMenuSlot(slot)) {
                addComponent(new StaticItemComponent(slot, item)
                        .onClick(event -> event.setCancelled(true)));
            }
        }
        return this;
    }

    public Menu fillBorder(ItemStack item) {
        return fillSlots(MenuLayout.border(size), item);
    }

    public Menu fillRow(int row, ItemStack item) {
        return fillSlots(MenuLayout.row(row), item);
    }

    public Menu fillColumn(int column, ItemStack item) {
        return fillSlots(MenuLayout.column(column, size / 9), item);
    }

    public Menu fillRectangle(int startSlot, int width, int height, ItemStack item) {
        return fillSlots(MenuLayout.rectangle(startSlot, width, height), item);
    }

    public Menu fillCircle(int centerSlot, int radius, ItemStack item) {
        return fillSlots(MenuLayout.circle(centerSlot, radius, size), item);
    }

    public List<Integer> rectangle(int startSlot, int width, int height) {
        return MenuLayout.rectangle(startSlot, width, height);
    }

    public List<Integer> circle(int centerSlot, int radius) {
        return MenuLayout.circle(centerSlot, radius, size);
    }

    public List<Integer> borderlessCenter() {
        return MenuLayout.borderlessCenter(size);
    }

    public boolean isViewing() {
        return player != null
                && player.getOpenInventory().getTopInventory().getHolder() == this;
    }

    protected void afterAllowedClick(InventoryClickEvent event, int rawSlot) {
    }

    protected void afterAllowedDrag(InventoryDragEvent event) {
    }

    protected void afterShiftClickPlaced(Collection<Integer> changedSlots) {
    }

    protected void onLockedClickDenied(InventoryClickEvent event) {
    }

    protected void onDataLoaded(Object data) {
    }

    protected void onDataLoadFailure(Throwable throwable) {
        plugin.getLogger().warning("Failed to load menu data for " + title + ": " + throwable.getMessage());
    }

    protected ItemStack createSkull(String url, String displayName) {
        ItemStack skull = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = null;
        if (skull != null) {
            meta = (SkullMeta) skull.getItemMeta();
        }
        if (meta == null) {
            return null;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), "FakeProfile");
        String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String encoded = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        profile.getProperties().put("textures", new Property("textures", encoded));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        meta.setDisplayName(color(displayName));
        skull.setItemMeta(meta);
        return skull;
    }

    protected ItemStack createButton(Material material, String name, List<String> lore, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(color(name));
        if (lore != null) {
            List<String> coloredLore = new ArrayList<>(lore.size());
            for (String line : lore) {
                coloredLore.add(color(line));
            }
            meta.setLore(coloredLore);
        }
        if (glow) {
            meta.addEnchant(Objects.requireNonNull(XEnchantment.UNBREAKING.get()), 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    protected ItemStack createButtonSkull(String textureUrl, String name, List<String> lore, boolean glow) {
        ItemStack skull = createSkull(textureUrl, name);
        if (skull == null) {
            return null;
        }

        ItemMeta meta = skull.getItemMeta();
        if (meta != null && lore != null) {
            List<String> coloredLore = new ArrayList<>(lore.size());
            for (String line : lore) {
                coloredLore.add(color(line));
            }
            meta.setLore(coloredLore);
            if (glow) {
                meta.addEnchant(Objects.requireNonNull(XEnchantment.UNBREAKING.get()), 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public static void forceCloseAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (inv.getHolder() instanceof Menu) {
                player.closeInventory();
            }
        });
    }

    private void replaceStaticSlotComponent(int slot, SlotComponent component) {
        removeStaticSlotComponent(slot);

        staticSlotComponents.put(slot, component);
        components.add(component);
        component.onAttach(this);
        registerComponentSlots(component);
        if (isViewing()) {
            component.render(this);
            component.onRender(this);
        }
    }

    private void removeStaticSlotComponent(int slot) {
        SlotComponent component = staticSlotComponents.remove(slot);
        if (component == null) {
            return;
        }

        components.remove(component);
        unregisterComponentSlots(component);
        component.onDetach(this);
    }

    private void removeStaticSlotComponents() {
        for (SlotComponent component : new ArrayList<>(staticSlotComponents.values())) {
            components.remove(component);
            unregisterComponentSlots(component);
            component.onDetach(this);
        }
        staticSlotComponents.clear();
    }

    private void registerComponentSlots(MenuComponent component) {
        for (Integer slot : component.getOwnedSlots()) {
            if (slot != null && isMenuSlot(slot)) {
                componentSlots.put(slot, component);
            }
        }
    }

    private void unregisterComponentSlots(MenuComponent component) {
        componentSlots.entrySet().removeIf(entry -> entry.getValue() == component);
        rebuildComponentLookup();
    }

    private void rebuildComponentLookup() {
        componentSlots.clear();
        for (MenuComponent component : components) {
            registerComponentSlots(component);
        }
    }

    private void routeDrag(InventoryDragEvent event) {
        Set<MenuComponent> routed = new LinkedHashSet<>();
        for (Integer rawSlot : event.getRawSlots()) {
            if (!isMenuSlot(rawSlot)) {
                continue;
            }

            MenuComponent component = componentSlots.get(rawSlot);
            if (component != null) {
                routed.add(component);
            }
        }

        for (MenuComponent component : components) {
            if (component.getOwnedSlots().isEmpty()) {
                routed.add(component);
            }
        }

        for (MenuComponent component : routed) {
            component.onDrag(this, event);
        }
    }

    private boolean isDirectMutationAllowed(InventoryClickEvent event, int rawSlot) {
        InventoryAction action = event.getAction();
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        switch (action) {
            case NOTHING:

            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                return true;

            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
            case CLONE_STACK:

            case MOVE_TO_OTHER_INVENTORY:
                return isAir(current) || canTake(rawSlot);

            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                return isAir(cursor) || canPlace(rawSlot, cursor);

            case SWAP_WITH_CURSOR:
                return (isAir(current) || canTake(rawSlot))
                        && (isAir(cursor) || canPlace(rawSlot, cursor));

            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                return isHotbarSwapAllowed(event, rawSlot);

            default:
                return false;
        }
    }

    private boolean isHotbarSwapAllowed(InventoryClickEvent event, int rawSlot) {
        if (event.getHotbarButton() < 0 || event.getWhoClicked() == null) {
            return false;
        }

        ItemStack current = event.getCurrentItem();
        ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        return (isAir(current) || canTake(rawSlot)) && (isAir(hotbarItem) || canPlace(rawSlot, hotbarItem));
    }

    private boolean handleShiftClickIntoMenu(InventoryClickEvent event) {
        if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return false;
        }

        if (isMenuSlot(event.getRawSlot())) {
            return false;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            event.setCancelled(true);
            return true;
        }

        ItemStack moving = event.getCurrentItem();
        if (isAir(moving)) {
            return false;
        }

        event.setCancelled(true);
        int sourceSlot = event.getSlot();
        Bukkit.getScheduler().runTask(plugin, () -> placeIntoAllowedSlots(clickedInventory, sourceSlot));
        return true;
    }

    private void placeIntoAllowedSlots(Inventory sourceInventory, int sourceSlot) {
        ItemStack sourceItem = sourceInventory.getItem(sourceSlot);
        if (isAir(sourceItem)) {
            return;
        }

        ItemStack moving = sourceItem.clone();
        int remaining = moving.getAmount();
        List<Integer> changedSlots = new ArrayList<>();

        for (int slot = 0; slot < size && remaining > 0; slot++) {
            if (!canPlace(slot, moving)) {
                continue;
            }

            ItemStack existing = inventory.getItem(slot);
            if (isAir(existing)) {
                ItemStack placed = moving.clone();
                int amount = Math.min(remaining, placed.getMaxStackSize());
                placed.setAmount(amount);
                renderSlot(slot, placed);
                changedSlots.add(slot);
                remaining -= amount;
                continue;
            }

            if (!existing.isSimilar(moving)) {
                continue;
            }

            int maxStack = Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize());
            int available = maxStack - existing.getAmount();
            if (available <= 0) {
                continue;
            }

            ItemStack updated = existing.clone();
            int amount = Math.min(remaining, available);
            updated.setAmount(existing.getAmount() + amount);
            renderSlot(slot, updated);
            changedSlots.add(slot);
            remaining -= amount;
        }

        if (remaining <= 0) {
            sourceInventory.clear(sourceSlot);
        } else if (remaining != sourceItem.getAmount()) {
            ItemStack remainder = sourceItem.clone();
            remainder.setAmount(remaining);
            sourceInventory.setItem(sourceSlot, remainder);
        }

        if (player != null) {
            player.updateInventory();
        }

        if (!changedSlots.isEmpty()) {
            afterShiftClickPlaced(Collections.unmodifiableList(changedSlots));
        }
    }

    private void scheduleRenderedSlotCapture(int rawSlot) {
        Bukkit.getScheduler().runTask(plugin, () -> captureRenderedSlot(rawSlot));
    }

    private void captureRenderedSlot(int rawSlot) {
        if (!isMenuSlot(rawSlot)) {
            return;
        }

        ItemStack item = normalize(inventory.getItem(rawSlot));
        if (isAir(item)) {
            previousRenderMap.remove(rawSlot);
        } else {
            previousRenderMap.put(rawSlot, item.clone());
        }
    }

    private boolean isLocked() {
        if(state instanceof TransactionState) {
            TransactionState transactionState = (TransactionState) state;
            return  transactionState.isLocked();
        }
        return false;
    }

    private ItemStack normalize(ItemStack item) {
        return isAir(item) ? null : item;
    }

    private boolean renderEquals(ItemStack first, ItemStack second) {
        if (isAir(first) && isAir(second)) {
            return true;
        }
        if (isAir(first) || isAir(second)) {
            return false;
        }
        if (first.getType() != second.getType()) {
            return false;
        }
        if (first.getAmount() != second.getAmount()) {
            return false;
        }
        if (first.hasItemMeta() != second.hasItemMeta()) {
            return false;
        }
        return Objects.equals(first.getItemMeta(), second.getItemMeta());
    }

    private boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }

    private void validateSlot(int slot) {
        if (!isMenuSlot(slot)) {
            throw new IllegalArgumentException("Slot " + slot + " is outside menu size " + size);
        }
    }

    private boolean isMenuSlot(int rawSlot) {
        return rawSlot >= 0 && rawSlot < size;
    }

    protected void ensureMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Menu inventory mutation must run on the Bukkit main thread");
        }
    }

    protected void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    protected String color(String value) {
        return value == null ? null : value.replace("&", "\u00A7");
    }

    public void setState(MenuState state) {
        this.state = state == null ? new MenuState() : state;
    }


}
